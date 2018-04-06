#!/bin/bash
# this file is called by cron wrapper called after layer files are generated

set -o xtrace
video_file_counter=$1
layerLast=`cat /var/spool/vector/lastLayer`
counterPart=`printf "%05d" ${video_file_counter}`

#DestDir=`ls -d /run/user/*/gvfs/mtp*/*/RoamnetData | head -n 1`
DestDir=`/usr/bin/adb shell "ls /sdcard/VectorsData/*L0T1*json ; exit 0" | head -n 1`

# if bridge device is not connected we generate only one layer
# but do not change the layerLast value
if [ -z "${DestDir}" ]; then
   layerLast=1;
else
    # find if the base layer of last two layer has been delivered
    #  if last layer delivered and  layerLast < 10, increment it
    #  if last two layers not delivered and layerLast > 1, decrement it
    if [ -z ${layerLast} ]; then
        layerLast=10;
    else
        indexLast=$(( ${video_file_counter} - 4 ));
        indexSecondLast=$(( ${indexLast} - 1 ));
        /usr/bin/adb shell "ls /sdcard/VectorsData/*L0T1* ; exit 0" > /tmp/tempList
        #ls -l "${DestDir}" > /tmp/tempList
        count=`grep -c -E "${indexLast}_L0T1.out|${indexSecondLast}_L0T1.out" /tmp/tempList`
        if [ "${count}" -eq "0" ] && [ "${layerLast}" -lt "10" ] ; then
            layerLast=$(( ${layerLast} + 1 ));
        fi
        if [ "${count}" -eq "4" ] && [  "${layerLast}" -gt "1" ] ; then
            layerLast=$(( ${layerLast} - 1 ));
        fi

    fi
    echo ${layerLast} > /var/spool/vector/lastLayer
fi

counterPart=`printf "%05d" ${video_file_counter}`
origin=`hostname`
src_fldr="/var/www/video_out"

# video_00175_L0T1.out  video_00175_L0T3.out  video_00175_L0T5.out  video_00175_L1T2.out  video_00175_L1T4.out  video_00175.md
# video_00175_L0T2.out  video_00175_L0T4.out  video_00175_L1T1.out  video_00175_L1T3.out  video_00175_L1T5.out

fileNames=( ".md" "_L0T1.out" "_L0T2.out" "_L0T3.out" "_L0T4.out" "_L0T5.out" "_L1T1.out" "_L1T2.out" "_L1T3.out" "_L1T4.out" "_L1T5.out" )
copyCounts=( 32   32         16          16           8           8              6           6         6           6           6         )

echo "Listing the target folder - may have errors or can be blank"
#ls -l "${DestDir}"
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

        outStr="{\"creationTime\":${timeAtOrigin},\"fileName\":\"video_${counterPart}${baseFileEnd}\",\"maxSvcLayer\":2,\"maxTemporalLayer\":5,\"sequenceNumber\":${video_file_counter},\"svcLayer\":${layer},\"temporalLayer\":${tempId},\"tickets\":${count},\"traversal\":[{\"first\":${timeAtOrigin},\"second\":\"${origin}\"}],\"ttl\":86400}"
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



# date >> /var/spool/vector/run_out


