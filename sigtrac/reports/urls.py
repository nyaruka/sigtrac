from .views import ReportCRUDL, SubmitHandler, Results
from django.conf.urls import patterns, url, include

urlpatterns = ReportCRUDL().as_urlpatterns()

urlpatterns += patterns('reports.views',
                        url(r'^results', Results.as_view(), name='reports.results'),
                        url(r'^submit', SubmitHandler.as_view(), name='reports.submit'))
