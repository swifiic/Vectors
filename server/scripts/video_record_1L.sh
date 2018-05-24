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


set -o xtrace

filesBase=/var/spool/vectors
cd ${filesBase}

# use v4l2-ctl --list-formats-ext to find options on framerate etc.
outBase=${filesBase}/video_out
#framerate=0.25
framerate=5
num_frames=129
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

resolution=320x240
dc_res_1="320 240 "
srcWidthStr=" -wdt0 320 -hgt0 240 "


video_file_counter=`cat ./counter`
cntr=`printf "%05d" ${video_file_counter}`
temp_file_counter=$((video_file_counter+1))
echo ${temp_file_counter} > ./counter

inputOutputFiles=" -i0 rec/op_${cntr}.yuv -b rec/op_${cntr}.bin"
# framerateStr=" -fr0 ${framerate} -fr1 ${framerate} "
framerateStr=" -fr0 1 "
encoderCommand="${filesBase}/bin/TAppEncoderStaticd -c ${filesBase}/cfg/raScale.cfg \
		-c ${filesBase}/cfg/1L-1X_vectors.cfg -c ${filesBase}/cfg/layers1.cfg ${inputOutputFiles} \
		${srcWidthStr} ${framerateStr} -f ${num_frames}"
extractCommand="${filesBase}/bin/ExtractAddLS op_${cntr}.bin video_${cntr} 6 1"
ffmpegPath=`which ffmpeg`

if [ -z $ffmpegPath ]; then
    echo "ffmpeg path not found";
    exit 5
fi

# recording and saving as 420p file for TAppEncoderStaticd
# ${ffmpegPath} -f video4linux2 or v4l2 based on OS version
${ffmpegPath} -f v4l2 -framerate ${framerate} -video_size ${resolution} -i ${input} -vframes ${num_frames} \
		rec/op_${cntr}.yuv

${ffmpegPath} -y -r ${framerate} -f rawvideo -s ${resolution} -pix_fmt yuyv422 -i rec/op_${cntr}.yuv \
		-pix_fmt yuv420p -f rawvideo -r ${framerate} -s ${resolution} rec/op_${cntr}_yuv420p.yuv

mv rec/op_${cntr}_yuv420p.yuv rec/op_${cntr}.yuv

${downConvertCommand}

echo "Downconverded the file";
ls -l rec/op_${cntr}*


# Runing Enncoder to encode the recorded sample.
echo "Coding as ${encoderCommand}"
${encoderCommand}

# Extracting the layers from the encoded file.
cd rec
echo "Extracting as ${extractCommand} "
${extractCommand}


count=`ls -1 video* 2>/dev/null | wc -l`
if [ $count != 0 ]; then
    mv video* ${outBase}
fi
# remove files older than 4 days - for capture
find . -type f -mtime +96 -exec rm {} \;

cd ${outBase}

# remove files older than 2 days - for transfer
find . -type f -mtime +48 -exec rm {} \;

