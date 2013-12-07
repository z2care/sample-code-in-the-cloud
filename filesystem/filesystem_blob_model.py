
#START: SeparateContentModel
class ContentModel(db.Model):#<callout id="co.blob-property"/>
    data = db.BlobProperty()

class DirectoryEntry(db.Model):
    name = db.StringProperty()
    resource = db.ReferenceProperty(PersistentResourceModel)

class Resource(db.Model):
    content = db.Reference(ContentModel)#<callout id="co.content-reference"/>
    attributes = db.ListProperty(db.Key)
    children = db.ListProperty(DirectoryEntry)
#END: SeparateContentModel    
