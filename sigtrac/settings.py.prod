from settings_common import *

DATABASES = {
    'default': {
        'ENGINE': 'django.db.backends.mysql',
        'NAME': '${db}',
        'USER': '${db_user}',
        'PASSWORD': '${db_password}',
        'HOST': '${db_host}',
        'PORT': '',                      
        'OPTIONS': {   "init_command": "SET storage_engine=INNODB",
                       "charset": "utf8", 
        }
    }
}

DEBUG = True
TEMPLATE_DEBUG = True
COMPRESS_ENABLED = True

MANAGERS = ADMINS
INSTALLED_APPS = INSTALLED_APPS + ('django_nose', 'debug_toolbar')

# static dir is different for prod
# STATIC_URL = '/sitestatic/'

INTERNAL_IPS = ('127.0.0.1',)

CELERY_ALWAYS_EAGER = True
BROKER_BACKEND = 'memory'

