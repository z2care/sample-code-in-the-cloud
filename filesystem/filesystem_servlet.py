from google.appengine.ext import webapp
from google.appengine.ext.webapp.util import run_wsgi_app
import datetime

from google.appengine.ext import db

class ResourceAttribute(db.Model):
    name = db.StringProperty(required=True)
    value = db.TextProperty(required=True)

class ResourceContent(db.Model):
    data = db.BlobProperty()

class DirectoryEntry(db.Model):
    name = db.StringProperty()
    resource = db.Reference(db.PersistentResourceModel)

class Resource(db.Model):
    content = db.Reference(ContentModel)
    attributes = db.ListProperty(db.Key)
    children = Db.ListProperty(db.Key)

    @staticmethod
    def MakeResource(creator):
        resource = PersistentResource()
        resource.content = ""
        attribute = ResourceAttribute(name="creator", value=creator)
        attribute.put()
        resource.attributes.append(attribute.key())
        resource.put()

    def GetAttribute(self, name):
      if name == "children":
        return [ DirectoryEntry.get(key) for key in self.children ]
      else:
        for attr_key in self.attributes:
          attr = ResourceAttribute.get(attr_key)
          if attr.name == name:
            return attr.value
        return None

#START: SetAttribute
    def SetAttribute(self, name, value):
      if name == "children":
        self.children = [ de.key() for de in value ]
        self.put()
      else:
        for attr_key in self.attributes:
          attr = ResourceAttributeModel.get(attr_key) 
          if attr.name == name:
            attr.value = value
            attr.put()
            return
          newAttr = ResourceAttribute(parent=self, name=name, value=value)
          newAttr.put()
          self.attributes.append(newAttr.key())
          self.put()
#END: SetAttribute
    def IsDir(self):
        return (self.children is not [])


def GenerateUploadForm(resourceid, out):
    upload_url = blobstore.create_upload_url('/file_upload/%s')
    out.write("<html><body>\n")
    out.write("""<form action="%s" method="POST"
              enctype="multipart/form-data">""" %
              upload_url)
    out.write('Upload File: <input type="file" name="contents"><br>\n')
    out.write('<input type="submit" name="submit" value="Submit"> </form></body></html>')


#START: RealFilesystem
class Filesystem(db.Model):
    root = db.Reference(Resource)

    def GetRoot(self):
        return self.root

    def GetResource(self, path):
        path_elements = path.split("/")
        return self.getResourceFromChildByList(self.root, path_elements)

    def GetResourceFromChildByList(self, resource, path_elements):
        if path_elements is []:
            return resource
        else:
            for direntry in resource.children:
                if direntry.name == path_elements[0]:
                    return getResourceFromChildByList(direntry.resource, 
                                                      path_elements[1:])
            return None
#END: RealFilesystem

#START: GetHandler
class FilesystemResourceHandler(webapp.RequestHandler):
    def GetFilesystem(self):
        query = Filesystem.gql("")
        return query.get();
        
    def get(self):
        filesystem = self.GetFilesystem() #<callout id="co.get_filesystem"/>
        root = filesystem.root #<callout id="co.get_root"/>
        url = self.request.path#<callout id="co.get_path"/>
        urlElements = url.split("/") 
        # And then check if it's a content request or an attribute request.
        # if the first char of the last name element is "~", then it's 
        # an attribute.
        resourcePath = None
        attr = None
        if urlElements[-1].startswith("~"): #<callout id="co.parse_attr"/>
            attr = urlElements[-1]
            resourcePath = urlElements[:-1]
        else:
            resourcePath = urlElements
        resource = filesystem.getResourceFromChildByList(root, resourcePath) #<callout id="co.get_resource"/>
        if resource is None: #<callout id="co.not-found"/>
            self.response.error(404)
            return
        if attr is not None: #<callout id="co.get-attr"/>
            result = resource.getAttribute(name)
            if result is None:
                self.response.error(404)
            self.response.out.write(str(result))
            return
        else:
            self.response.out.write(resource.content.data)  #<callout id="co.get-content"/>
#END: GetHandler
#START: PutHandler            
    def put(self):
        filesystem = GetFilesystem() 
        root = filesystem.root 
        url = self.request.path
        urlElements = url.split("/") 
        resourcePath = None
        attr = None
        if urlElements[-1].startswith("~"):
            attr = urlElements[-1]
            resourcePath = urlElements[:-1]
        else:
            resourcePath = urlElements
        resource = filesystem.getResourceFromChildByList(root, resourcePath)
        if resource is None: #<callout id="co.put-not-found"/>
            parent = filesystem.getResourceFromChildBylist(root,
                                                           resourcePath[0:-1])
            name = resourcePath[-1]
            if parent is None:
                self.response.set_status(404,
                                         "Parent dir of new resource not found")
            else:
                resource = Resource(content = self.request.body, attributes=[],
                                    children=[])#<callout id="co.new-resource"/>
                resource.put()
                dirEntry = DirectoryEntry(name=name,resource=resource)#<callout id="co.new-direntry"/>
                parent.children.append(dirEntry)
                parent.put()
                self.response.set_status(100, "Resource created")
                return
        else:
            resource.content=self.request.body#<callout id="co.update-resource"/>
            resource.put()
            self.response.set_status(100, "Resource updated")
#END: PutHandler
