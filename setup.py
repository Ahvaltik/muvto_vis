try:
    from setuptools import setup
except ImportError:
    from distutils.core import setup

config = {
    'name': 'multivarianttrafficoptimization',
    'version': '0.1.0',
    'description': 'Multivariant traffic optimization',
    'packages': ['trafficoptimization'],
    'scripts': [],
    'entry_points': {
        'console_scripts': [
            'trafficoptimization = trafficoptimization.main:main'
        ]
    }
}

setup(**config)
