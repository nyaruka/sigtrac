from django.db import models
from smartmin.models import SmartModel

class Carrier(SmartModel):
    name = models.CharField(max_length=32)

    def __unicode__(self):
        return self.name