
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

#START: RenderPage
class ChatRoomPage(webapp2.RequestHandler):
  def get(self):
    user = users.get_current_user()
    if user is None: 
      self.redirect(users.create_login_url(self.request.uri))
    else:
      self.response.headers["Content-Type"] = "text/html"
      messages = db.GqlQuery("SELECT * From ChatMessage ORDER BY timestamp")
      template_values = {
        'title': "MarkCC's AppEngine Chat Room",
        'msg_list': messages,
        }
      path = os.path.join(os.path.dirname(__file__), 'chat-template.html')#<callout id="path.create"/>
      page = template.render(path, template_values) #<callout id="template.render"/>
      self.response.write(page)
#END: RenderPage


#START: ChatRoomPoster
class ChatRoomPoster(webapp2.RequestHandler):
  def post(self):
    user = users.get_current_user()
    msgtext = self.request.get("message")
    msg = ChatMessage(user=user.nickname(), message=msgtext, chat="chat")
    msg.put() 
    # Now that we've added the message to the chat, we'll redirect
    # to the root page,
    self.redirect('/')
#END: ChatRoomPoster

# START: Frame
chatapp = webapp2.WSGIApplication([('/', ChatRoomPage),
                                  ('/talk', ChatRoomPoster)])
# END: Frame



