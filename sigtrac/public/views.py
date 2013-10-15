from __future__ import unicode_literals

from smartmin.views import *
from sigtrac.carriers.models import Carrier
from sigtrac.reports.models import Report
from django.db.models import Avg
from django.utils import timezone
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
            start = end - timedelta(hours=24)

            series = []

            while start <= end:
                reports = Report.objects.filter(carrier=carrier, created_on__range=[start, start+timedelta(hours=1)]).order_by('created_on').aggregate(download_speed=Avg('download_speed'))
                if reports['download_speed'] is not None:
                    series.append([start.strftime('%Y-%m-%dT%H:%M:%S.%f-0200'),  reports['download_speed']])

                start += timedelta(hours=1)

            carrier_data['series'] = series

            data.append(carrier_data)

        return dict(time_data=data)

