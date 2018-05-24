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



# this file pulls the latest ack from devices and updates if it is newer than last ack

MY_PATH="`dirname \"$0\"`"
file_counter=`date +"%d_%H_%M_%S"`

/usr/bin/adb pull /sdcard/VectorsData/ack_video_00.json /tmp/${file_counter}


value=`cat /tmp/${file_counter} | grep  traversal | awk -F'traversal":' '{ print $2 }'`
echo "${file_counter} ${value}" >> ${MY_PATH}/../ack_received.txt

if [[ -e /tmp/last_unique ]] ; then 
    diffVal=`diff /tmp/${file_counter} /tmp/last_unique`
    if [[ -z "$diffVal" ]] ; then
       rm /tmp/${file_counter}
    else 
       cp -p /tmp/${file_counter} /tmp/last_unique
    fi
else 
   cp -p /tmp/${file_counter} /tmp/last_unique
fi


