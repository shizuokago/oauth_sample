#!/usr/bin/env python

import os
import sys 
import jinja2
import webapp2

sys.path.insert(0, 'libs')

from webapp2_extras import sessions

JINJA_ENVIRONMENT = jinja2.Environment(
    loader=jinja2.FileSystemLoader(os.path.dirname(__file__)),
    extensions=['jinja2.ext.autoescape'],
    autoescape=True)

from oauth2client.client import OAuth2WebServerFlow
import httplib2
from apiclient.discovery import build

class SessionEnabledHandler(webapp2.RequestHandler):
    def dispatch(self):

        self.session_store = sessions.get_store(request=self.request)

        try:
            webapp2.RequestHandler.dispatch(self)
        finally:
            self.session_store.save_sessions(self.response)

    @webapp2.cached_property
    def session(self):
        return self.session_store.get_session(backend='memcache')

class Index(SessionEnabledHandler):
    def get(self):
        template = JINJA_ENVIRONMENT.get_template('index.html')
        params = {}
        self.response.write(template.render(params))

class Request(SessionEnabledHandler):
    def post(self):

        cid = self.request.get('cid')
        secret = self.request.get('csecret')
        flow = OAuth2WebServerFlow(client_id=cid,
                                   client_secret=secret,
                                   scope='https://www.googleapis.com/auth/gmail.readonly',
                                   redirect_uri='http://python.kneetenzero.appspot.com/callback')
        self.session['flow'] = flow

        auth_uri = flow.step1_get_authorize_url()
        self.redirect(auth_uri)

class Callback(SessionEnabledHandler):

    def get(self):

        flow = self.session.get('flow')
        code = self.request.get('code')
        credentials = flow.step2_exchange(code)

        http = credentials.authorize(httplib2.Http())
        service = build('gmail', 'v1', http=http)

        results = service.users().labels().list(userId='me').execute()
        labels = results.get('labels', [])
        if not labels:
            self.response.out.write('No labels found.')
        else:
            print('Labels:')
            for label in labels:
                self.response.out.write(label['name'])



config = {}
config['webapp2_extras.sessions'] = { 'secret_key' : 'my-secret-key' }
app = webapp2.WSGIApplication([
    ('/', Index),
    ('/request', Request),
    ('/callback', Callback),
], config=config,debug=True)

