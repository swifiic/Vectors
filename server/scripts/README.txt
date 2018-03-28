The source and destination scripts to generate the video bursts and compute / decode received videos.

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
