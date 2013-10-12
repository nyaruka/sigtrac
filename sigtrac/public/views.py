from __future__ import unicode_literals

from smartmin.views import *

class IndexView(SmartTemplateView):
    template_name = 'public/public_index.html'

    def get_context_data(self, **kwargs):
        return dict()

