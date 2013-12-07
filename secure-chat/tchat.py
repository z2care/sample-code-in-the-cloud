import webapp2
from google.appengine.api import users
from google.appengine.ext import db
import datetime
import os

from google.appengine.ext.webapp import template

#START: ChatMessage
class ChatMessage(db.Model):
    user = db.StringProperty(required=True)
    timestamp = db.DateTimeProperty(auto_now_add=True)
    message = db.TextProperty(required=True)
    chat = db.StringProperty(required=True)

CHATS = ['main', 'book', 'flame' ]
#END: ChatMessage

#START: UserRoles
class UserRole(db.Model):
    name = db.StringProperty(required=True)
    role = db.StringProperty(choices=["User", "admin", "privileged"],
                             default="User")

    @staticmethod
    def GetUserRole(name):
       user_record = db.GqlQuery("SELECT * from UserRole WHERE " +
                                  "name = :1",
                                  name).get()
       if user_record != None:
         return user.role
       else:
	     return "User"
#END: UserRoles    

#START: ValidateRole
def ValidateUserRole(actual, required):
    if required == "admin": #<callout id="co.admin-check"/>
        return actual == "admin"
    elif required == "privileged": #<callout id="co.priv-check"/>
        return (actual == "admin" and actual or "privileged")
    elif required == "User":
	return True
    else: #<callout id="co.priv-default"/>
        return False
#END: ValidateRole

#START: NewChatRoom
class NewChatRoomHandler(webapp2.RequestHandler):
  '''@login_required''' #<callout id="co.login-decorator"/>
  #http://djangosnippets.org/snippets/691/
  #http://flask.pocoo.org/docs/patterns/viewdecorators/
  def get(self):
    user = users.get_current_user()
    role = GetUserRole(user) #<callout id="co.role-check"/>
    if not ValidateRole(role, "privileged"):
        self.response.headers["Context-Type"] = "text/html"
        self.response.write(
            "<html><head>\n" +
            "<title>Insufficient Privileges</title>\n" +
            "</head>\n" +
            "<body><h1>Insufficient Privileges</h1>\n" +
            "<p> I'm sorry but you aren't allowed to " +
            "access this page</p>\n" +
            "</body></html>\n")
    else:
      self.response.headers["Content-Type"] = "text/html"
      template_values = {
        'title': "MarkCC's AppEngine Chat Room",
        }
      path = os.path.join(os.path.dirname(__file__), 'new-chat.html')
      page = template.render(path, template_values)
      self.response.write(page)
#END: NewChatRoom

#START: NewChatRoomPost
class NewChatRoomPostHandler(webapp2.RequestHandler):
  '''@login_required'''
  def post(self):
    user = users.get_current_user()
    role = GetUserRole(user) 
    if not ValidateRole(role, "privileged"):
        self.response.headers["Context-Type"] = "text/html"
        self.response.write(
            "<html><head><title>Insufficient Privileges</title></head>\n" +
            "<body><h1>Insufficient Privileges</h1>\n" +
            "<p> I'm sorry but you aren't allowed to access this page</p>\n" +
            "</body></html>\n")
    else:
        newchat = cgi.escape(self.request.get("newchat"))
        CreateChat(user, newchat)
        self.response.write(
            "<html><head><title>Chat Room Created</title></head>\n" +
            "<body><h1>Chat Room Created</h1>\n" +
            "<p> New chat room %s created.</p>\n" 
            "</body></html>\n" % newchat)
#END: NewChatRoomPost

#START: GenericChat
class GenericChatPage(webapp2.RequestHandler):
  def get(self):
    requested_chat = self.request.get("chat", default_value="none")
    if requested_chat == "none" or requested_chat not in CHATS:
      template_params = {
          'title': "Error! Requested chat not found!",
          'chatname': requested_chat,
          }
      error_template = os.path.join(os.path.dirname(__file__), 'error.html')
      page = template.render(error_template, template_params)
      self.response.write(page)
    else:
      messages = db.GqlQuery("SELECT * from ChatMessage WHERE chat = :1 "
                             "ORDER BY time", requested_chat)
      template_params = {
          'title': "MarkCC's AppEngine Chat Room",
          'msg_list': messages,
          'chat': requested_chat
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
      messages = db.GqlQuery("SELECT * From ChatMessage ORDER BY time "
                             "DESC LIMIT 20")
      msglist = list(messages).reverse()
      for msg in msglist:
        msg.deltatime = datetime.datetime.now() - msg.timestamp
      template_values = {
        'title': "MarkCC's AppEngine Chat Room",
        'msg_list': messages,
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
#      msglist = list(messages).reverse()
      template_values = {
        'title': "MarkCC's AppEngine Chat Room",
        'msg_list': messages,
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
    self.redirect('/enterchat&chat=%s' % chat)
#END: ChatRoomPoster

# START: Frame
chatapp = webapp2.WSGIApplication([('/', ChatRoomLandingPage),
                                  ('/talk', ChatRoomPoster),
                                  ('/enterchat', GenericChatPage)])

# END: Frame

