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
binDir=${filesBase}/bin
cfgDir=${filesBase}/cfg
recDir=${filesBase}/rec

video_inDir=${filesBase}/import
video_outDir=${filesBase}/video_out

counterFile=${filesBase}/counter

if [ ! -d "${binDir}" ]; then
  echo "${binDir} doesn't exist";
else
  cd ${binDir}

  if [ -x "TAppEncoderStaticd" ] && [ -x "ExtractAddLS" ] && [ -x "DownConvertStaticd" ] ; then
     echo "Binaries present and executable"
  else 
     echo "Binaries missing or non-executable [TAppEncoderStaticd / ExtractAddLS / DownConvertStaticd ]"
  fi
fi

if [ ! -d "${cfgDir}" ]; then
  echo "${cfgDir} doesn't exist";
else
  #1L-1X_vectors.cfg 2L-2X_vectors.cfg  4L-2X_vectors.cfg  raScale.cfg  layers2_final.cfg  layers4_final.cfg
  cd ${cfgDir}
  if [ -f "1L-1X_vectors.cfg" ] && [ -f "2L-2X_vectors.cfg" ] && [ -f "4L-2X_vectors.cfg" ] && [ -f "raScale.cfg" ] && \
		[ -f "layers1.cfg" ] && [ -f "layers2.cfg" ] && [ -f "layers4.cfg" ] ; then
     echo "Config files present"
  else
     echo "one of 1L-1X_vectors.cfg 2L-2X_vectors.cfg  4L-2X_vectors.cfg  raScale.cfg layers1.cfg  layers2.cfg  layers4.cfg missing"
  fi
  cd -
fi



if [ ! -w "${recDir}" ]; then
  echo "${recDir} doesn't exist or not writeable";
fi

if [ ! -d "${video_inDir}" ]; then
  echo "${video_inDir} doesn't exist or not writeable";
fi

if [ ! -d "${video_outDir}" ]; then
  echo "${video_outDir} doesn't exist or not writeable";
fi

if [[ ! -f  "${counterFile}" ]]; then
    echo "${counterFile} doesn't exist or not writeable";
fi
