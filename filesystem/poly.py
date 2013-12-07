
#START: PolyModel
class ResourceAttribute(polymodel.PolyModel):
    name = db.StringProperty(required=True)

class StringAttribute(ResourceAttribute):
    strVal = db.StringProperty(required=True)

class IntegerAttribute(ResourceAttribute):
    intVal = db.IntegerProperty(required=True)
#END: PolyModel    

class ContentModel(db.Model):#<callout id="co.blob-property"/>
    data = db.BlobProperty()

class DirectoryEntry(db.Model):
    name = db.StringProperty()
    resource = db.Reference(db.PersistentResourceModel)

class Resource(db.Model):
    content = db.Reference(ContentModel)#<callout id="co.content-reference"/>
    attributes = db.ListProperty(ResourceAttribute)
    children = Db.ListProperty(DirectoryEntry)

    def getAttribute(self, name):
        for a in attributes:
            if a.name == name:
                return a.value
        return None


class BaseResource(polymodel.PolyModel):
    attributes = db.ListProperty(
