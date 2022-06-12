from flask import Flask, request, make_response, jsonify
from PIL import Image
import os
from machine_learning import predict_image, generate_recipe
# from generate_recipe import generate

#~/.local/bin/gunicorn -b :8080 main:app
app = Flask(__name__)

@app.route('/predict', methods=['GET', 'POST'])
def model():
    if request.method == 'POST':
        # Mengambil data dari upload
        image = request.files.get('file')
        prosesdata = predict_image(image.filename)
        hasil = generate_recipe(prosesdata)
        responseBody = {'result':hasil}
        return make_response(jsonify(responseBody),200)

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=80)
