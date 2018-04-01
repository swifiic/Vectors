#!/bin/bash

# this file is a wrapper around 2L or 4L script for SHM

MY_PATH="`dirname \"$0\"`"

echo -n "Receiver called at " >> /var/spool/vector/run_out_rcvr
date >> /var/spool/vector/run_out_rcvr

echo "Generating the ACK JSON and moving files from MTP of connected device"
bash ${MY_PATH}/file_usb_pull.sh  >> /var/spool/vector/run_out_rcvr 2>&1
echo -n "cron done at " >> /var/spool/vector/run_out_rcvr
date >> /var/spool/vector/run_out_rcvr
