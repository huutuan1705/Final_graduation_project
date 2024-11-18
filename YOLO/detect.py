import yaml
import argparse
import cv2
import torch
import numpy as np
import random
import os
import matplotlib.pyplot as plt
import matplotlib.patches as patches
from matplotlib.ticker import NullLocator

from pytorchyolo.models import load_model
from pytorchyolo.utils.utils import non_max_suppression, rescale_boxes
from PIL import Image

classes = ['mild_decay', 'moderate_decay', 'severe_decay']
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

def draw_picture(image, model, device, conf_thres, nms_thres):
    image = cv2.imread(image)
    
    image_rgb = cv2.cvtColor(image, cv2.COLOR_BGR2RGB)
    image_tensor = torch.tensor(image_rgb).permute(2, 0, 1).float().to(device) / 255.0
    image_tensor = image_tensor.unsqueeze(0)  
    
    model.eval()
    with torch.no_grad():
        predictions = model(image_tensor)
        detections = non_max_suppression(predictions, conf_thres, nms_thres)
        
    list_label = ["mild", "moderate", "severe"]
    for x1, y1, x2, y2, conf, cls_pred in detections[0]:
        x1, y1, x2, y2 = int(x1), int(y1), int(x2), int(y2)
        cv2.rectangle(image_rgb, (x1, y1), (x2, y2), (0, 255, 0), 2)
        cv2.putText(image_rgb, f'{list_label[int(cls_pred)]}: {conf:.2f}', 
                        (x1-20, y1 - 10), cv2.FONT_HERSHEY_SIMPLEX, 0.5, (255, 255, 255), 1)
    
    output_image = cv2.cvtColor(image_rgb, cv2.COLOR_RGB2BGR)
    return output_image

def draw_image(image_path, detections, output_path):
    img = np.array(Image.open(image_path))
    plt.figure()
    
    fig, ax = plt.subplots(1)
    ax.imshow(img)

    for x1, y1, x2, y2, conf, cls_pred in detections:
        box_w = x2 - x1
        box_h = y2 - y1

        color = "#00FF00"
        # Create a Rectangle patch
        bbox = patches.Rectangle((x1, y1), box_w, box_h, linewidth=2, edgecolor=color, facecolor="none")
        # Add the bbox to the plot
        ax.add_patch(bbox)
        # Add label
        plt.text(
            x1 - 10,
            y1 - 20,
            s=f"{int(cls_pred)}: {conf:.2f}",
            color="white",
            verticalalignment="top",
            # bbox={"color": color, "pad": 0}
        )

    # Save generated image with detections
    plt.axis("off")
    plt.gca().xaxis.set_major_locator(NullLocator())
    plt.gca().yaxis.set_major_locator(NullLocator())
    filename = os.path.basename(image_path).split(".")[0]
    output_path = os.path.join(output_path, f"{filename}.jpg")
    plt.savefig(output_path, bbox_inches="tight", pad_inches=0.0)
    plt.close()
  
image_path = "images/image_2900.jpg"
output_path = "outputs/image_2900.jpg"
# predictions = predict(image_path, yolov3_model, device, args['conf_thres'], args['nms_thres'])
# draw_image(image_path, predictions[0], classes, output_path)

output_img = draw_picture(image_path, yolov3_model, device, args['conf_thres'], args['nms_thres'])
cv2.imwrite(output_path, output_img) 