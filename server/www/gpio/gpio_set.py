# -*- coding: utf-8 -*-
import RPi.GPIO as GPIO
import time
import sys
import json

if len(sys.argv) < 3:
    print "Too few parameters!"
    sys.exit()

GPIO.setmode(GPIO.BOARD)
mPort = int(sys.argv[1])

if mPort <= 0 or mPort > 40:
    print "id must be 1-40!"
    sys.exit()

mMode = sys.argv[2]
if mMode == "in":
    GPIO.setup(mPort,GPIO.IN)
    inputValue = GPIO.input(mPort)
    data = {'name':'gpio','id':mPort,'mode':mMode,'var':inputValue}
    print json.dumps(data)
elif mMode == "out":
    GPIO.setup(mPort,GPIO.OUT)
    if len(sys.argv) < 4:
        print "Voltage not specified!"
        sys.exit()
    mVol = sys.argv[3]
    if mVol == "low":
        GPIO.output(mPort,GPIO.LOW)
        data = {'name':'gpio','id':mPort,'mode':mMode,'var':mVol}
        print json.dumps(data)
    elif mVol == "high":
        GPIO.output(mPort,GPIO.HIGH)
        data = {'name':'gpio','id':mPort,'mode':mMode,'var':mVol}
        print json.dumps(data)
    else:
        print "voltage must be high or low!"
else:
    print "mode must be in or out!"