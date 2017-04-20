import os
import sys
import requests
import json

from urllib2 import HTTPError
from urllib import quote
from urllib import urlencode

from watson_developer_cloud import ConversationV1

# Global Yelp API Constants.
API_HOST = 'https://api.yelp.com'
SEARCH_PATH = '/v3/businesses/search'
BUSINESS_PATH = '/v3/businesses/'
TOKEN_PATH = '/oauth2/token'
GRANT_TYPE = 'client_credentials'
    
def obtain_bearer_token(host, path):
    CLIENT_ID = os.environ['yelpclientid']
    CLIENT_SECRET = os.environ['yelpclientsecret']
    
    url = '{0}{1}'.format(host, quote(path.encode('utf8')))
    assert CLIENT_ID, "Please supply your client_id."
    assert CLIENT_SECRET, "Please supply your client_secret."
    data = urlencode({
        'client_id': CLIENT_ID,
        'client_secret': CLIENT_SECRET,
        'grant_type': 'client_credentials',
    })
    headers = {
        'content-type': 'application/x-www-form-urlencoded',
    }
    response = requests.request('POST', url, data=data, headers=headers)
    bearer_token = response.json()['access_token']
    return bearer_token


def request(host, path, bearer_token, url_params=None):
    url_params = url_params or {}
    url = '{0}{1}'.format(host, quote(path.encode('utf8')))
    headers = {
        'Authorization': 'Bearer %s' % bearer_token,
    }

    response = requests.request('GET', url, headers=headers, params=url_params)

    return response.json()

def search(bearer_token, term, location):
    url_params = {
        'term': term.replace(' ', '+'),
        'location': location.replace(' ', '+'),
        'limit': 3
    }
    return request(API_HOST, SEARCH_PATH, bearer_token, url_params=url_params)

def lambda_handler(event, context):
    DEFAULT_LOCATION = 'New York, NY'

    bearer_token = obtain_bearer_token(API_HOST, TOKEN_PATH)
    
    conversation = ConversationV1(
      username=os.environ['username'],
      password=os.environ['password'],
      version='2017-04-21',
      url='https://gateway.watsonplatform.net/conversation/api')
    
    inputText = 'I want to eat noodles!'
    response = conversation.message(workspace_id=os.environ['workspaceid'],
                            message_input={'text': inputText})

    outputText = response['output']['text'][0]
    entityObj = response['entities']
    
    if(entityObj == []):
        return outputText
    else:
        location = entityObj[0]['location']
        start, end = int(location[0]), int(location[1])
        entity = inputText[start:end]
        
        places = None
        try:
            places = search(bearer_token, entity, DEFAULT_LOCATION)
            places = places['businesses']
            assert len(places) == 3
            
            outputText += "\n"
            for index in range(len(places)):
                outputText += str(index+1) + " " + places[index]['url'] + "\n"
            return outputText[:len(outputText)-1]
        except:
            return "Sorry! I was not able to retrieve what you wanted! Pleae enter text again so that I can try again!"
