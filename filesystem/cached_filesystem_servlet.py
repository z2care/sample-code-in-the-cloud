from google.appengine.api import memcache
from google.appengine.ext import db

class ResourceAttribute(db.Model):
    name = db.StringProperty(required=True)
    value = db.TextProperty(required=True)

class ContentModel(db.Model):
    data = db.BlobProperty()

class DirectoryEntry(db.Model):
    name = db.StringProperty()
    resource = db.Reference(db.PersistentResourceModel)

class Resource(db.Model):
    content = db.Reference(ContentModel)
    attributes = db.ListProperty(ResourceAttribute)
    children = db.ListProperty(DirectoryEntry)

    def getAttribute(self, name):
        for a in attributes:
            if a.name == name:
                return a.value
        return None
#START: SetAttribute
    def setAttribute(self, name, value):
        for a in attributes:
            if a.name == name:
                a.value = value
                return
        a = ResourceAttribute(parent=self, name=name, value=value)
#END: SetAttribute

        

#START: FullFilesystem
class Filesystem(db.Model):
    root = db.Reference(Resource)

    def getRoot(self):
        return self.root

    def getResource(self, path):
        path_elements = path.split("/")
        return self.getResourceFromChildByList(self.root, path_elements)

    def getResourceFromChildByList(self, resource, path_elements):
        if path_elements is []:
            return resource
        else:
            for direntry in resource.children:
                if direntry.name == path_elements[0]:
                    return getResourceFromChildByList(direntry.resource, path_elements[1:])
            return None
#END: RealFilesystem

#START: GetHandler
def FilesystemResourceHandler(webapp.RequestHandler):        
    def get(self):
#START: CachePattern        
        root = memcache.get(key="root")        
        if root is None:
            query = Filesystem.gql("")
            filesystem = query.get()
            root = filesystem.getRoot()
            memcache.put("root", root)
#END: CachePattern            
        url = self.request.path
        urlElements = url.split("/") 
        # And then check if it's a content request, or an attribute request.
        # if the first char of the last name element is "~", then it's an attribute.
        resourcePath = None
        attr = None
        if urlElements[-1][0] == "~": 
            attr = urlElements[-1]
            resourcePath = urlElements[0:-1]
        else:
            resourcePath = urlElements
        resource = filesystem.getResourceFromChildByList(root, resourcePath)
        if resource is None: 
            self.response.set_status(404, "Resource not found")
            return
        if attr is not None: 
            result = resource.getAttribute(name)
            if result is None:
                self.response.set_status(404, "Resource attribute not found")
            self.response.out.write(str(result))
            return
        else:
            self.response.out.write(resource.content.data)  
#END: GetHandler
#START: PutHandler            
    def put(self):
        filesystem = getFilesystem() 
        root = filesystem.root 
        url = self.request.path
        urlElements = url.split("/") 
        resourcePath = None
        attr = None
        if urlElements[-1][0] == "~":
            attr = urlElements[-1]
            resourcePath = urlElements[0:-1]
        else:
            resourcePath = urlElements
        resource = filesystem.getResourceFromChildByList(root, resourcePath)
        if resource is None: 
            parent = filesystem.getResourceFromChildBylist(root, resourcePath[0:-1])
            name = resourcePath[-1]
            if parent is None:
                self.response.set_status(404, "Parent directory of new resource not found")
            else:
                resource = Resource(content = self.request.body, attributes=[], children=[])
                resource.put()
                dirEntry = DirectoryEntry(name=name,resource=resource)
                parent.children.append(dirEntry)
                parent.put()
                self.response.set_status(100, "Resource created")
                return
        else:
            resource.content=self.request.body
            resource.put()
            self.response.set_status(100, "Resource updated")
#END: PutHandler
