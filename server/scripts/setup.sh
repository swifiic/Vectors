#!/bin/bash

if [ "$#" -ne 3 ]; then
    echo "Usage: bash setup.sh /path/to/vectors/server /path/to/SHMFolder /path/to/JSVMFolder"
    echo "   e.g. bash setup.sh /home/vectors/git/vectors/server/ ~/SHM-12.4 ~/JSVM-master/JSVM"

    exit
fi

pToRepo=$1
pToSHM=$2
pToJSVM=$3

filesBase=/var/spool/vector

path=${pToRepo}

mkdir -p ${filesBase}/bin ${filesBase}/scripts ${filesBase}/rec ${filesBase}/php

cp -r ${path}/cfg ${path}/scripts ${path}/php ${filesBase}/
echo 0 > ${filesBase}/counter
touch /var/www/video_in/rcvdlist.txt

echo "Starting compilation "
cd ${path}/src
make -j`nproc`
cd -

cd ${pToSHM}/build/linux
make -j`nproc`
cd -

cd ${pToJSVM}/H264Extension/build/linux
make -j`nproc`
cd -

echo "Finished compilation. Now deploying the binaries"
cp ${pToSHM}/bin/*Static* ${filesBase}/bin
echo "Deployed SHM binaries"
cp ${pToJSVM}/../bin/DownConvertStatic* ${filesBase}/bin
echo "Deployed JSVM binaries"
cp ${path}/src/CombineLS ${path}/src/ExtractAddLS ${filesBase}/bin
echo "Deployed Vectors binaries"

chmod a+x ${filesBase}/scripts/*
