# -*- coding: utf-8 -*-
import datetime
from south.db import db
from south.v2 import SchemaMigration
from django.db import models


class Migration(SchemaMigration):

    def forwards(self, orm):
        # Adding model 'Device'
        db.create_table(u'devices_device', (
            (u'id', self.gf('django.db.models.fields.AutoField')(primary_key=True)),
            ('device_type', self.gf('django.db.models.fields.CharField')(max_length=16)),
            ('uuid', self.gf('django.db.models.fields.CharField')(max_length=40)),
            ('created_on', self.gf('django.db.models.fields.DateTimeField')(auto_now_add=True, blank=True)),
        ))
        db.send_create_signal(u'devices', ['Device'])


    def backwards(self, orm):
        # Deleting model 'Device'
        db.delete_table(u'devices_device')


    models = {
        u'devices.device': {
            'Meta': {'object_name': 'Device'},
            'created_on': ('django.db.models.fields.DateTimeField', [], {'auto_now_add': 'True', 'blank': 'True'}),
            'device_type': ('django.db.models.fields.CharField', [], {'max_length': '16'}),
            u'id': ('django.db.models.fields.AutoField', [], {'primary_key': 'True'}),
            'uuid': ('django.db.models.fields.CharField', [], {'max_length': '40'})
        }
    }

    complete_apps = ['devices']