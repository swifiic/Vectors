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


filesBase=/var/spool/vectors

mkdir -p ${filesBase}/analysis

time=`date +%Y-%m-%d_%H:%M:%S`

dataFile=data_${time}.txt

cd ${filesBase}/video_in

echo "******************" >> ${filesBase}/analysis/${dataFile}
echo "******************" >> ${filesBase}/analysis/${dataFile}
echo "analyis done for the last 24 hrs" >> ${filesBase}/analysis/${dataFile}
echo "******************" >> ${filesBase}/analysis/${dataFile}
echo "******************" >> ${filesBase}/analysis/${dataFile}
value=`find ./ -name "video*.md" -mtime -1 | wc -l`
echo -n no. of md files : '\t' >> ${filesBase}/analysis/${dataFile}
echo ${value} >> ${filesBase}/analysis/${dataFile}
echo "******************" >> ${filesBase}/analysis/${dataFile}
value=`find ./ -name "video*L0T1.out" -mtime -1 | wc -l`
echo -n no. of L0T1 files : '\t' >> ${filesBase}/analysis/${dataFile}
echo ${value} >> ${filesBase}/analysis/${dataFile}
echo "******************" >> ${filesBase}/analysis/${dataFile}
value=`find ./ -name "video*L0T2.out" -mtime -1 | wc -l`
echo -n no. of L0T2 files : '\t' >> ${filesBase}/analysis/${dataFile}
echo ${value} >> ${filesBase}/analysis/${dataFile}
echo "******************" >> ${filesBase}/analysis/${dataFile}
value=`find ./ -name "video*L0T3.out" -mtime -1 | wc -l`
echo -n no. of L0T3 files : '\t' >> ${filesBase}/analysis/${dataFile}
echo ${value} >> ${filesBase}/analysis/${dataFile}
echo "******************" >> ${filesBase}/analysis/${dataFile}
value=`find ./ -name "video*L0T4.out" -mtime -1 | wc -l`
echo -n no. of L0T4 files : '\t' >> ${filesBase}/analysis/${dataFile}
echo ${value} >> ${filesBase}/analysis/${dataFile}
echo "******************" >> ${filesBase}/analysis/${dataFile}
value=`find ./ -name "video*L0T5.out" -mtime -1 | wc -l`
echo -n no. of L0T5 files : '\t' >> ${filesBase}/analysis/${dataFile}
echo ${value} >> ${filesBase}/analysis/${dataFile}
echo "******************" >> ${filesBase}/analysis/${dataFile}
value=`find ./ -name "video*L1T1.out" -mtime -1 | wc -l`
echo -n no. of L1T1 files : '\t' >> ${filesBase}/analysis/${dataFile}
echo ${value} >> ${filesBase}/analysis/${dataFile}
echo "******************" >> ${filesBase}/analysis/${dataFile}
value=`find ./ -name "video*L1T2.out" -mtime -1 | wc -l`
echo -n no. of L1T2 files : '\t' >> ${filesBase}/analysis/${dataFile}
echo ${value} >> ${filesBase}/analysis/${dataFile}
echo "******************" >> ${filesBase}/analysis/${dataFile}
value=`find ./ -name "video*L1T3.out" -mtime -1 | wc -l`
echo -n no. of L1T3 files : '\t' >> ${filesBase}/analysis/${dataFile}
echo ${value} >> ${filesBase}/analysis/${dataFile}
echo "******************" >> ${filesBase}/analysis/${dataFile}
value=`find ./ -name "video*L1T4.out" -mtime -1 | wc -l`
echo -n no. of L1T4 files : '\t' >> ${filesBase}/analysis/${dataFile}
echo ${value} >> ${filesBase}/analysis/${dataFile}
echo "******************" >> ${filesBase}/analysis/${dataFile}
value=`find ./ -name "video*L1T5.out" -mtime -1 | wc -l`
echo -n no. of L1T5 files : '\t' >> ${filesBase}/analysis/${dataFile}
echo ${value} >> ${filesBase}/analysis/${dataFile}
echo "******************" >> ${filesBase}/analysis/${dataFile}
