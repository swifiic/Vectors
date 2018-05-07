#!/bin/bash

# this file is a wrapper around 2L or 4L script for SHM

MY_PATH="`dirname \"$0\"`"
file_counter=`date +"%d_%H_%M_%S"`

# "time":1523788802}],"traversal":[{"first":1524043801,"second":"aperture"},{"first":1524043861,"second":"Vectors_SM-T355Y_1.2_b500 / shobna.dhamija@gmail.com"},{"first":1524047174,"second":"Vectors_Moto G (4)_1.2.1_b043 / arnav.dhamija@gmail.com"}]}

/usr/bin/adb pull /sdcard/VectorsData/ack_video_00.json /tmp/${file_counter}


value=`cat /tmp/${file_counter} | grep  traversal | awk -F'traversal":' '{ print $2 }'`
echo "${file_counter} ${value}" >> ${MY_PATH}/../ack_received.txt

if [[ -e /tmp/last_unique ]] ; then 
    diffVal=`diff /tmp/${file_counter} /tmp/last_unique`
    if [[ -z "$diffVal" ]] ; then
       rm /tmp/${file_counter}
    else 
       cp -p /tmp/${file_counter} /tmp/last_unique
    fi
else 
   cp -p /tmp/${file_counter} /tmp/last_unique
fi


