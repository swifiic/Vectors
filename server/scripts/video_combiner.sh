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

if [ "$#" -ne 2 ]; then
    echo "Usage: video_combiner.sh  \"no.of sequence numbers\"  \"no.of frames\""
    exit
fi

filesBase=/var/spool/vectors
base=${filesBase}/video_in

# combined layers output bin
mkdir -p ${base}/encoded

# combined layers output yuv
mkdir -p ${base}/decoded

# temp dir workspace
mkdir -p ${base}/todecode


mkdir -p ${base}/combined/yuv

mkdir -p ${base}/combined/compressed

mkdir -p ${base}/waste

cd ${base}/dSpace

files=() # array to save file sequence numbers

numSeq=$1

seqNumStr=""

#get the first file in the folder
while [[ true ]]; do
    firstFile=` ls video_*.md | head -n 1`

    echo ${firstFile}

    tmp=${firstFile#*_}   # remove prefix ending in "_"
    seqNumStr=${tmp%.*}   # remove suffix starting with "."

    echo "seq number = ${seqNumStr}"

    echo video_${seqNumStr}_L0T1.out
    if [[ -e video_${seqNumStr}_L0T1.out ]]; then
        break
    else
        mv video_${seqNumStr}* ../waste
    fi
done

seqNumInt=`expr $seqNumStr + 0`
echo ${seqNumInt}

for (( i = ${seqNumInt}; i <= $((seqNumInt+numSeq)); i++ )); do
    counterPart=`printf "%05d" ${i}`
    files+=(${counterPart})
    mv ./video_${counterPart}.md ./video_${counterPart}_L0*.out ../todecode
done

echo "files array elements"

printf '%s\n' "${files[@]}"

newYUVFile=$(printf "_%s" "${files[@]}")
newYUVFile=${newYUVFile:1}
newYUVFile="output_${newYUVFile}.yuv"
echo $newYUVFile

bAddFirstFrame=0

numFramesToAdd=32

count=0

echo "num values in array = ${#files[*]}"

tFactor=0
lCount=0

dc_res_1="320 240 "
dc_res_2="160 120 "

cd ${filesBase}/video_in/todecode

for (( i = 0; i < ${#files[*]}; ++ i ))
do
    echo "current file sequence number ${files[$i]}"

    if [[ ! -e video_${files[$i]}.md ]]; then
        # nothing can be done
        # add frames from the previous and the next video
        tFactor=0
    elif [[ ! -e video_${files[$i]}_L0T1.out ]]; then
        # nothing can be done
        # add frames from the previous and the next video
        tFactor=0
    elif [[ ! -e video_${files[$i]}_L0T2.out ]]; then
        # each frame 16 times
        tFactor=16
    elif [[ ! -e video_${files[$i]}_L0T3.out ]]; then
        # each frame 8 times
        tFactor=8
    elif [[ ! -e video_${files[$i]}_L0T4.out ]]; then
        # each frame 4 times
        tFactor=4
    elif [[ ! -e video_${files[$i]}_L0T5.out ]]; then
        # each frame 2 times
        tFactor=2
    else
        tFactor=1
        echo "All the layers are available"
    fi

    if [[ ${tFactor} == 0 ]]; then
        #add last frame of previous video
        lastFrame=`ls ${filesBase}/video_in/decoded/frames* -Art | tail -n 1`
        for (( i = 0; i < 97; i++ )); do
            cat ${lastFrame} >> ${filesBase}/video_in/combined/yuv/${newYUVFile}
        done
    else
        echo "combining the layers"
        ${filesBase}/bin/CombineLS video_${files[$i]} ${filesBase}/video_in/encoded/enc_${files[$i]}.bin 5 2

        echo "decoding the layers"
        ${filesBase}/bin/TAppDecoderStaticd -b ${filesBase}/video_in/encoded/enc_${files[$i]}.bin -ls 2 -o0 ${filesBase}/video_in/decoded/out_${files[$i]}_Q.yuv

        echo  "scaling the video"
        ${filesBase}/bin/DownConvertStaticd ${dc_res_2} ${filesBase}/video_in/decoded/out_${files[$i]}_Q.yuv ${dc_res_1} ${filesBase}/video_in/decoded/out_${files[$i]}.yuv

        echo "splitting frames"
        ffmpeg -f rawvideo -framerate 5 -s 320x240 -pixel_format yuv420p -i ${filesBase}/video_in/decoded/out_${files[$i]}.yuv -c copy -f segment -segment_time 0.01 ${filesBase}/video_in/decoded/frames%d.yuv

        echo "creating the new YUV file"
        tNumFrames=`ls ${filesBase}/video_in/decoded/frames* | wc -l`
        for (( k = 0; k < ${tNumFrames}; k++ )); do
            # for each frame
            for (( j = 0; j < ${tFactor}; j++ )); do
                # adding each frame tFactor times
                cat ${filesBase}/video_in/decoded/frames${k}.yuv >> ${filesBase}/video_in/combined/yuv/${newYUVFile}
            done
        done
    fi
    mv ./video_${files[$i]}* ${filesBase}/video_in/waste
done

rm ${filesBase}/video_in/decoded/frames*

compressedFile=${newYUVFile%.*}
echo "convert yuv to .264 in mp4 container"
ffmpeg -f rawvideo -vcodec rawvideo -s 320x240 -r 5 -pix_fmt yuv420p -i ${filesBase}/video_in/combined/yuv/${newYUVFile} -c:v libx264 -preset ultrafast -qp 0 ${filesBase}/video_in/combined/compressed/${compressedFile}.mp4
