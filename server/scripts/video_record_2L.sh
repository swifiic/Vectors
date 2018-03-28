#!/bin/bash

set -o xtrace

cd /var/spool/vector

# use v4l2-ctl --list-formats-ext to find options on framerate etc.
fileBase=/var/www/video_out
framerate=5
num_frames=1600
input=/dev/video0

# resolution based arguments
# # # # # # high Res
# resolution=1280x960
# dc_res_1="1280 960 "
# dc_res_2="640 480 "
# srcWidthStr=" -wdt0 640 -hgt0 480 -wdt1 1280 -hgt1 960 "

# # # # # # Medium Res
# resolution=640x480
# dc_res_1="640 480 "
# dc_res_2="320 240 "
# srcWidthStr=" -wdt0 320 -hgt0 240 -wdt1 640 -hgt1 480 "

# # # # # # Low res
resolution=320x240
dc_res_1="320 240 "
dc_res_2="160 120 "
srcWidthStr=" -wdt0 160 -hgt0 120 -wdt1 320 -hgt1 240 "


video_file_counter=`cat ./counter`
counterPart=`printf "%05d" ${video_file_counter}`
temp_file_counter=$((video_file_counter+1))
echo ${temp_file_counter} > ./counter

inputOutputFiles=" -i0 rec/output_${counterPart}_Q.yuv -i1 rec/output_${counterPart}.yuv -b rec/output_${counterPart}.bin"
framerateStr=" -fr0 ${framerate} -fr1 ${framerate} "
encoderCommand="bin/TAppEncoderStaticd -c cfg/encoder_randomaccess_scalable.cfg -c cfg/2L-2X_vector.cfg -c cfg/layers2_final.cfg ${inputOutputFiles} ${srcWidthStr} ${framerateStr} -f ${num_frames}"
extractCommand="../bin/ExtractAddLS output_${counterPart}.bin video_${counterPart} 5 2"
downConvertCommand="bin/DownConvertStaticd ${dc_res_1} rec/output_${counterPart}.yuv ${dc_res_2} rec/output_${counterPart}_Q.yuv"
ffmpegPath=`which ffmpeg`

if [ -z $ffmpegPath ]; then
    echo "ffmpeg path not found";
    exit 5
fi

# recording and down converting to get QCIF file.
${ffmpegPath} -f v4l2 -framerate ${framerate} -video_size ${resolution} -i ${input} -vframes ${num_frames} rec/output_${counterPart}.yuv
${downConvertCommand}

echo "Downconverded the file";
ls -l rec/output_${counterPart}*


# Runing Enncoder to encode the recorded sample.
echo "Coding as ${encoderCommand}"
${encoderCommand}

# Extracting the layers from the encoded file.
cd rec
echo "Extracting as ${extractCommand} "
${extractCommand}


count=`ls -1 video* 2>/dev/null | wc -l`
if [ $count != 0 ]; then
    mv video* ${fileBase}
    mv ${fileBase}/video_${counterPart}.md  ${fileBase}/md_${counterPart}.md
fi
# remove files older than 4 days - for capture
find . -type f -mtime +96 -exec rm {} \;

cd ${fileBase}

# remove files older than 2 days - for transfer
find . -type f -mtime +48 -exec rm {} \;

