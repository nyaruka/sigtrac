from .views import CarrierCRUDL, Carriers
from django.conf.urls import patterns, url, include

urlpatterns = CarrierCRUDL().as_urlpatterns()

urlpatterns += patterns('carriers.views',
                        url(r'^carriers', Carriers.as_view(), name='carriers.carriers'))
