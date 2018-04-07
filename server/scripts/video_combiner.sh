#!/bin/sh

combinerCommand="/var/spool/vector/bin/CombineLS video_${fileCounter} /var/www/video_in/encoded/enc_${fileCounter}.bin 5 2"

mkdir -p /var/www/video_in/combined

mkdir -p /var/www/video_in/encoded

mkdir -p /var/www/video_in/decoded

mkdir -p /var/www/video_in/todecode

cd /var/www/video_in/import/

if [ -f video* ]; then
    mv video* ../todecode
fi

cd ../todecode

oldestFileName=$(find ./ -maxdepth 1 -type f -printf '%T@ %p\0' | sort -zn | \
  sed -zn '1s/[0-9,\.]\+ //p')
echo " oldestFileName : ${oldestFileName}"

fileCounter=$(echo ${oldestFileName}| cut -d'_' -f 2)
echo " fileCounter : ${fileCounter}"

/var/spool/vector/bin/CombineLS video_${fileCounter} /var/www/video_in/encoded/enc_${fileCounter}.bin 5 2 | grep 'Cannot open intput meta-data file' &> /dev/null

if [ $? == 0 ]; then
    echo "md file not found"
else
    /var/spool/vector/bin/TAppDecoderStaticd -b /var/www/video_in/encoded/enc_${fileCounter}.bin -ls 2 -o0 /var/www/video_in/decoded/out_${fileCounter}_Q.yuv -o1 /var/www/video_in/decoded/out_${fileCounter}.yuv
fi

mv ./video_${fileCounter}* /var/www/video_in/combined
