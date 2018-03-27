#!/bin/bash

# set -o xtrace

cd /var/spool/vector
video_file_counter=`cat ./counter`

counterPart=`printf "%05d" ${video_file_counter}`
temp_file_counter=$((video_file_counter+1))
echo ${temp_file_counter} > ./counter

fileBase=/var/www/video_out
#if [[ "$#" -ne 5 ]]; then
#    echo "Usage: ./video_record.sh framerate(30) num_frames(9000)"
#fi

#video_file_counter = 0
#framerate = $1
#num_frames = $2

framerate=10
num_frames=64
input=/dev/video0
resolution=640x480

/usr/local/bin/ffmpeg -f v4l2 -framerate ${framerate} -video_size ${resolution} -i ${input} -vframes ${num_frames} rec/output_${counterPart}.yuv

bin/DownConvertStaticd 640 480 rec/output_${counterPart}.yuv 320 240 rec/output_${counterPart}_Q.yuv

# DownConvertStaticd - DownConvertStaticd 352 288 highway_cif_2k.yuv 176 144 highway_qcif30_2k.yuv

echo "Downconverded the file";
ls -l rec/output_${counterPart}*

# now SHM ecode it
srcWidthStr=" -wdt0 320 -hgt0 240 -wdt1 640 -hgt1 480 "
inputOutputFiles=" -i0 rec/output_${counterPart}_Q.yuv -i1 rec/output_${counterPart}.yuv -b rec/output_${counterPart}.bin"
framerateStr=" -fr0 ${framerate} -fr1 ${framerate} "
command="bin/TAppEncoderStaticd -c cfg/encoder_randomaccess_scalable.cfg -c cfg/Highway-2L-2x_final.cfg -c cfg/layers2_final.cfg ${inputOutputFiles} ${srcWidthStr} ${framerateStr} -f ${num_frames}"

echo "Coding as ${command}"
`${command}`

# now extract the layer files
command="../bin/ExtractAddLSStaticd output_${counterPart}.bin layers_${counterPart} 5 2"
cd rec
# `${command}`
# ls -l layers*
if [ -f blah ]; then
   mv layers* ${fileBase}
fi

cd ${fileBase}

# now remove the files that are really old
# fileCount="$(ls layers*)"
fileCount="$(find . -type f | wc -l)"
while [[ ${fileCount} -gt 256 ]] ; do
    fileToDel=`ls -t | tail -n 1`
    echo "Deleting ${fileToDel}"; rm ${fileToDel}
    fileToDel=`ls -t | tail -n 1`
    echo "Deleting ${fileToDel}"; rm ${fileToDel}
    fileToDel=`ls -t | tail -n 1`
    echo "Deleting ${fileToDel}"; rm ${fileToDel}
    fileToDel=`ls -t | tail -n 1`
    echo "Deleting ${fileToDel}"; rm ${fileToDel}
    fileToDel=`ls -t | tail -n 1`
    echo "Deleting ${fileToDel}"; rm ${fileToDel}
    fileToDel=`ls -t | tail -n 1`
    echo "Deleting ${fileToDel}"; rm ${fileToDel}
    fileCount=`ls layers*`
done
