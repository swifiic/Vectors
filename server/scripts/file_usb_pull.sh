#!/bin/bash

# this file by cron wrapper called after layer files are generated

tgtdir=`ls -d /run/user/*/gvfs/mtp*/Inter*/RoamnetData | head -n 1`
TgtDir=$(printf %q "$tgtdir")

dest_fldr="/var/www/video_in"
import="${dest_fldr}/import"

ls -l "${tgtdir}"
timeAtDest=`date +%s`
mv "${tgtdir}"/video* "${import}"
rcvdFiles=`ls ${import}/video*| grep -v json`
outStr=""
for file in ${rcvdFiles} ; do
   mv "${file}" ${dest_fldr}
   fileName=$(basename ${file})
   outStr=$"${outStr}{\"filename\":\"${fileName}\",\"time\":${timeAtDest}},"
done

echo "${outStr}" | cat - ${dest_fldr}/rcvdlist.txt > /tmp//rcvdlist.txt.temp && mv /tmp//rcvdlist.txt.temp ${dest_fldr}/rcvdlist.txt
subStr=`head -n 20 ${dest_fldr}/rcvdlist.txt`
str2=${subStr::-1} ; # remove the last ","

echo "{\"ack_time\":${timeAtDest},"items":[${str2}]}" > "${tgtdir}"/ack_video_00.json
echo "Generated Ack as # {\"ack_time\":${timeAtDest},"items":[${str2}]} #"

# {"ack_time":1522417102585,"items":[{"filename":"md_00063.md","time":1522417102585},{"filename":"md_00062.md","time":1522417102585},{"filename":"video_00096_L0T1.out","time":1522417102585},{"filename":"md_00061.md","time":1522417102585},{"filename":"md_00096.md","time":1522417102585}]}

ls "${tgtdir}"/video_${counterPart}* "${tgtdir}"/ack*

# date >> /var/spool/vector/run_out


