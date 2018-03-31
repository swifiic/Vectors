#!/bin/bash

# this file by cron wrapper called after layer files are generated

DestDir=`ls -d /run/user/*/gvfs/mtp*/Inter*/RoamnetData | head -n 1`

video_file_counter=$1
counterPart=`printf "%05d" ${video_file_counter}`
origin=`hostname`
src_fldr="/var/www/video_out"

# video_00175_L0T1.out  video_00175_L0T3.out  video_00175_L0T5.out  video_00175_L1T2.out  video_00175_L1T4.out  video_00175.md
# video_00175_L0T2.out  video_00175_L0T4.out  video_00175_L1T1.out  video_00175_L1T3.out  video_00175_L1T5.out

fileNames=("_L0T1.out" "_L0T2.out" "_L0T3.out" "_L0T4.out" "_L0T5.out" "_L1T1.out" "_L1T2.out" "_L1T3.out" "_L1T4.out" "_L1T5.out" ".md")
copyCounts=( 32         16          16           8           8              6           6         6           6           6         32)



ls -l "${DestDir}"

# {"creationTime":1522471595,"fileName":"video_00179.md","maxSvcLayer":2,"maxTemporalLayer":5,"sequenceNumber":179,"svcLayer":0,"temporalLayer":0,"tickets":16,"traversal":[{"first":1522471595,"second":"hpslkbk"}],"ttl":86400}
# {"creationTime":1522324375,"fileName":"video_00179.md","maxSvcLayer":2,"maxTemporalLayer":5,"sequenceNumber":101,"svcLayer":0,"temporalLayer":0,"tickets":16,"traversal":[{"first":1522325856,"second":"hpslkbk"}],"ttl":86400}

timeAtOrigin=`date +%s`
for (( k=0 ; k < 11 ; k++ )) ; do 
    baseFileEnd=${fileNames[k]}
    count=${copyCounts[k]}
    layer=$(( $k / 5 ));
    tempId=$(( $k % 5 ));
    if [ -f "${src_fldr}/video_${counterPart}${baseFileEnd}" ] ; then
        echo " processing ${src_fldr}/video_${counterPart}${baseFileEnd} for count ${count} layer ${layer} and tempId=${tempId}";

        outStr="{\"creationTime\":${timeAtOrigin},\"fileName\":\"video_${counterPart}${baseFileEnd}\",\"maxSvcLayer\":2,\"maxTemporalLayer\":5,\"sequenceNumber\":${video_file_counter},\"svcLayer\":${layer},\"temporalLayer\":${tempId},\"tickets\":${count},\"traversal\":[{\"first\":${timeAtOrigin},\"second\":\"${origin}\"}],\"ttl\":86400}"
        echo ${outStr}
        echo ${outStr} > ${src_fldr}/video_${counterPart}${baseFileEnd}.json
    else
        echo "file not found ${src_fldr}/video_${counterPart}${baseFileEnd}"
    fi
done

mv ${src_fldr}/video_${counterPart}* "${DestDir}"
ls ${src_fldr}/video_${counterPart}* "${DestDir}"/video_${counterPart}*

# date >> /var/spool/vector/run_out


