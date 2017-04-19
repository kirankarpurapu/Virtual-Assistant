from flask import Flask, request
import base64
import boto
import sys
import os
import json
import dlib
import scipy.ndimage
from scipy.ndimage import imread
import numpy as np
from boto.s3.key import Key

app = Flask(__name__)
connection = boto.connect_s3(profile_name = "Recognito-account")
bucket = connection.get_bucket('recognito')

@app.route('/')
def hello():
    return "Hello World!"

def write_to_file(filename, file_data):
	file = open(filename, 'w')
	file.write(file_data)
	file.close()
	return file


def percent_cb(complete, total):
	sys.stdout.write('.')
	sys.stdout.flush()	

def upload_to_s3(filename):
	key = Key(bucket)
	key.key = filename
	key.set_contents_from_filename(filename, cb = percent_cb, num_cb = 10)
	url = key.generate_url(expires_in=300)
	return url

def get_face_encodings(path_to_image):
	print("The path of the photo is ", path_to_image)
	# path_to_image = '/Users/kirank/Documents/recongnito/images/kiran.jpg'
	path_to_image ='/Users/kirank/Documents/recongnito/flask/kiran.jpg'
	face_detector = dlib.get_frontal_face_detector()
	shape_predictor = dlib.shape_predictor('./../shape_predictor_68_face_landmarks.dat')
	face_recognition_model = dlib.face_recognition_model_v1('./../dlib_face_recognition_resnet_model_v1.dat')
	TOLERANCE = 0.6
	image = scipy.ndimage.imread(path_to_image)
	detected_faces = face_detector(image, 1)
	for face in detected_faces:
		print("detected the following face, ", face)
	shapes_faces = [shape_predictor(image, face) for face in detected_faces]
	return [np.array(face_recognition_model.compute_face_descriptor(image, face_pose, 1)) for face_pose in shapes_faces]	

def write_to_database(url, json, image):
	encodings = get_face_encodings(image)
	print("The encoding for the photo:", encodings)
	return 1


@app.route('/newImage', methods=['POST'])
def image():
	if request.method == 'POST':
		data = request.form
		base64_data = data['base64']
		image_data = base64.b64decode(base64_data)
		image_name = str(data['ImageName'])
		image_info = str(data['ImageInfo'])
		image_json = json.loads(image_info)
		file = write_to_file(image_name, image_data)
		print("****************************")
		# url = upload_to_s3(image_name)
		url = "kiran"
		write_to_database(url, image_json, image_name)
		print("Image info", image_json['name'], url)
	return "Yo"   

if __name__ == '__main__':
	app.run(host='0.0.0.0', debug = True)
