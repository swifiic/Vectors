#!/bin/bash

# this file by cron wrapper called after layer files are generated

tgtdir=`/usr/bin/adb shell "ls /sdcard/RoamnetData/ ; exit 0" | head -n 1`

dest_fldr="/var/www/video_in"
import="${dest_fldr}/import"
newLine="
"
timeAtDest=`date +%s`
/usr/bin/adb pull /sdcard/RoamnetData/ ${import}
rcvdFiles=`ls ${import}/video*| grep -v json`
outStr=""
for file in ${rcvdFiles} ; do
   mv ${file} ${dest_fldr}
   mv ${file}.json ${dest_fldr}
   fileName=$(basename ${file})
   /usr/bin/adb shell "rm /sdcard/RoamnetData/${fileName}*; exit 0"
   echo "Moved ${file}"
   if [ -f "${dest_fldr}/${fileName}" ] ; then
       outStr=$"${outStr}${newLine}{\"filename\":\"${fileName}\",\"time\":${timeAtDest}},"
   else
       echo "Failed to move file ${fileName} - not generating an ack"
   fi
done

echo "${outStr}" | cat - ${dest_fldr}/rcvdlist.txt > /tmp//rcvdlist.txt.temp && mv /tmp//rcvdlist.txt.temp ${dest_fldr}/rcvdlist.txt
subStr=`head -n 500 ${dest_fldr}/rcvdlist.txt | grep -v "^$"`
str2=${subStr::-1} ; # remove the last ","

echo "{\"ack_time\":${timeAtDest},"items":[${str2}]}" > /tmp/ack_video_00.json
/usr/bin/adb push /tmp/ack_video_00.json /sdcard/RoamnetData/
if [ "$?" -ne "0" ]; then
      echo "Push of ack may have failed"
fi
echo "Generated Ack as # {\"ack_time\":${timeAtDest},"items":[${str2}]} #"

# {"ack_time":1522417102585,"items":[{"filename":"md_00063.md","time":1522417102585},{"filename":"md_00062.md","time":1522417102585},{"filename":"video_00096_L0T1.out","time":1522417102585},{"filename":"md_00061.md","time":1522417102585},{"filename":"md_00096.md","time":1522417102585}]}


# date >> /var/spool/vector/run_out


