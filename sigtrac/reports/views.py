import json
from django.views.decorators.csrf import csrf_exempt
from smartmin.views import *
from .models import *
from sigtrac.devices.models import DEVICE_CHOICES

class ReportCRUDL(SmartCRUDL):
    model = Report
    actions = ('create', 'read', 'update', 'delete', 'list')

class ReportForm(forms.Form):
    device = forms.CharField(max_length=40)
    device_type = forms.CharField(max_length=3)
    carrier = forms.CharField(max_length=20)
    connection_type = forms.CharField(max_length=32)
    download_speed = forms.IntegerField()
    ping = forms.IntegerField()
    packets_dropped = forms.IntegerField()
    signal_strength_dbm = forms.IntegerField()
    signal_strength_asu = forms.IntegerField()
    latitude = forms.DecimalField()
    longitude = forms.DecimalField()
    created_on = forms.IntegerField()


class SubmitHandler(View):
    @csrf_exempt
    def dispatch(self, *args, **kwargs):
        return super(SubmitHandler, self).dispatch(*args, **kwargs)

    def get(self, request, *args, **kwargs):
        return HttpResponse("ILLEGAL METHOD", status=400)

    def post(self, request, *args, **kwargs):
        """
        Endpoint to submit a report via AJAX, format should be a POST in the following format.

          device: 'ASDFASDFASDGASDG-ASDGASDGASD-ASDGASDGASFG',
          device_type: 'AND',
          carrier: 'MTN',
          connection_type: "HSPDA",
          download_speed: 12415,
          ping: 150,
          packets_dropped: 0,
          signal_strength_dbm: -15,
          signal_strength_asu: -15,
          latitude: 1.1515,
          longitude: 1.1515,
          created_on: 15815881585
        """
        form = ReportForm(request.POST)
        if not form.is_valid():
            return HttpResponse("Invalid report: %s" % ",".join([str(e) for e in form.errors]))

        Report.create(form.cleaned_data)

        return HttpResponse("New Report Created")