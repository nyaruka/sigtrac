from smartmin.views import *
from .models import Carrier

class CarrierCRUDL(SmartCRUDL):
    model = Carrier
    actions = ('create', 'read', 'update', 'delete', 'list')

