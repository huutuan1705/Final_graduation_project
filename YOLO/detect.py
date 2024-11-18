import yaml
import argparse
import cv2
import torch

import numpy as np

from pytorchyolo.models import load_model
from pytorchyolo.utils.utils import non_max_suppression, rescale_boxes

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

def predict(image, model, device, conf_thres, nms_thres):
    image_arr = cv2.imread(image, cv2.IMREAD_COLOR)
    image_tensor = torch.tensor(image_arr).permute(2, 0, 1).float().to(device) / 255.0
    image_tensor = image_tensor.unsqueeze(0)
    
    with torch.no_grad():
        predictions = model(image_tensor)
        detections = non_max_suppression(predictions, conf_thres, nms_thres)
        
    return detections

image_path = "images/image_2874.jpg"
predictions = predict(image_path, yolov3_model, device, args['conf_thres'], args['nms_thres'])
print(predictions[0])