import email
import webapp2 
from google.appengine.ext.webapp.mail_handlers import InboundMailHandler 

class ChatMailHandler(InboundMailHandler):
    def receive(self, mail_message):
        mail.send_mail(sender="admin@markcc-chatroom-one.appspot.com",
                       to="markcc@gmail.com",
                       subject="CHAT ADMIN MAIL: %s" % mail_message.subject,
                       body="Original message from: %s\n%s" %
                       (mail_message.sender,
                        mail_message.body)

chatmail = webapp2.WSGIApplication([InboundMailHandler.mapping()])



