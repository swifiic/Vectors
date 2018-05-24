#!/bin/bash
############################################################################
#    Copyright (C) 2018 by The SWiFiIC Project <apps4rural@gmail.com>      #
#                                                                          #
#    This program is free software; you can redistribute it and/or modify  #
#    it under the terms of the GNU General Public License as published by  #
#    the Free Software Foundation; either version 2 of the License, or     #
#    (at your option) any later version.                                   #
#                                                                          #
#    This program is distributed in the hope that it will be useful,       #
#    but WITHOUT ANY WARRANTY; without even the implied warranty of        #
#    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         #
#    GNU General Public License for more details.                          #
#                                                                          #
#    You should have received a copy of the GNU General Public License     #
#    along with this program; if not, write to the                         #
#    Free Software Foundation, Inc.,                                       #
#    51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA            #
############################################################################

############################################################################
#    Code for Campus Experiments: April 2018                               #
#    Authors: Abhishek Thakur, Arnav Dhamija, Tejashwar Reddy G            #
############################################################################



# this file by cron wrapper called after layer files are generated
filesBase=/var/spool/vectors

tgtdir=`/usr/bin/adb shell "ls /sdcard/VectorsData/ ; exit 0" | head -n 1`

mkdir -p ${filesBase}/video_in/dSpace
dSpace="${filesBase}/video_in/dSpace"
dest_fldr="${filesBase}/video_in"
import="${dest_fldr}/import"
newLine="
"

timeAtDest=`date +%s`
/usr/bin/adb pull /sdcard/VectorsData/ ${import}
echo "attempted to pull content at $timeAtDest}"
rcvdFiles=`ls ${import}/video*| grep -v json`
outStr=""
for file in ${rcvdFiles} ; do
    cp ${file} ${dSpace}
    mv ${file} ${dest_fldr}
    mv ${file}.json ${dest_fldr}
    fileName=$(basename ${file})
    /usr/bin/adb shell "rm /sdcard/VectorsData/${fileName}*; exit 0"
    echo "Moved ${file}"
    if [ -f "${dest_fldr}/${fileName}" ] ; then
        outStr=$"${outStr}${newLine}{\"filename\":\"${fileName}\",\"time\":${timeAtDest}},"
    else
        echo "Failed to move file ${fileName} - not generating an ack"
    fi
done

echo "${outStr}" | cat - ${dest_fldr}/rcvdlist.txt > /tmp//rcvdlist.txt.temp && mv /tmp//rcvdlist.txt.temp ${dest_fldr}/rcvdlist.txt
subStr=`head -n 2500 ${dest_fldr}/rcvdlist.txt | grep -v "^$"`
str2=${subStr::-1} ; # remove the last ","
origin=`hostname`


echo "{\"ack_time\":${timeAtDest},\"traversal\":[{\"first\":${timeAtDest},\"second\":\"${origin}\"}],"items":[${str2}]}" > /tmp/ack_video_00.json
/usr/bin/adb push /tmp/ack_video_00.json /sdcard/VectorsData/
if [ "$?" -ne "0" ]; then
    echo "Push of ack may have failed"
fi
echo "Tried to push the ack"


