#!/bin/sh

binDir=/var/spool/vector/bin
cfgDir=/var/spool/vector/cfg
recDir=/var/spool/vector/rec

video_inDir=/var/www/video_in
video_outDir=/var/www/video_out

counterFile=/var/spool/vector/counter

if [ ! -d "${binDir}" ]; then
  echo "${binDir} doesn't exist";
elif
  cd ${binDir}

  if [ -x "TAppEncoderStaticd" && -x "ExtractAddLS" && -x "DownConvertStaticd" ] ; then
     echo "Binaries present and executable"
  else 
     echo "Binaries missing or non-executable [TAppEncoderStaticd / ExtractAddLS / DownConvertStaticd ]"
  fi
fi

if [ ! -d "${cfgDir}" ]; then
  echo "${cfgDir} doesn't exist";
elif
  #2L-2X_vector.cfg  4L-2X_vector.cfg  encoder_randomaccess_scalable.cfg  layers2_final.cfg  layers4_final.cfg
  cd ${cfgDir}
  if [ -f "2L-2X_vector.cfg" && -f "4L-2X_vector.cfg" && -f "encoder_randomaccess_scalable.cfg" && -f "layers2_final.cfg" && -f "layers4_final.cfg" ] ; then
     echo "Config files present"
  else
     echo "one of 2L-2X_vector.cfg  4L-2X_vector.cfg  encoder_randomaccess_scalable.cfg  layers2_final.cfg  layers4_final.cfg missing"
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
