from flask import Flask, request
import base64
import boto
import sys
import os
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


def percent_cb(complete, total):
	sys.stdout.write('.')
	sys.stdout.flush()	

def upload_to_s3(filename):
	key = Key(bucket)
	key.key = image_name
	key.set_contents_from_filename(filename, cb = percent_cb, num_cb = 10)


@app.route('/image', methods=['POST'])
def image():
	if request.method == 'POST':
		data = request.form
		base64_data = data['base64']
		print(len(base64_data))
		image_data = base64.b64decode(base64_data)
		image_name = str(data['ImageName'])
		write_to_file(image_name, image_data)
		print("****************************")
		upload_to_s3(image_name)
	return "Yo"   

if __name__ == '__main__':
	app.run(host='0.0.0.0', debug = True)
