#!/bin/bash

# this file is a wrapper around 2L or 4L script for SHM

MY_PATH="`dirname \"$0\"`"
echo "$MY_PATH" >> /var/spool/vector/run_out
date >> /var/spool/vector/run_out

bash ${MY_PATH}/video_record_2L.sh >> /var/spool/vector/run_out 2>&1

