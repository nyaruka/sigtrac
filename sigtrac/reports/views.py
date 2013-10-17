import base64
import hashlib
import hmac
import json
import re
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

    class List(SmartListView):
        fields = ('device', 'carrier', 'connection_type', 'download_speed', 'ping', 'packets_dropped', 'created_on')
        default_order = ('-created_on', )
        search_fields = ('carrier__name__icontains', 'connection_type__icontains')

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
    latitude = forms.DecimalField(required=False)
    longitude = forms.DecimalField(required=False)
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

        if 'connection_type' in request.POST:
            form = ReportForm(request.POST)
        else:
            # use a query string instead since order is important for signing
            form = ReportForm(request.GET)

        # for now, we'll accept anything under version 4
        # but this will soon be enforced once our clients upgrade
        if 'version' in request.GET and int(request.GET['version']) > 4:
            sig = request.GET['s']
            qs = request.META['QUERY_STRING']
            time = request.GET['created_on']

            # trim off the signature
            qs = re.sub('&s=.*$', '', qs)

            # sign the request
            signature = hmac.new(key=str(settings.BITRANKS_SECRET + time), msg=bytes(qs), digestmod=hashlib.sha256).digest()

            # base64 and url sanitize
            signature = base64.urlsafe_b64encode(signature).strip()

            if signature != sig:
                return HttpResponse("Invalid signature: " + sig)

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
        last_week = now - timedelta(days=7)

        data = {}
        period_data = dict()
        for carrier in Carrier.objects.all():
            reports = Report.objects.filter(carrier=carrier, created_on__gte=last_hour).aggregate(ping=Avg('ping'),
                                            download_speed=Avg('download_speed'), packets_dropped=Avg('packets_dropped'),
                                            report_count=Count('id'), device_count=Count('device__id', distinct=True))
            period_data[carrier.slug] = reports

        data['hour'] = period_data

        period_data = dict()
        for carrier in Carrier.objects.all():
            reports = Report.objects.filter(carrier=carrier, created_on__gte=last_day).aggregate(ping=Avg('ping'),
                                            download_speed=Avg('download_speed'), packets_dropped=Avg('packets_dropped'),
                                            report_count=Count('id'), device_count=Count('device__id', distinct=True))
            period_data[carrier.slug] = reports

        data['day'] = period_data

        period_data = dict()
        for carrier in Carrier.objects.all():
            reports = Report.objects.filter(carrier=carrier, created_on__gte=last_week).aggregate(ping=Avg('ping'),
                                            download_speed=Avg('download_speed'), packets_dropped=Avg('packets_dropped'),
                                            report_count=Count('id'), device_count=Count('device__id', distinct=True))
            period_data[carrier.slug] = reports

        data['week'] = period_data
        data['current'] = data['day']

        return HttpResponse(json.dumps([data]), content_type="application/json")

