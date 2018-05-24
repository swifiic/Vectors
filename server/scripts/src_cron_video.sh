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
#                                                                          #
#    Change video_record_2L.sh to video_record_1L.sh if needed             #
############################################################################


# this file is a wrapper around 1L, 2L or 4L script for SHM
filesBase=/var/spool/vectors

layerFileScript="video_record_2L.sh" # video_record_1L.sh

MY_PATH="`dirname \"$0\"`"
video_file_counter=`cat ${filesBase}/counter`
counterPart=`printf "%05d" ${video_file_counter}`



echo "$MY_PATH" >> ${filesBase}/ro_${counterPart}
echo -n "${video_file_counter} at " >> ${filesBase}/ro
date >> ${filesBase}/ro

bash ${MY_PATH}/${layerFileScript} >> ${filesBase}/ro_${counterPart} 2>&1

echo "Generating the JSON and moving files to connected device"
bash  -x ${MY_PATH}/push_at_src.sh ${video_file_counter} >> ${filesBase}/ro_${counterPart} 2>&1

echo -n "${video_file_counter} completed at " >> ${filesBase}/ro
date >> ${filesBase}/ro

