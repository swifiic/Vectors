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



# this file is a wrapper to pull content and push ack at destination

filesBase=/var/spool/vectors

MY_PATH="`dirname \"$0\"`"

echo -n "Receiver called at " >> ${filesBase}/roRcvr
date >> ${filesBase}/roRcvr

echo "Generating the ACK JSON and moving files from connected device"
bash ${MY_PATH}/txfr_at_dest.sh  >> ${filesBase}/roRcvr 2>&1
echo -n "cron done at " >> ${filesBase}/roRcvr
date >> ${filesBase}/roRcvr
