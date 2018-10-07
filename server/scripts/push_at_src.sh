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


# this file is called by cron wrapper called after layer files are generated
source config
filesBase=/var/spool/vectors
set -o xtrace
# for Remi1S dest_node="f083"   : G3 a3e6df
# dest_node="a3e6df"
# src_node="42bf"
video_file_counter=$1
layerLast=`cat ${filesBase}/lastLayer`
counterPart=`printf "%05d" ${video_file_counter}`

#DestDir=`ls -d /run/user/*/gvfs/mtp*/*/RoamnetData | head -n 1`
DestDir=`/usr/bin/adb shell "ls /sdcard/VectorsData/*L0T1*json ; exit 0" | head -n 1`

# if bridge device is not connected we generate only one layer
# but do not change the layerLast value
if [ -z "${DestDir}" ]; then
   layerLast=10;
else
    # find if the base layer of last two layer has been delivered
    #  if last layer delivered and  layerLast < 10, increment it
    #  if last two layers not delivered and layerLast > 1, decrement it
    if [ -z ${layerLast} ]; then
        layerLast=100;
    else
        # indexLast=$(( ${video_file_counter} - 40 )); # 4 hours ago
        # indexSecondLast=$(( ${indexLast} - 40 ));   # 8 hours (2+2) ago
        # indexThirdLast=$(( ${indexLast} - 80 ));    # 16 ours (2+6) ago
              # 6,12,24 becomes 12,24,48
        indexLast=$(( ${video_file_counter} - 30 )); # 6 hours ago
        indexSecondLast=$(( ${indexLast} - 30 ));   # 12 hours (2+2) ago
        indexThirdLast=$(( ${indexLast} - 60 ));    # 24 ours (2+6) ago
        /usr/bin/adb shell "ls /sdcard/VectorsData/*L0T1* ; exit 0" > /tmp/tempList
        #ls -l "${DestDir}" > /tmp/tempList
        count=`grep -c -E "${indexLast}_L0T1.out|${indexSecondLast}_L0T1.out|${indexThirdLast}_L0T1.out" /tmp/tempList`
        oldMetric=${layerLast}
        if [ "${count}" -eq "0" ] && [ "${layerLast}" -lt "101" ] ; then
            layerLast=$(( ${layerLast} + 4 ));
        fi
        if [ "${count}" -eq "2" ] ; then
            if [ "${layerLast}" -lt "66" ] ; then
                layerLast=$(( ${layerLast} + 2 ));
            else
                layerLast=$(( ( ${layerLast} * 9 + 5)/10 ));
            fi
        fi
        if [ "${count}" -eq "4" ] ; then
            if [ "${layerLast}" -lt "33" ] ; then
                layerLast=$(( ${layerLast} + 1 ));
            else
                layerLast=$(( ( ${layerLast} * 9 + 5)/10 ));
            fi
        fi
        if [ "${count}" -eq "6" ] && [  "${layerLast}" -gt "15" ] ; then
            layerLast=$(( ( ${layerLast} * 9 + 5)/10 ));
        fi

    fi
    echo ${layerLast} > ${filesBase}/lastLayer
    echo "New metric ${layerLast} for count ${count} and old metric ${oldMetric}"
fi

layerLast=$(( ${layerLast} / 10 ));  # # no roundoff here

counterPart=`printf "%05d" ${video_file_counter}`
origin=`hostname`
src_fldr="${filesBase}/video_out"

# video_00175_L0T1.out  video_00175_L0T3.out  video_00175_L0T5.out  video_00175_L1T2.out  video_00175_L1T4.out  video_00175.md
# video_00175_L0T2.out  video_00175_L0T4.out  video_00175_L1T1.out  video_00175_L1T3.out  video_00175_L1T5.out

fileNames=( ".md" "_L0T1.out" "_L0T2.out" "_L0T3.out" "_L0T4.out" "_L0T5.out" "_L1T1.out" "_L1T2.out" "_L1T3.out" "_L1T4.out" "_L1T5.out" )
#copyCounts=( 128   128         64          64           48           48              32           32         24           24           16         )
copyCounts=( 768   512         256          256           192           192              128           128         96           96           64         )

echo "Listing the target folder - may have errors or can be blank"
ls -l "${src_fldr}"
/usr/bin/adb shell "ls /sdcard/VectorsData/ ; exit 0"


timeAtOrigin=`date +%s`
for (( k=0 ; k <= 10 ; k++ )) ; do
    baseFileEnd=${fileNames[k]}
    count=${copyCounts[k]}

    if  [ "$k" -gt "${layerLast}" ] ; then
        echo "     -----   removing file ${src_fldr}/video_${counterPart}${baseFileEnd}"
        rm ${src_fldr}/video_${counterPart}${baseFileEnd}
        continue
    fi
    echo "Processing file ${src_fldr}/video_${counterPart}${baseFileEnd} if present"
    layer=$(( ($k-1) / 5 ));
    tempId=$(( ($k-1) % 5 ));
    if [ "$k" -eq "0" ] ; then
        layer=0;
        tempId=0;
    fi

    if [ -f "${src_fldr}/video_${counterPart}${baseFileEnd}" ] ; then
        echo " processing ${src_fldr}/video_${counterPart}${baseFileEnd} for count ${count} layer ${layer} and tempId=${tempId}";

        outStr="{\"creationTime\":${timeAtOrigin},\"fileName\":\"video_${counterPart}${baseFileEnd}\",\"maxSvcLayer\":2,\"maxTemporalLayer\":5,\"sequenceNumber\":${video_file_counter},\"svcLayer\":${layer},\"temporalLayer\":${tempId},\"tickets\":${count},\"traversal\":[{\"first\":${timeAtOrigin},\"second\":\"${origin}\"}],\"ttl\":$ttl_time,\"destinationNode\":\"${destNode}\",\"sourceNode\":\"${srcNode}\"}"
        echo ${outStr}
        echo ${outStr} > ${src_fldr}/video_${counterPart}${baseFileEnd}.json
    else
        # for overlapping runs
        echo "file not found ${src_fldr}/video_${counterPart}${baseFileEnd}"
    fi
done

# now remove old files for layer 1 before pushing
echo "Removing older files for layer 1 and higher temporal ids"
find ${src_fldr} -name "vide*L1*" -mmin +30 -exec rm {} \; -print
find ${src_fldr} -name "vide*L0T[23456]*" -mmin +60 -exec rm {} \; -print
find ${src_fldr} -name "vide*md" -mmin +240 -exec rm {} \; -print

/usr/bin/adb push ${src_fldr}/ /sdcard/VectorsData/
if [ "$?" -ne "0" ]; then
      echo "Push of content may have falied. Leaving the files  in ${src_fldr}"
else
      rm -f ${src_fldr}/video_*
fi


