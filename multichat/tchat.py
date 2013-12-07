from google.appengine.ext import db
from google.appengine.api import users
import webapp2
from google.appengine.ext.webapp import template

import datetime
import os


#START: ChatMessage
class ChatMessage(db.Model):
    user = db.StringProperty(required=True)
    timestamp = db.DateTimeProperty(auto_now_add=True)
    message = db.TextProperty(required=True)
    chat = db.StringProperty(required=True)

CHATS = ['main', 'book', 'flame' ]
#END: ChatMessage

#START: GenericChat
class GenericChatPage(webapp2.RequestHandler):
  def get(self):
    requested_chat = self.request.get("chat", default_value=None)#<callout id="co.getchat"/>
    if requested_chat == None or requested_chat not in CHATS:#<callout id="co.handle_errors"/>
      template_params = {
          'title': "Error! Requested chat not found!",
          'chatname': requested_chat,
          'chats': CHATS
          }
      error_template = os.path.join(os.path.dirname(__file__), 'error.html')
      page = template.render(error_template, template_params)
      self.response.write(page)
    else:
      messages = db.GqlQuery("SELECT * from ChatMessage WHERE chat = :1 "#<callout id="co.handle_named_chat"/>
                             "ORDER BY timestamp", requested_chat)
      template_params = {
          'title': "MarkCC's AppEngine Chat Room",
          'msg_list': messages,
          'chat': requested_chat,
          'chats': CHATS
          }
      path = os.path.join(os.path.dirname(__file__), 'multichat.html')
      page = template.render(path, template_params)
      self.response.write(page)
#END: GenericChat    

#START: ChatRoomCounted
class ChatRoomCountedHandler(webapp2.RequestHandler):
  def get(self):
    user = users.get_current_user()
    if user is None: 
      self.redirect(users.create_login_url(self.request.uri))
    else:
      self.response.headers["Content-Type"] = "text/html"
      messages = db.GqlQuery("SELECT * From ChatMessage ORDER BY timestamp "
                             "DESC LIMIT 20")
      msglist = messages.fetch()
      for msg in msglist:
        msg.deltatime = datetime.datetime.now() - msg.timestamp
      template_values = {
        'title': "MarkCC's AppEngine Chat Room",
        'msg_list': messages.fetch(),
        }
      path = os.path.join(os.path.dirname(__file__), 'count.html')
      page = template.render(path, template_values)
      self.response.write(page)
#END: ChatRoomCounted


#START: LandingPage
class ChatRoomLandingPage(webapp2.RequestHandler):
  def get(self):
    user = users.get_current_user()
    if user is None: 
      self.redirect(users.create_login_url(self.request.uri))
    else:
      self.response.headers["Content-Type"] = "text/html"
      messages = db.GqlQuery("SELECT * From ChatMessage ORDER BY timestamp "
                             "DESC LIMIT 20")
      template_values = {
        'title': "MarkCC's AppEngine Chat Room",
        'msg_list': messages,
        'chats': CHATS
        }
      path = os.path.join(os.path.dirname(__file__), 'landing.html')
      page = template.render(path, template_values)
      self.response.write(page)
#END: LandingPage

#START: ChatRoomPoster
class ChatRoomPoster(webapp2.RequestHandler):
  def post(self):
    user = users.get_current_user()
    msgtext = self.request.get("message")
    chat = self.request.get("chat")
    msg = ChatMessage(user=user.nickname(), message=msgtext, chat=chat)
    msg.put() 
    # Now that we've added the message to the chat, we'll redirect
    # to the root page,
    self.redirect('/enterchat?chat=%s' % chat)
#END: ChatRoomPoster

# START: Frame
chatapp = webapp2.WSGIApplication([('/', ChatRoomLandingPage),
                                  ('/talk', ChatRoomPoster),
                                  ('/enterchat', GenericChatPage)])

# END: Frame
