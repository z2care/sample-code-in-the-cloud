from datetime import datetime
from google.appengine.ext import db
import string

class ResourceAttribute(db.Model):
    name = db.StringProperty(required=True)
    value = db.TextProperty(required=True)

#START: DirectoryEntry
class DirectoryEntry(db.Model):#<callout id="co.dir_entry_class"/>
    name = db.StringProperty()
    resource = db.ReferenceProperty(PersistentResourceModel)#<callout id="co.resource-reference"/>
#END: DirectoryEntry

#START: ResourceDefinition
class PersistentResource(db.Model):
    content = db.BlobProperty(default = "")
    attributes = db.ListProperty(db.Key)
    children = db.ListProperty(db.Key)#<callout id="co.children-property"/>
#END: ResourceDefinition
    @staticmethod
    def MakeResource(creator):
        resource = PersistentResource()
        resource.content = ""
        attribute = ResourceAttribute(name="creator", value=creator)
        attribute.put()
        resource.attributes.append(attribute.key())
        resource.put()
#START: GetAndSetAttribute
  def GetAttribute(self, name):
    if name == "children": #<callout id="co.get_children_attr"/>
      return [ DirectoryEntry.get(key) for key in self.children ]
    else:
      for attr_key in self.attributes:
        attr = ResourceAttribute.get(attr_key)
        if attr.name == name:
          return attr.value
      return None

  def SetAttribute(self, name, value):
    if name == "children":
      self.children = [ de.key() for de in value ] #<callout id="co.set_children_attr"/>
      self.put()
    else:
      for attr_key in self.attributes:
        attr = ResourceAttributeModel.get(attr_key) 
        if attr.name == name: 
          attr.value = value
          attr.put()
          return
        newAttr = ResourceAttribute(name=name, value=value)
        newAttr.put()
        self.attributes.append(newAttr.key())
        self.put()
  def IsDir(self):
    return (self.children is not [])
#END: GetAndSetAttribute


#START: filesystem
class FileSystem(object):
  def __init__(self):
    self.root = MakeFile("/", "root", "")
    return self
  def getRoot(self):
    return self.root

  def getResourceFromChild(self, child, nameElements):
    if nameElements is []:
      return child
    childsChildren = child.getProperty("children")
    if childsChildren is None:
      return None
    else:
      nextChild = childsChildren[nameElements[0]]
      if nextChild is None:
        return None
      else:
        return getResourceFromChild(nextChild,
                                    nameElements[1:])
          
  def getResourceAtPath(self, path):
    pathElements = string.split(path, "/")
#END: filesystem
    

#START: RenderChildren
def RenderChildren(dir):
  children = dir.GetAttribute(children)
  for c in children:
    # c is a DirectoryEntry
    if c.resource.IsDir(): #<callout id="co.through-reference"/>
       print("Child %s is a directory" % c.name)
     else:
   print("Child %s is not a directory" % c.name)
#END: RenderChildren
