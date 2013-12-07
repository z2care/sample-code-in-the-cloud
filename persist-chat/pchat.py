import webapp2
import datetime

from google.appengine.ext import db

#START: ChatMessage
class ChatMessage(db.Model):
    user = db.StringProperty(required=True)
    timestamp = db.DateTimeProperty(auto_now_add=True)
    message = db.TextProperty(required=True)

    def __str__(self):
        return "%s (%s): %s" % (self.user, self.timestamp, self.message)
#END: ChatMessage


class ChatRoomPage(webapp2.RequestHandler):
    def get(self):
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
#START: MessageLoop
        # Output the set of chat messages
        messages = db.GqlQuery("SELECT * From ChatMessage ORDER BY time")
        for msg in messages:
            self.response.write("<p>%s</p>" % msg)
#END: MessageLoop
        self.response.write("""
           <form action="/talk" method="post"> 
          <div><b>Name:</b> 
           <textarea name="name" rows="1" cols="20"></textarea></div>
           <p><b>Message</b></p>
           <div><textarea name="message" rows="5" cols="60"></textarea></div>
           <div><input type="submit" value="Send ChatMessage"/></div>
           </form>
         </body>
       </html>
       """)
# END: MainPage    

class TmpChatRoomCountViewPage(webapp2.RequestHandler):
    def get(self):
        self.response.headers["Content-Type"] = "text/html"
        self.response.write("""
           <html>
             <head>
               <title>MarkCC's AppEngine Chat Room (last 20)</title>
             </head>
             <body>
               <h1>Welcome to MarkCC's AppEngine Chat Room</h1>
               <p>(Current time is %s; viewing the last 20 messages.)</p>
           """ % (datetime.datetime.now()))
#START: MessageLoopLimited
        # Output the set of chat messages
        messages = db.GqlQuery("SELECT * From ChatMessage ORDER BY " +
                               "timestamp DESC LIMIT 20").fetch()
        for msg in reversed(messages):
            self.response.write("<p>%s</p>" % msg)
#END: MessageLoopLimited
        self.response.write("""
           <form action="/talk" method="post"> 
          <div><b>Name:</b> 
           <textarea name="name" rows="1" cols="20"></textarea></div>
           <p><b>Message</b></p>
           <div><textarea name="message" rows="5" cols="60"></textarea></div>
           <div><input type="submit" value="Send ChatMessage"/></div>
           </form>
         </body>
       </html>
       """)

#START: CountPage
class ChatRoomCountViewPage(webapp2.RequestHandler):
    def get(self):
        self.response.headers["Content-Type"] = "text/html"
        self.response.write("""
           <html>
             <head>
               <title>MarkCC's AppEngine Chat Room (last 20)</title>
             </head>
             <body>
               <h1>Welcome to MarkCC's AppEngine Chat Room</h1>
               <p>(Current time is %s; viewing the last 20 messages.)</p>
           """ % (datetime.datetime.now()))
        # Output the set of chat messages
        messages = db.GqlQuery("SELECT * From ChatMessage " +#<callout id="co.limit-query"/>
				"ORDER BY timestamp DESC LIMIT 20").fetch(20)
        messages.reverse()#<callout id="co.limit-reverse"/>
        for msg in list(messages):
            self.response.write("<p>%s</p>" % msg)
        self.response.write("""
           <form action="/talk" method="post"> 
          <div><b>Name:</b> 
           <textarea name="name" rows="1" cols="20"></textarea></div>
           <p><b>Message</b></p>
           <div><textarea name="message" rows="5" cols="60"></textarea></div>
           <div><input type="submit" value="Send ChatMessage"/></div>
           </form>
         </body>
       </html>
       """)
#END: CountPage

class ChatRoomTimeViewPage(webapp2.RequestHandler):
    def get(self):
        self.response.headers["Content-Type"] = "text/html"
        self.response.write("""
           <html>
           <head><title>MarkCC's AppEngine Chat Room (last 20)</title>
           </head>
           <body><h1>Welcome to MarkCC's AppEngine Chat Room</h1>
           <p>(Current time is %s; viewing the last 20 messages.)</p>
           """ % (datetime.datetime.now()))
        # Output the set of chat messages
        messages = ChatMessage.gql("WHERE timestamp > :fiveago ORDER BY timestamp", 
                                  fiveago=datetime.datetime.now() - datetime.timedelta(minutes=5))
        for msg in messages:
            self.response.write("<p>%s</p>" % msg)
        self.response.write("""
          <form action="/talk" method="post"> 
          <div><b>Name:</b> <textarea name="name" rows="1" cols="20">
          </textarea></div>
          <p><b>Message</b></p>
          <div><textarea name="message" rows="5" cols="60"></textarea></div>
          <div><input type="submit" value="Send ChatMessage"/></div>
          </form>
         </body>
       </html>""")




# START: PostHandler
class ChatRoomPoster(webapp2.RequestHandler):
    def post(self):
        chatter = self.request.get("name")
        msgtext = self.request.get("message")
        msg = ChatMessage(user=chatter, message=msgtext)
        msg.put() #<callout id="co.put"/>
        # Now that we've added the message to the chat, we'll redirect
        # to the root page,
        self.redirect('/')        
# END: PostHandler




# START: Frame
chatapp = webapp2.WSGIApplication([('/', ChatRoomPage),
                                  ('/talk', ChatRoomPoster),
                                  ('/limited/count', ChatRoomCountViewPage),
                                  ('/limited/time', ChatRoomTimeViewPage)])
# END: Frame
