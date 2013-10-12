from django.db import models
from smartmin.models import SmartModel

DEVICE_CHOICES = (('WEB', "Web Browser"),
                  ('AND', "Android Phone"),
                  ('IOS', "iPhone"))

class Device(models.Model):
    device_type = models.CharField(max_length=16, choices=DEVICE_CHOICES,
                                   help_text="What kind of device this is")

    uuid = models.CharField(max_length=40,
                            help_text="The unique id for this device")
    created_on = models.DateTimeField(auto_now_add=True,
                                      help_text="When this report was created")


    def __unicode__(self):
        return self.uuid