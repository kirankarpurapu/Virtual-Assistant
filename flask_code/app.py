from flask import Flask, request
import base64
import boto
import boto.dynamodb
import sys
import os
import json
import dlib
import uuid
import thread
import scipy.ndimage
from scipy.ndimage import imread
import numpy as np
from boto.s3.key import Key
from werkzeug import secure_filename

app = Flask(__name__)
app.config['MAX_CONTENT_LENGTH'] = 500 * 1024 * 1024  # for 20MB max-limit.

# aws connections
# connection_s3 = boto.connect_s3(profile_name = "Recognito-account")
connection_s3 = boto.connect_s3()
connection_dynamodb = boto.dynamodb.connect_to_region('us-west-2')


# aws resources
bucket = connection_s3.get_bucket('recognito')
table_name = 'PhotoInfo-Recognito'
table = connection_dynamodb.get_table(table_name)


#photo recognition params
TOLERANCE = 0.4


# helper functions

def compare_face_encodings(known_faces, face):
    values = np.linalg.norm(known_faces - face, axis=1)
    print("The {0} comparision value(s) are ").format(len(values))
    print(values)
    print("The tolerance is {0}").format(TOLERANCE)
    matches = (values <= TOLERANCE)
    return matches

def write_to_dynamodb(user_id, photo_id, contact_id, image_name, url, encodings):
	print("****************************")
	print("user id", user_id)
	print("photo id", photo_id)
	print("contact id", contact_id)
	print("url", url)
	print("encodings length", len(encodings))
	encoding_string = ','.join(str(x) for x in encodings)
	print( connection_dynamodb.describe_table(table_name))
	item = {
		'S3_URL' : url,
		'encodings' : encoding_string,
		'contactid' : contact_id,
		'imagename' : image_name
		}
	item_row = table.new_item(hash_key = str(user_id), range_key = str(photo_id), attrs = item)
	dynamodb_response = item_row.put()
	print("The response from dynamo db is", dynamodb_response)
	return dynamodb_response


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
	key.set_metadata('Content-Type', 'image/jpeg')
	key.set_acl('public-read')
	url = key.generate_url(expires_in=0, query_auth=False)
	print("The S3 url is ", url)
	return url

def get_face_encodings(path_to_image):
	print("The path of the photo is ", path_to_image)
	# path_to_image ='/Users/kirank/Documents/recongnito/flask/kiran.jpg'
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

def write_to_database(image_info, image_name, encodings):
	print("*********************************")
	print("trying to upload to S3")
	url = upload_to_s3(image_name)
	print("The S3 url is ", url)
	print("*********************************")
	user_id = 123
	u_id = uuid.uuid4()
	# need to write to the dynamo db
	dynamodb_response = write_to_dynamodb(user_id, u_id, image_info, image_name, url, encodings)
	return dynamodb_response

def get_matching_photo_details(image):
	# get all the rows from dynamo db
	images = table.scan()
	
	encodings_array = []
	info_array = []
	for item in images:
		contact_id = item['contactid']
		person_name = item['imagename']
		encodings = item['encodings']
		user_dict = {}
		user_dict['contactid'] = contact_id
		user_dict['name'] = person_name
		user_dict['encodings'] = encodings
		info_array.append(user_dict)
		encodings_values_as_array = np.array([float(x) for x in str(encodings).split(',')])
		encodings_array.append(encodings_values_as_array)

	
	print("found {0} images in the database").format(len(encodings_array))
	# get the encodings of this photo
	test_encodings = np.array(get_face_encodings(image))
	if (len(test_encodings) < 1):
		return_dict = {}
		return_dict['message'] = 'no face in the image'
		return_dict['contact_id'] = -1
		json_return_value = json.dumps(return_dict)
		return json_return_value
	if(len(test_encodings) > 1):
		return_dict = {}
		return_dict['message'] = 'cannot handle multiple people'
		return_dict['contact_id'] = -1
		json_return_value = json.dumps(return_dict)
		return json_return_value	
	else:
		matches = compare_face_encodings(encodings_array, test_encodings)
		print("matches ", matches)
		print("names")
		for person in info_array:
			print("person: " ,person['name'])
		return process_matches(matches, info_array)

def process_matches(matches, info_array):	
	match_found = False
	counter = 0
	for match in matches:
		if match:
			match_found = True
			print("found a match")
			break
		counter = counter + 1	
	if not match_found:
		print("no user at all")
		return_dict = {}
		return_dict['message'] = 'could not identify'
		return_dict['contact_id'] = -1
		json_return_value = json.dumps(return_dict)
		return json_return_value
	else:
		if counter < len(info_array):
			print(info_array[counter])
			return_dict = {}
			return_dict['message'] = 'found a match'
			return_dict['contact_id'] = info_array[counter]['contactid']
			json_return_value = json.dumps(return_dict)
			return json_return_value
		else:
			return_dict = {}
			return_dict['message'] = 'something fishy!'
			return_dict['contact_id'] = -1
			json_return_value = json.dumps(return_dict)
			return json_return_value
				

# routes

@app.route("/")
def hello():
    return "<h1 style='color:blue'>Welcome to Recognito!</h1>"


#type: POST, params = 'base64', 'ImageName', 'ImageInfo'



# @app.route('/newS3Image', methods = ['POST'])
# def s3_image():
# 	result_json_dict = {}
# 	if request.method == 'POST':
# 		print("got a new image post request")	
#       	data = request.form
#       	if 'contactid' not in data or 's3_url' not in data:	
#       		result_json_dict['message'] = 'insufficient data sent, please retry'
#       		result_json_dict['result_status'] = -1
#       		json_result = json.dumps(result_json_dict)
#       		return json_result
#       	else:		
# 			image_info = str(data['contactid'])
# 			s3_url = str(data['s3_url'])
# 			print("got all the required info")
# 			print("the id of the contact is ", image_info)
# 			print("the url of the image is ", s3_url)
			# encodings = get_face_encodings(image_name)
			# if len(encodings) == 1:
			# 	encodings = encodings[0]
			# 	print("The encoding for the photo:", encodings)
			# 	# thread.start_new_thread(write_to_database, (image_info, image_name, encodings, ))
			# 	write_to_database(image_info, image_name, encodings)
			# elif len(encodings) > 1:
			# 	print("cannot handle multiple people in the image")
			# 	result_json_dict['message'] = 'cannot handle multiple people in the image'
			# 	result_json_dict['result_status'] = -1
			# 	json_result = json.dumps(result_json_dict)
			# 	return json_result
			# else:
			# 	print("no human found in the photo")
			# 	result_json_dict['message'] = 'no human found in the photo'
			# 	result_json_dict['result_status'] = -1
			# 	json_result = json.dumps(result_json_dict)
			# 	return json_result
			# result_json_dict['message'] = 'Successfully added the new photo'
			# result_json_dict['result_status'] = 1
			# json_result = json.dumps(result_json_dict)
			# return json_result	 


@app.route('/newImage', methods=['POST'])
def new_image():
	result_json_dict = {}
	if request.method == 'POST':
		print("got a new image post request")
		files = request.files
		if 'file' not in files:
			print("no file found in the new image post request")
			result_json_dict['message'] = 'no file found in the new image post request'
			result_json_dict['result_status'] = -1
			json_result = json.dumps(result_json_dict)
			return json_result
		f = request.files['file']
		if f.filename == '':
			print("no filename found")
			result_json_dict['message'] = 'no file found in the new image post request'
			result_json_dict['result_status'] = -1
			json_result = json.dumps(result_json_dict)
			return json_result
		# image_name = secure_filename(f.filename)
		if 'imagename' not in request.form:
			print ("image name not found")	
		else:
			print("The name of the image is ", request.form['imagename'])	
		image_name = request.form['imagename']
		# image_name = "kiraninfor.jpg"
      	f.save(image_name)
      	print("The name of the file is ", image_name)
      	print("new method to save the file worked")
      	data = request.form
      	if 'contactid' not in data:	
      		result_json_dict['message'] = 'insufficient data sent, please retry'
      		result_json_dict['result_status'] = -1
      		json_result = json.dumps(result_json_dict)
      		return json_result
      	else:		
			image_info = str(data['contactid'])
			print("got all the required info")
			print("the id of the contact is ", image_info)
			# image_json = json.loads(image_info)
			# file = write_to_file(image_name, image_data)
			encodings = get_face_encodings(image_name)
			if len(encodings) == 1:
				encodings = encodings[0]
				print("The encoding for the photo:", encodings)
				# thread.start_new_thread(write_to_database, (image_info, image_name, encodings, ))
				write_to_database(image_info, image_name, encodings)
			elif len(encodings) > 1:
				print("cannot handle multiple people in the image")
				result_json_dict['message'] = 'cannot handle multiple people in the image'
				result_json_dict['result_status'] = -1
				json_result = json.dumps(result_json_dict)
				return json_result
			else:
				print("no human found in the photo")
				result_json_dict['message'] = 'no human found in the photo'
				result_json_dict['result_status'] = -1
				json_result = json.dumps(result_json_dict)
				return json_result
			result_json_dict['message'] = 'Successfully added the new photo'
			result_json_dict['result_status'] = 1
			json_result = json.dumps(result_json_dict)
			return json_result	  


# type : POST, params: 'base64'
@app.route('/testImage', methods = ['POST'])
def test_image():
	if request.method == 'POST':
		print("TESTING THE IMAGE")
		data = request.form
		if 'base64' not in data:
			return 'insufficient data in the post request'	
		base64_data = data['base64']
		image_data = base64.b64decode(base64_data)
		test_file = write_to_file("test.jpg", image_data)
		return_value_json = get_matching_photo_details("test.jpg")
		print("returning the following value")
		print(return_value_json)
		return return_value_json

#error handlers

@app.errorhandler(413)
def request_entity_too_large(error):
	print("REQUEST ENTITY TOO LARGE")
	return 'File Too Large'


# starting the server

if __name__ == '__main__':
	# app.config.update(MAX_CONTENT_LENGTH = 50000000)
	app.run(host='0.0.0.0',port = 8000,  debug = True)
