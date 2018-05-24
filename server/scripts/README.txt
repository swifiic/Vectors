The source and destination scripts to generate the video bursts and compute / decode received videos.

**************
Initial Setup
**************

Download JSVM and SHM code and unzip to appropriate folder

First runs setup_asroot.sh with sudo ; e.g.
aperture@aperture:~/vectors/server/scripts$ sudo bash setup_asroot.sh /home/aperture/vectors/server

Then run setup.sh ; e.g.
aperture@aperture:~/vectors/server/scripts$ bash setup.sh /home/aperture/vectors/server /home/aperture/Downloads/SHM-12.4 /home/aperture/Downloads/JSVM-master/JSVM

************************
Running cron job.
************************
Add new cron job to crontab:
crontab –e

An example command would be “0 0 * * * /path/to/script.sh”. This
would mean that the shell script will exactly execute at midnight every
night.

To save the changes to the crontab that you just made, hit ESC key, and
then type :w followed by :q to exit.

To list existing cron jobs:
    crontab –l

To remove an existing cron job:
    crontab -r

************************
Binaries and CFG files
************************
The binaries are copied into /var/spool/vectors/bin
The CFG files are copied into /var/spool/vectors/cfg
The recorded samples are stored in /var/spool/vectors/rec
There is a file named counter which takes care of the sample sequence number. It is located at /var/spool/vectors/counter

***********************
Details of sript files
***********************
setup_asroot.sh - first file to be run for installation with sudo to create the required folders
setup.sh        - main file that installs the scripts and config

checkDirs.sh  - verifies permissions etc. after installation is done by setup_asroot.sh

data_analysis.sh - for trend analysis - generally not required during setup and run

src_cron_ack_pull.sh  - script that is scheduled using cron to pull the acks from the device at Source
src_cron_video.sh     - script that pushes the payloads by calling push_at_src.sh
  video_record_1L.sh  - called by src_cron_video.sh to generate the video with 1 spatial layer
  video_record_2L.sh  - called by src_cron_video.sh to generate the video with 2 spatial layers
  push_at_src.sh      - called by src_cron_video.sh to push content to device


rcvr_cron.sh          - script scheduled on destination / sink / receiver to pull out the video bundles
  txfr_at_dest.sh     - called by rcvr_cron.sh to pull files and generate the cumulative ack

video_combiner.sh     - at destination to create video using the received payloads and metadata from extraction
