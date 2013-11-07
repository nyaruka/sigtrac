from __future__ import unicode_literals

from smartmin.views import *
from sigtrac.carriers.models import Carrier
from sigtrac.reports.models import Report
from django.db.models import Avg
from django.utils import timezone
from django.shortcuts import get_object_or_404
from datetime import timedelta
import json

class IndexView(SmartTemplateView):
    template_name = 'public/public_index.html'

    def get_context_data(self, **kwargs):

        carriers = Carrier.objects.all()

        data = []
        for carrier in carriers:
            carrier_data = dict()
            carrier_data['carrier'] = carrier.slug
            carrier_data['color'] = "#" + carrier.color


            end = timezone.now() + timedelta(hours=1)
            end = end.replace(minute=0, second=0, microsecond=0)
            start = end - timedelta(hours=120)

            series = []

            while start <= end:
                reports = Report.objects.filter(carrier=carrier, download_speed__gte=0, created_on__range=[start, start+timedelta(hours=1)]).order_by('created_on').aggregate(download_speed=Avg('download_speed'))
                if reports['download_speed'] is not None:
                    series.append([start.strftime('%Y-%m-%dT%H:%M:%S.%f-0200'),  reports['download_speed']])

                start += timedelta(hours=1)

            carrier_data['series'] = series
            data.append(carrier_data)

        return dict(time_data=data)

class Series(View):
    def get(self, request, *args, **kwargs):
        """
        Public view, 1mo time series, hourly ave bandwidth per carrier
        """
        time_series = {}
        # d is the length of the x axis, interval is the distance between points
        deltas = [{'name': 'hour', 'd': timedelta(hours=1), 'interval': timedelta(minutes=5)},
                  {'name': 'day', 'd': timedelta(days=1), 'interval': timedelta(hours=1)},
                  {'name': 'week', 'd': timedelta(days=7), 'interval': timedelta(hours=3)}]
        for delta in deltas:
            time_series[delta['name']] = {}
            for carrier in Carrier.objects.all():
                time_series[delta['name']][carrier.slug] = self.get_time_series(carrier, delta)

        return HttpResponse(json.dumps(time_series), content_type="application/json")

    def get_time_series(self, carrier, delta):
        end = timezone.now() + timedelta(hours=1)
        end = end.replace(minute=0, second=0, microsecond=0)
        start = end - delta['d']
        series = []

        while start <= end:
            reports = carrier.report_set.filter(carrier=carrier, download_speed__gte=0, created_on__range=[start, start+delta['interval']]).order_by('created_on').aggregate(download_speed=Avg('download_speed'))
            if reports['download_speed'] is not None:
                series.append([start.strftime('%Y-%m-%dT%H:%M:%S.%f-0200'), reports['download_speed']])
            start += delta['interval']

        return series
