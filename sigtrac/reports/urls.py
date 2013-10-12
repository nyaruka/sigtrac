from .views import ReportCRUDL, SubmitHandler
from django.conf.urls import patterns, url, include

urlpatterns = ReportCRUDL().as_urlpatterns()

urlpatterns += patterns('reports.views',
                       url(r'^submit', SubmitHandler.as_view(), name='reports.submit'))