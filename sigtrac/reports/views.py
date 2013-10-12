from smartmin.views import *
from .models import *

class ReportCRUDL(SmartCRUDL):
    model = Report
    actions = ('create', 'read', 'update', 'delete', 'list')
