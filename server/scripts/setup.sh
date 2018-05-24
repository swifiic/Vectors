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



if [ "$#" -ne 2 ]; then
    echo "Usage: bash setup.sh  /path/to/SHMFolder /path/to/JSVMFolder"
    echo "   e.g. bash setup.sh  ~/SHM-12.4 ~/JSVM-master/JSVM"

    exit
fi

pToRepo="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
pToSHM=$1
pToJSVM=$2

filesBase=/var/spool/vectors

path=${pToRepo}

mkdir -p ${filesBase}/bin ${filesBase}/scripts ${filesBase}/rec

cp -r ${path}/cfg ${path}/scripts  ${filesBase}/
echo 0 > ${filesBase}/counter
touch ${filesBase}/video_in/rcvdlist.txt

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
