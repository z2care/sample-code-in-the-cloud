from datetime import datetime
from google.appenging.ext import db
import string

#START: AttributeModel
class ResourceAttribute(db.Model):
    name = db.StringProperty(required=True)
    value = db.TextProperty(required=True)
#END: AttributeModel

#START: ResourceModel
class PersistentResource(db.Model):
    content = db.BlobProperty(default = "")
    attributes = db.ListProperty(item_type=db.Key)#<callout id="co.list_prop"/>

    @staticmethod
    def MakeResource(creator): #<callout id="co.create_resource"/>
        resource = PersistentResource()
        resource.content = ""
        attribute = ResourceAttribute(name="creator", value=creator)
        attribute.put()#<callout id="co.put-attribute"/>
        resource.attributes.append(attribute.key())#<callout id="co.append-key"/>
        resource.put()
#END: ResourceModel
#START: GetAndPutContent
    def GetContent(self):
        return self.content

    def PutContent(self, content):
        self.content = content
        self.put()
#END: GetAndPutContent
#START: GetAttribute
    def GetAttribute(self, name): 
        for attr_key in self.attributes: #<callout id="co.iterate_key"/>
            attr = ResourceAttribute.get(attr_key)#<callout id="co.getbykey"/>
            if attr.name == name:
                return attr.value
        return None
#END: GetAttribute

#START: SetAttribute
    def SetAttribute(self, name, value):
        for attr_key in self.attributes: #<callout id="co.put-iter"/>
            attr = ResourceAttributeModel.get(attr_key) 
            if attr.name == name: #<callout id="co.update-attribute"/>
                attr.value = value
                attr.put()
                return
        newAttr = ResourceAttribute(name=name, value=value)#<callout id="co.create-resource-attr"/>
        newAttr.put()
        self.attributes.append(newAttr.key())
        self.put()
#END: SetAttribute
#START: IsDir
    def IsDir(self):
        return self.GetAttribute("children") is not None
#END: IsDir
#START: NaiveAddFileToDir
    def NaiveAddChildToDirectory(self, name, resource):
        children = self.getAttribute("children")
        if children is not None:
            children.value.append(resource) # WRONG!
#END: NaiveAddFileToDir
