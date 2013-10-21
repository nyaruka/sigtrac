from .views import *

urlpatterns = patterns('',
                       (r'^$', IndexView.as_view(), {}, 'public.public_index'),
                       url(r'^series/$', Series.as_view(), name='public.series'))
