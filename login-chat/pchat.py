from google.appengine.api import users
from google.appengine.ext import db
import webapp2
import datetime
import sys

#START: ChatMessage
class ChatMessage(db.Model):
  user = db.StringProperty(required=True)
  timestamp = db.DateTimeProperty(auto_now_add=True)
  message = db.TextProperty(required=True)

  def __str__(self):
    return "%s (%s): %s" % (self.user, self.timestamp, self.message)
#END: ChatMessage

#START: ChatLogin
class ChatRoomPage(webapp2.RequestHandler):
  def get(self):
    user = users.get_current_user()
    if user is None: #<callout id="co.login-test"/>
      self.redirect(users.create_login_url(self.request.uri))
    else:
      self.response.headers["Content-Type"] = "text/html"
      self.response.write("""
        <html>
        <head>
          <title>MarkCC's AppEngine Chat Room</title>
        </head>
        <body>
          <h1>Welcome to MarkCC's AppEngine Chat Room</h1>
          <p>(Current time is %s)</p>
        """ % (datetime.datetime.now()))
      # Output the set of chat messages
      messages = db.GqlQuery("SELECT * From ChatMessage ORDER BY timestamp")
      
      for msg in messages:
        self.response.write("<p>%s</p>"%msg)

      self.response.write("""
         <form action="/" method="post">  
         <p><b>Enter new message from:  %s
         <p><b>Message</b></p>
         <div><textarea name="message" rows="5" cols="60"></textarea></div>
         <div><input type="submit" value="Send ChatMessage"/></div>
         </form>
       </body>
       </html>
      """ % user.nickname()) #<callout id="co.no-user"/>
# END: ChatLogin
# START: PostHandler
  def post(self):
    if user is None:
      self.redirect(users.create_login_url(self.request.uri))
    user = users.get_current_user()#<callout id="co.get_user"/>
    msgtext = self.request.get("message")
    if user.nickname() is None or user.nickname() == "":
      nick = "No Nickname"
    else:
      nick = user.nickname()
    msg = ChatMessage(user=user.nickname(), message=msgtext)
    msg.put() 
    sys.stderr.write("****** Just stored message: %s" % msg)
    # Now that we've added the message to the chat, we'll redirect
    # to the root page,
    self.redirect('/')    
# END: PostHandler

# START: Frame
chatapp = webapp2.WSGIApplication([('/', ChatRoomPage)])

# END: Frame
