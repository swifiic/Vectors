#!/bin/bash

# this file is a wrapper around 2L or 4L script for SHM

MY_PATH="`dirname \"$0\"`"
video_file_counter=`cat /var/spool/vector/counter`
counterPart=`printf "%05d" ${video_file_counter}`

echo "$MY_PATH" >> /var/spool/vector/run_out_${counterPart}
date >> /var/spool/vector/run_out

bash ${MY_PATH}/video_record_2L.sh >> /var/spool/vector/run_out_${counterPart} 2>&1

echo "Generating the JSON and moving files to MTP of connected device"
bash ${MY_PATH}/file_usb_push.sh ${video_file_counter} >> /var/spool/vector/run_out_${counterPart} 2>&1

