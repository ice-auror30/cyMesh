#!/bin/bash

ssh -A pbody.mattgerst.me "cd ~/projects/senior_design/batphone-real; git pull; ant debug"

scp pbody.mattgerst.me:~/projects/senior_design/batphone-real/bin/batphone-debug.apk bin/batphone-debug.apk

devices=`adb devices | tail -n +2 | awk '{ print $1;}'`

for device in $devices; do
    adb -s $device install -r bin/batphone-debug.apk
    adb -s $device shell am start -a android.intent.action.MAIN -n org.servalproject/.Main
done
