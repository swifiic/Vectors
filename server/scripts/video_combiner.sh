#!/bin/sh

mkdir -p /var/www/video_in/combined

mkdir -p /var/www/video_in/encoded

mkdir -p /var/www/video_in/decoded

mkdir -p /var/www/video_in/todecode

cd /var/www/video_in/

cp ./video* ./todecode

cd ./todecode

files=()

seqNumStr=""

while [[ true ]]; do
    oldestMDFile=`find ./ -maxdepth 1 -type f -name "video*.md" -printf '%T@ %p\0' | sort -zn | sed -zn '1s/[0-9,\.]\+ //p'`

    echo ${oldestMDFile}

    tmp=${oldestMDFile#*_}   # remove prefix ending in "_"
    seqNumStr=${tmp%.*}   # remove suffix starting with "."

    echo "seq number = ${seqNumStr}"

    echo video_${seqNumStr}_L0T1.out
    if [[ -e video_${seqNumStr}_L0T1.out ]]; then
        break
    else
        rm video_${seqNumStr}*
        echo "hello"
    fi
done

seqNumInt=`expr $seqNumStr + 0`
echo ${seqNumInt}

for (( i = 0; i < 5; i++ )); do
    counterPart=`printf "%05d" ${seqNumInt}`
    files+=(${counterPart})
    ((seqNumInt++));
done

echo "files array elements"

printf '%s\n' "${files[@]}"

newYUVFile=$(printf "_%s" "${files[@]}")
newYUVFile=${newYUVFile:1}
newYUVFile="output_${newYUVFile}.yuv"
echo $newYUVFile

addFirstFrame=0

numFramesToAdd=32

count=0

echo "num values in array = ${#files[*]}"

for (( i = 0; i < ${#files[*]}; ++ i ))
do
   echo ${files[$i]}
   if [[ -f video_${files[$i]}_L0T1.out && -f video_${files[$i]}.md ]]; then

       /var/spool/vector/bin/CombineLS video_${files[$i]} /var/www/video_in/encoded/enc_${files[$i]}.bin 5 2

       /var/spool/vector/bin/TAppDecoderStaticd -b /var/www/video_in/encoded/enc_${files[$i]}.bin -ls 2 -o0 /var/www/video_in/decoded/out_${files[$i]}_Q.yuv

       if [[ 1 == ${addFirstFrame} ]]; then
           echo 'extending the first frame'
           firstFrame=`ls -At frames* | tail -n 1`

           for (( j=1; j <= ${numFramesToAdd}; ++j ))
           do
               cat firstFrame >> $newYUVFile
           done
           rm frames*
           addFirstFrame=0
           numFramesToAdd=32
       fi

       cat /var/www/video_in/decoded/out_${files[$i]}_Q.yuv >> $newYUVFile

   else
       echo 'base layer not found'
       echo 'extending the last frame'
       addFirstFrame=1

       ffmpeg -f rawvideo -framerate 5 -s 640x480 -pixel_format yuyv422 -i out_${files[$i-1]}_Q.yuv -c copy -f segment -segment_time 1 frames%d.yuv
       lastFrame=`ls -Art frames* | tail -n 1`

       for (( k = i+1; k < ${#files[*]}; k++ )); do
           if [[ ! -f video_${files[$i]}_L0T1* && -f video_${files[$i]}.md  ]]; then
               numFramesToAdd+=${numFramesToAdd}
               ((count++))
           else
               break
           fi
       done

       for (( j=1; j <= ${numFramesToAdd}; ++j ))
       do
           cat lastFrame >> $newYUVFile
       done

       i=i+count
       count=0
   fi
done

echo  "scaling the video"
# ffmpeg -s:v 160x120 -r 5 -i ${newYUVFile} -vf scale=320:240 -c:v rawvideo -pix_fmt yuyv422 ${newYUVFile}
#
echo "convert yuv to .264 in mp4 container"
# ffmpeg -f rawvideo -vcodec rawvideo -s 640x480 -r 30 -pix_fmt yuyv422 -i ${newYUVFile} -c:v libx264 -preset ultrafast -qp 0 ${newYUVFile}.mp4

# mv ./video_${fileCounter}* /var/www/video_in/combined

# ffmpeg -f lavfi -i nullsrc=s=640x480:d=10:r=30 -i output_640x480.yuv -filter_complex "[0:v][1:v]overlay[video]" -map "[video]" -shortest output_video.yuv

# ffmpeg -f rawvideo -framerate 5 -s 640x480 -pixel_format yuv420p -i in.yuv -c copy -f segment -segment_time 0.01 frames%d.yuv
