from settings_common import *

DEBUG = False
TEMPLATE_DEBUG = DEBUG
COMPRESS_ENABLED = True

HOSTNAME = 'sigtrac.nyaruka.com'

DATABASES = {
    'default': {
        'ENGINE': 'django.db.backends.mysql',
        'NAME': 'sigtrac',
        'USER': 'sigtrac',
        'PASSWORD': 'nyaruka',
        'HOST': 'nyaruka.cqqi0i4pkloc.us-east-1.rds.amazonaws.com',
        'PORT': '',                      
        'OPTIONS': {   "init_command": "SET storage_engine=INNODB",
                       "charset": "utf8", 
        }
    }
}

DEBUG = True
TEMPLATE_DEBUG = True
COMPRESS_ENABLED = False

MANAGERS = ADMINS
INSTALLED_APPS = INSTALLED_APPS + ('gunicorn',)

# static dir is different for prod
STATIC_URL = '/sitestatic/'

INTERNAL_IPS = ('127.0.0.1',)

CELERY_ALWAYS_EAGER = True
BROKER_BACKEND = 'memory'

