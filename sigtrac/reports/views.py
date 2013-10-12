import json
from django.views.decorators.csrf import csrf_exempt
from smartmin.views import *
from .models import *
from sigtrac.devices.models import DEVICE_CHOICES
from django.utils import timezone
from datetime import timedelta
from django.db.models import Avg, Count
import json

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

class Results(View):
    def get(self, request, *args, **kwargs):
        """
        View to publish data of reports we got
        """
        now = timezone.now()
        last_hour = now - timedelta(hours=1)
        last_day = now - timedelta(hours=24)
        last_month = now - timedelta(days=7)

        carriers = Carrier.objects.all()

        data = []
        for carrier in carriers:
            carrier_data = dict()
            carrier_data['carrier'] = carrier.name
            hour_reports = Report.objects.filter(carrier=carrier, created_on__gte=last_hour).aggregate(ping=Avg('ping'), download_speed=Avg('download_speed'), packets_dropped=Avg('packets_dropped'), report_count=Count('id'))
            carrier_data['hour'] = hour_reports

            day_reports =  Report.objects.filter(carrier=carrier, created_on__gte=last_day).aggregate(ping=Avg('ping'), download_speed=Avg('download_speed'), packets_dropped=Avg('packets_dropped'), report_count=Count('id'))
            carrier_data['day'] = day_reports

            month_reports =  Report.objects.filter(carrier=carrier, created_on__gte=last_month).aggregate(ping=Avg('ping'), download_speed=Avg('download_speed'), packets_dropped=Avg('packets_dropped'), report_count=Count('id'))
            carrier_data['month'] = month_reports

            data.append(carrier_data)

        return HttpResponse(json.dumps(data), content_type="application/json")

