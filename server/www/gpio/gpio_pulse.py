# -*- coding: utf-8 -*-
import RPi.GPIO as GPIO
import time
import sys

if len(sys.argv) < 2 or len(sys.argv) > 4:
    print "Too few parameters!"
    sys.exit()

GPIO.setmode(GPIO.BOARD)
mPort = int(sys.argv[1])
pulseTime = 0.1

if mPort <= 0 or mPort > 40:
    print "id must be 1-40!"
    sys.exit()

GPIO.setup(mPort,GPIO.OUT)

if len(sys.argv) == 2:
    GPIO.setup(mPort,GPIO.OUT)
    GPIO.output(mPort,GPIO.HIGH)
    time.sleep(pulseTime)
    GPIO.output(mPort,GPIO.LOW)
elif len(sys.argv) == 3:
    GPIO.setup(mPort,GPIO.OUT)
    mFrequency = int(sys.argv[2])
    if mFrequency<=0:
        print "frequency must be larger than 0!"
        sys.exit()
    for i in range(1,mFrequency+1):
        GPIO.output(mPort,GPIO.HIGH)
        time.sleep(pulseTime)
        GPIO.output(mPort,GPIO.LOW)
        time.sleep(1)
elif len(sys.argv) == 4:
    GPIO.setup(mPort,GPIO.OUT)
    mFrequency = int(sys.argv[2])
    mLag = float(sys.argv[3])
    if mFrequency<=0:
        print "frequency must be larger than 0!"
        sys.exit()
    if mLag<=0:
        print "lag must be larger than 0!"
        sys.exit()
    for i in range(1,mFrequency+1):
        GPIO.output(mPort,GPIO.HIGH)
        time.sleep(pulseTime)
        GPIO.output(mPort,GPIO.LOW)
        time.sleep(mLag)
print "Success!"