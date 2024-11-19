import torch

import yaml
import argparse
import uvicorn
import cv2
import io
import numpy as np
import gdown
import os
import base64
from pydantic import BaseModel

from pytorchyolo.models import load_model
from predict import predict
from fastapi import FastAPI
from fastapi.responses import StreamingResponse

config_path = 'config/decay.yaml'
config = argparse.Namespace(config_path=config_path)
with open(config.config_path, 'r') as file:
    try:
        args = yaml.safe_load(file)
    except yaml.YAMLError as exc:
        print(exc)

yolov3_model = load_model(args['model'], args['weights'])
yolov3_model.eval()
device = torch.device('cuda' if torch.cuda.is_available() else 'cpu')
       
app = FastAPI()

class Input(BaseModel):
    img_base64: str
    
@app.get("/")
async def root():
    return {
        "message": "Hello world"
    }

@app.post("/api/predict")
async def predict_api(input_data: Input):
    base64_image = input_data.img_base64
    if "," in base64_image:
        base64_image = base64_image.split(",")[1]
      
    image_data = base64.b64decode(base64_image)
    nparr = np.frombuffer(image_data, np.uint8)
    image = cv2.imdecode(nparr, cv2.IMREAD_COLOR)
    
    output_img = predict(image, yolov3_model, device, args['conf_thres'], args['nms_thres'])
    
    _, buffer = cv2.imencode('.jpg', output_img)
    io_buf = io.BytesIO(buffer)
    
    return StreamingResponse(io_buf, media_type="image/png")
     
if __name__ == "__main__":
    uvicorn.run("main:app", host='100.68.49.61', port=8081, reload=True) 