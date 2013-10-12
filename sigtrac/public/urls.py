from .views import *

urlpatterns = patterns('',
                       (r'^$', IndexView.as_view(), {}, 'public.public_index'),)
