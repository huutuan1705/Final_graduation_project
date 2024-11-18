import uvicorn
import yaml
import argparse

from pytorchyolo.models import load_model
from fastapi import FastAPI

config_path = 'config/decay.yaml'
config = argparse.Namespace(config_path=config_path)
with open(config.config_path, 'r') as file:
    try:
        args = yaml.safe_load(file)
    except yaml.YAMLError as exc:
        print(exc)

yolov3_model = load_model(args['model'], args['weights'])
yolov3_model.eval()

       
app = FastAPI()

@app.get("/")
async def root():
    return {
        "message": "Hello world"
    }
      
if __name__ == "__main__":
    uvicorn.run("main:app", host='100.68.49.61', port=8081, reload=True) 