import uvicorn
import yaml
import argparse

from fastapi import FastAPI

config_path = 'config/decay.yaml'
args = argparse.Namespace(config_path=config_path)
with open(args.config_path, 'r') as file:
    try:
        config = yaml.safe_load(file)
    except yaml.YAMLError as exc:
        print(exc)
        
app = FastAPI()

@app.get("/")
async def root():
    return {
        "message": "Hello world"
    }
      
if __name__ == "__main__":
    uvicorn.run("main:app", host='100.68.49.61', port=8081, reload=True) 