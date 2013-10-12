from smartmin.views import *
from .models import *

class DeviceCRUDL(SmartCRUDL):
    model = Device
    actions = ('create', 'read', 'update', 'delete', 'list')
