import webapp2 #<callout id="co.import"/>
import datetime

class WelcomePage(webapp2.RequestHandler):#<callout id="co.class"/>
    def get(self):#<callout id="co.get"/>
        self.response.headers["Content-Type"] = "text/html"#<callout id="co.header"/>
        self.response.write(#<callout id="co.output"/>
          """<html>
               <head>
                 <title>Welcome to MarkCC's chat service</title>
               </head>
               <body>
                 <h1>Welcome to MarkCC's chat service</h1>
                 <p> The current time is: %s</p>
               </body>
             </html>
          """ % (datetime.datetime.now()))
        

chatapp = webapp2.WSGIApplication([('/', WelcomePage)]) #<callout id="co.app"/>



