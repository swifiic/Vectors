#!/bin/sh

binDir=/var/spool/vector/bin
cfgDir=/var/spool/vector/cfg
recDir=/var/spool/vector/rec

video_inDir=/var/www/video_in
video_outDir=/var/www/video_out

counterFile=/var/spool/vector/counter

if [ ! -d "${binDir}" ]; then
  echo "${binDir} doesn't exist";
fi

if [ ! -d "${cfgDir}" ]; then
  echo "${cfgDir} doesn't exist";
fi

if [ ! -d "${recDir}" ]; then
  echo "${recDir} doesn't exist";
fi

if [ ! -d "${video_inDir}" ]; then
  echo "${video_inDir} doesn't exist";
fi

if [ ! -d "${video_outDir}" ]; then
  echo "${video_outDir} doesn't exist";
fi

if [[ ! -f  "${counterFile}" ]]; then
    echo "${counterFile} doesn't exist";
fi
