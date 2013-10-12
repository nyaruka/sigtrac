# import our default settings
from settings_common import *

DEBUG = False
TEMPLATE_DEBUG = DEBUG
COMPRESS_ENABLED = True
HOSTNAME = 'textit.in'

# these guys will get email from sentry
ADMINS = (
   ('Nyaruka Ops', 'ops@nyaruka.com'),
)

# Prod username / password
DEFAULT_FROM_EMAIL = 'TextIt <website@textit.in>'
EMAIL_HOST_USER = 'AKIAIZHUHM76XIUM4NSQ'
EMAIL_HOST_PASSWORD = 'AjphZ8DKRTWwQFld8ci76bCEcW93uXcnhkxmTBxwYune'

# our prod server uses a standalone redis instance
REDIS_HOST='10.122.249.28'
BROKER_HOST='10.122.249.28'

# use redis for our cache
CACHES = {
    "default": {
        "BACKEND": "redis_cache.cache.RedisCache",
        "LOCATION": "10.122.249.28:6379:13",
        "OPTIONS": {
            "CLIENT_CLASS": "redis_cache.client.DefaultClient",
        }
    }
}

# we store files on S3 on prod boxes
AWS_ACCESS_KEY_ID='AKIAIC3VWPV5ZKZNFCIQ'
AWS_SECRET_ACCESS_KEY='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'
AWS_STORAGE_BUCKET_NAME='nyaruka-textit'
DEFAULT_FILE_STORAGE = 'storages.backends.s3boto.S3BotoStorage'

# require cookies to be on https (ergo logged in users must be on https)
SESSION_COOKIE_SECURE = True

# trust connections that are coming in on this protocol
SECURE_PROXY_SSL_HEADER = ('HTTP_X_FORWARDED_PROTO', 'HTTPS')

MANAGERS = ADMINS

# configure sentry for our prod server
SENTRY_DSN = 'http://c5176c7b17cb4713b50a98058f7f44c2:579cca08efc545e49055fca33152bdaa@monitor.nyaruka.com/13'

# add gunicorn
INSTALLED_APPS = INSTALLED_APPS + ('gunicorn', 'raven.contrib.django.raven_compat', 'storages')
MIDDLEWARE_CLASSES = ('django.middleware.transaction.TransactionMiddleware',
		      'raven.contrib.django.raven_compat.middleware.SentryResponseErrorIdMiddleware',) + MIDDLEWARE_CLASSES

# static dir is different for prod
STATIC_URL = '/sitestatic/'
COMPRESS_URL = '/sitestatic/'

import dj_database_url
DATABASES['default'] =  dj_database_url.config()

# set the mail settings, we send throught gmail
EMAIL_HOST = 'email-smtp.us-east-1.amazonaws.com'
DEFAULT_FROM_EMAIL = 'TextIt <website@textit.in>'
EMAIL_HOST_USER = 'AKIAIZHUHM76XIUM4NSQ'
EMAIL_HOST_PASSWORD = 'AjphZ8DKRTWwQFld8ci76bCEcW93uXcnhkxmTBxwYune'
EMAIL_USE_TLS = True

LOGGING = {
    'version': 1,
    'disable_existing_loggers': True,
    'root': {
        'level': 'WARNING',
        'handlers': ['sentry'],
    },
    'formatters': {
        'verbose': {
            'format': '%(levelname)s %(asctime)s %(module)s %(process)d %(thread)d %(message)s'
        },
    },
    'handlers': {
        'sentry': {
            'level': 'ERROR',
            'class': 'raven.contrib.django.raven_compat.handlers.SentryHandler',
        },
        'console': {
            'level': 'DEBUG',
            'class': 'logging.StreamHandler',
            'formatter': 'verbose'
        }
    },
    'loggers': {
        'django.db.backends': {
            'level': 'ERROR',
            'handlers': ['console'],
            'propagate': False,
        },
        'raven': {
            'level': 'DEBUG',
            'handlers': ['console'],
            'propagate': False,
        },
        'sentry.errors': {
            'level': 'DEBUG',
            'handlers': ['console'],
            'propagate': False,
        },
    },
}

# Key for Google Cloud Messaging
GCM_API_KEY = "AIzaSyAHAZJTqoNa99JRY0QBJl-86MQyAkfJMZw"

COMPRESS_CSS_HASHING_METHOD = 'content'
COMPRESS_OFFLINE = True
COMPRESS_OFFLINE_CONTEXT = dict(STATIC_URL=STATIC_URL, base_template='frame.html', debug=False, testing=False)

