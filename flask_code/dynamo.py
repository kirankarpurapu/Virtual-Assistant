# import boto.dynamodb
import numpy as np
# connection_dynamodb = boto.dynamodb.connect_to_region('us-west-2')
# table_name = 'PhotoInfo-Recognito'
# table = connection_dynamodb.get_table(table_name)

# print( connection_dynamodb.describe_table(table_name))

# url = "xyz.com"
# user_id = str(1)
# photo_id = 'dgdfshdghgdh'
# # item = {
# # 	'S3_URL' : url,
# # 	'random' : 'kiran',
# # 	'haha' : '{abcd : efgh}',
# # 	}
# # item_row = table.new_item(hash_key = user_id, range_key = str(photo_id), attrs = item)
# # response = item_row.put()

# image_data = { 'phonenumber' : '1234', 'email' : '@abc.com', 'additional info' : 'kirankarpuarpu'}


# item = {
# 		'S3_URL' : url,
# 		'phone' : image_data['phonenumber'],
# 		'email' : image_data['email'],
# 		'additional info' : str(image_data['additional info'])
# 		}
# item_row = table.new_item(hash_key = str(user_id), range_key = str(photo_id), attrs = item)
# response = item_row.put()
# print(response)
arr =np.array([1,2,3,4,5,6])
print(type(arr))
print(arr)
ts = arr.tostring()
print("ts type", type(ts))
print(ts)
print np.fromstring(ts,dtype=int)