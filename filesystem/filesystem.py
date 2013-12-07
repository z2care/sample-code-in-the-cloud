from datetime import datetime
import string

class Resource(object):
    @staticmethod
    def MakeResource():
        return Resource(content=None, attributes={})

    def fs_put(self, content):
        self.content = content

    def fs_get(self):
        return self.content

    def fs_setAttribute(self, name, value):
        self.attributes[name] = value

    def fs_getAttribute(self, name):
        return self.attributes[name]

    def isDir(self):
        return self.getAttribute("children") is not None

    def addChild(self, name, resource):
        if self.getAttribute("children") is None:
            self.setAttribute("children", {})
        self.getAttribute("children")[name] = resource

class FileSystem(object):
    @staticmethod
    def MakeFilesystem():
        fs = FileSystem()
        fs.root = FileSystem.MakeFile("/", "root", "")
        return self
    
    @staticmethod
    def MakeFile(name, owner, content):
        file = Resource()
        file.put(content)
        file.setAttribute("owner", owner)
        file.setAttribute("time", datetime.now())
        return file
    
    def getRoot(self):
        return self.root

    def getResourceFromChild(self, child, nameElements):
        """A recursive helper for retrieving a file by path. child is the name
        of a directory which is transitively contained in the target of this call.
        nameElements is a list of path components for the pathname that come after
        the child. Each recursive call resolves one element of the path, then calls 
        itself on the remainder. When nameElements is empty we've found the file."""
        if nameElements is []:
            return child
        childsChildren = child.getAttribute("children")
        if childsChildren is None:
            return None
        else:
            nextChild = childsChildren[nameElements[0]]
            if nextChild is None:
                return None
            else:
                return self.getResourceFromChild(nextChild, nameElements[1:])
                    
    def getResourceAtPath(self, path):
        pathElements = string.split(path, "/")
        self.getResourceFromChild(self.getRoot(), pathElements)
