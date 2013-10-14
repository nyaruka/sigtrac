import datetime
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

    created_on = models.DateTimeField(help_text="When this report was created")


    @classmethod
    def create(cls, report_form):
        # look up our carrier, or create it
        carrier = Carrier.objects.filter(key=report_form['carrier'])
        if not carrier:
            report_form['carrier'] = Carrier.objects.create(key=report_form['carrier'],
                                                            name=report_form['carrier'],
                                                            slug=report_form['carrier'])
        else:
            report_form['carrier'] = carrier[0]

        existing_device = Device.objects.filter(uuid=report_form['device'])
        if existing_device:
            report_form['device'] = existing_device[0]
        else:
            report_form['device'] = Device.objects.create(uuid=report_form['device'],
                                                          device_type=report_form['device_type'])

        report_form['created_on'] = datetime.datetime.utcfromtimestamp(report_form['created_on'])

        del report_form['device_type']

        print report_form

        return Report.objects.create(**report_form)
