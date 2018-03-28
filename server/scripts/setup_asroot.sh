#!/bin/sh

if [ "$#" -ne 1 ]; then
    echo "Usage: setup.sh /path/to/gitrepo"
fi

if [ "$EUID" -ne 0 ]
  then echo "Please run as root"
  exit
fi

$pToRepo=$1

mkdir -p /var/spool/vector/
chmod -R 777 /var/spool/vector/
cp -r ${pToRepo}/php /var/www/
