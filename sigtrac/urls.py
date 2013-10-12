from django.conf.urls import patterns, include, url

urlpatterns = patterns('',
    url(r'^', include('sigtrac.public.urls')),
    url(r'^', include('sigtrac.carriers.urls')),
    url(r'^', include('sigtrac.devices.urls')),
    url(r'^', include('sigtrac.reports.urls')),
    url(r'^users/', include('smartmin.users.urls')),
)
