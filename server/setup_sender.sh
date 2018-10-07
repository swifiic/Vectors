# sh scripts/install_shvc.sh
# (crontab -l ; echo "*/10 * * * * scripts/src_cron_ack_pull.sh")| crontab -

echo; read -p "Default webcam suffix to be used; enter X in /dev/videoX (e.g. enter '1' for '/dev/video1'): " webcam_suffix
echo; read -p "Frequency of capture interval (minutes): " cron_time
echo; read -p "Time between frames (seconds): " frame_time
echo; read -p "TTL of video chunk files (hours): " ttl_time
config_file="scripts/config"
rm $config_file
touch $config_file
echo "webcam_suffix="$webcam_suffix >> $config_file
echo "cron_time="$cron_time >> $config_file
echo "frame_time="$frame_time >> $config_file
echo "ttl_time="$ttl_time >> $config_file

# * resolution
#     * check for V4L2 webcam
#     * which webcam to use by default?
#     * test
# * time for cronjob
# * number of frames per job
# * 4 secs between frames
# * ttl         
# * download and compile SHVC
# * (deployment does not adjust to number of nodes in network)
