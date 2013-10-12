from django.db import models
from smartmin.models import SmartModel

class Carrier(SmartModel):
    key = models.CharField(max_length=32, unique=True,
                           help_text="The key for this carrier as passed down by Android")
    name = models.CharField(max_length=32, unique=True,
                            help_text="The friendly name for this carrier")
    slug = models.CharField(max_length=32, unique=True,
                            help_text="The slug used in the UI for this carrier")

    def __unicode__(self):
        return self.name