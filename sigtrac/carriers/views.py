from smartmin.views import *
from .models import Carrier
import json

class CarrierCRUDL(SmartCRUDL):
    model = Carrier
    actions = ('create', 'read', 'update', 'delete', 'list')

class Carriers(View):
    def get(self, request, *args, **kwargs):
        """
        Public JSON view of carriers
        """
        carrier_list = [];
        for carrier in Carrier.objects.all():
            carrier_data = dict()
            carrier_data['name'] = carrier.name
            carrier_data['slug'] = carrier.slug
            carrier_data['color'] = '#' + carrier.color
            carrier_list.append(carrier_data)

        return HttpResponse(json.dumps(carrier_list), content_type="application/json")

