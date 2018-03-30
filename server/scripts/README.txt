The source and destination scripts to generate the video bursts and compute / decode received videos.

**************
Initial Setup
**************

Download JSVM and SHM code and unzip to appropriate folder

First runs setup_asroot.sh with sudo ; e.g.
aperture@aperture:~/roamnet/server/scripts$ sudo bash setup_asroot.sh /home/aperture/roamnet/server

Then run setup.sh ; e.g.
aperture@aperture:~/roamnet/server/scripts$ bash setup.sh /home/aperture/roamnet/server /home/aperture/Downloads/SHM-12.4 /home/aperture/Downloads/JSVM-master/JSVM



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
The binaries should be copied into /var/spool/vector/bin
The CFG files should be copied into /var/spool/vector/cfg
The recorded samples are stored in /var/spool/vector/rec
There is a file named counter which takes care of the sample sequence number. It is located at /var/spool/vector/counter
