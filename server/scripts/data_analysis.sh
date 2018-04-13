#!/bin/sh

mkdir -p /var/spool/vector/analysis

time=`date +%Y_%m_%d_%H_%M_%S`

#mv /var/spool/vector/run_out_rcvr /var/spool/vector/run_out_rcvr_${time}

dataFile=data_${time}.txt

cd /var/www/video_in

echo "******************" >> /var/spool/vector/analysis/${dataFile}
echo "******************" >> /var/spool/vector/analysis/${dataFile}
echo "analyis done for the last 24 hrs" >> /var/spool/vector/analysis/${dataFile}
echo "******************" >> /var/spool/vector/analysis/${dataFile}
echo "******************" >> /var/spool/vector/analysis/${dataFile}
value=`find ./ -name "video*.md" -mtime -1 | wc -l`
echo -n no. of md files : $'\t' >> /var/spool/vector/analysis/${dataFile}
echo ${value} >> /var/spool/vector/analysis/${dataFile}
echo "******************" >> /var/spool/vector/analysis/${dataFile}
value=`find ./ -name "video*L0T1.out" -mtime -1 | wc -l`
echo -n no. of L0T1 files : $'\t' >> /var/spool/vector/analysis/${dataFile}
echo ${value} >> /var/spool/vector/analysis/${dataFile}
echo "******************" >> /var/spool/vector/analysis/${dataFile}
value=`find ./ -name "video*L0T2.out" -mtime -1 | wc -l`
echo -n no. of L0T2 files : $'\t' >> /var/spool/vector/analysis/${dataFile}
echo ${value} >> /var/spool/vector/analysis/${dataFile}
echo "******************" >> /var/spool/vector/analysis/${dataFile}
value=`find ./ -name "video*L0T3.out" -mtime -1 | wc -l`
echo -n no. of L0T3 files : $'\t' >> /var/spool/vector/analysis/${dataFile}
echo ${value} >> /var/spool/vector/analysis/${dataFile}
echo "******************" >> /var/spool/vector/analysis/${dataFile}
value=`find ./ -name "video*L0T4.out" -mtime -1 | wc -l`
echo -n no. of L0T4 files : $'\t' >> /var/spool/vector/analysis/${dataFile}
echo ${value} >> /var/spool/vector/analysis/${dataFile}
echo "******************" >> /var/spool/vector/analysis/${dataFile}
value=`find ./ -name "video*L0T5.out" -mtime -1 | wc -l`
echo -n no. of L0T5 files : $'\t' >> /var/spool/vector/analysis/${dataFile}
echo ${value} >> /var/spool/vector/analysis/${dataFile}
echo "******************" >> /var/spool/vector/analysis/${dataFile}
value=`find ./ -name "video*L1T1.out" -mtime -1 | wc -l`
echo -n no. of L1T1 files : $'\t' >> /var/spool/vector/analysis/${dataFile}
echo ${value} >> /var/spool/vector/analysis/${dataFile}
echo "******************" >> /var/spool/vector/analysis/${dataFile}
value=`find ./ -name "video*L1T2.out" -mtime -1 | wc -l`
echo -n no. of L1T2 files : $'\t' >> /var/spool/vector/analysis/${dataFile}
echo ${value} >> /var/spool/vector/analysis/${dataFile}
echo "******************" >> /var/spool/vector/analysis/${dataFile}
value=`find ./ -name "video*L1T3.out" -mtime -1 | wc -l`
echo -n no. of L1T3 files : $'\t' >> /var/spool/vector/analysis/${dataFile}
echo ${value} >> /var/spool/vector/analysis/${dataFile}
echo "******************" >> /var/spool/vector/analysis/${dataFile}
value=`find ./ -name "video*L1T4.out" -mtime -1 | wc -l`
echo -n no. of L1T4 files : $'\t' >> /var/spool/vector/analysis/${dataFile}
echo ${value} >> /var/spool/vector/analysis/${dataFile}
echo "******************" >> /var/spool/vector/analysis/${dataFile}
value=`find ./ -name "video*L1T5.out" -mtime -1 | wc -l`
echo -n no. of L1T5 files : $'\t' >> /var/spool/vector/analysis/${dataFile}
echo ${value} >> /var/spool/vector/analysis/${dataFile}
echo "******************" >> /var/spool/vector/analysis/${dataFile}
