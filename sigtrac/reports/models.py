from django.db import models
from smartmin.models import SmartModel
from sigtrac.carriers.models import Carrier
from sigtrac.devices.models import Device

class Report(models.Model):
    device = models.ForeignKey(Device,
                               help_text="The device that is filing this report")

    carrier = models.ForeignKey(Carrier,
                                help_text="The carrier this device is connected to")

    connection_type = models.CharField(max_length=32,
                                       help_text="The type of the connection")

    download_speed = models.IntegerField(help_text="The download speed reported")
    ping = models.IntegerField(null=True,
                               help_text="The average ping time")
    packets_dropped = models.IntegerField(null=True,
                                          help_text="% of packets dropped")
    signal_strength_dbm = models.IntegerField(null=True,
                                              help_text="The signal strength in decibels")
    signal_strength_asu = models.IntegerField(null=True,
                                              help_text="The signal strength in ASU")

    latitude = models.DecimalField(max_digits=20, decimal_places=16, null=True, blank=True)
    longitude = models.DecimalField(max_digits=20, decimal_places=16, null=True, blank=True)

    created_on = models.DateTimeField(auto_now_add=True,
                                      help_text="When this report was created")