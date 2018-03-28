#!/bin/bash

set -o xtrace

cd /var/spool/vector

fileBase=/var/www/video_out
framerate=10
num_frames=64
input=/dev/video0
resolution=640x480

video_file_counter=`cat ./counter`
counterPart=`printf "%05d" ${video_file_counter}`
temp_file_counter=$((video_file_counter+1))
echo ${temp_file_counter} > ./counter

srcWidthStr=" -wdt0 320 -hgt0 240 -wdt1 640 -hgt1 480 "
inputOutputFiles=" -i0 rec/output_${counterPart}_Q.yuv -i1 rec/output_${counterPart}.yuv -b rec/output_${counterPart}.bin"
framerateStr=" -fr0 ${framerate} -fr1 ${framerate} "
encoderCommand="bin/TAppEncoderStaticd -c cfg/encoder_randomaccess_scalable.cfg -c cfg/Highway-2L-2x_final.cfg -c cfg/layers2_final.cfg ${inputOutputFiles} ${srcWidthStr} ${framerateStr} -f ${num_frames}"
extractCommand="../bin/ExtractAddLSStaticd output_${counterPart}.bin layers_${counterPart} 5 2"

ffmpegPath=`which ffmpeg`

if [ ! -z $ffmpegPath ]; then
    # recording and down converting to get QCIF file.
    ${ffmpegPath} -f v4l2 -framerate ${framerate} -video_size ${resolution} -i ${input} -vframes ${num_frames} rec/output_${counterPart}.yuv

    bin/DownConvertStaticd 640 480 rec/output_${counterPart}.yuv 320 240 rec/output_${counterPart}_Q.yuv

    echo "Downconverded the file";
    ls -l rec/output_${counterPart}*


    # Runing Enncoder to encode the recorded sample.
    echo "Coding as ${encoderCommand}"
    `${encoderCommand}`


    # Extracting the layers from the encoded file.
    cd rec
    `${extractCommand}`


    count=`ls -1 layers* 2>/dev/null | wc -l`
    if [ $count != 0 ]; then
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
else
    echo "ffmpeg path not found";
fi
