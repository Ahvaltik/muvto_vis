multivarianttrafficoptimization
===

## Quick-start

Pyage in pip repository is broken, thus requires special
handling.

```bash
$ virtualenv venv
$ source ./venv/bin/activate # or .\venv\Scripts\activate.bat on Windows
$ pip install -r requirements.txt
$ pip install git+git://github.com/maciek123/pyage.git#egg=pyage

$ # work with this env

$ deactivate
```

Setuptools are configured, so you may install the project
(this is not required for development):
```
python setup.py install
```
