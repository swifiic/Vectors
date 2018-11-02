# sh scripts/install_shvc.sh
# (crontab -l ; echo "*/10 * * * * scripts/src_cron_ack_pull.sh")| crontab -

sh setup_asroot.sh
sh install_shvc.sh
sudo bash setup.sh $PWD/shm $PWD/jsvm/JSVM
echo; read -p "Default webcam suffix to be used; enter X in /dev/videoX (e.g. enter '1' for '/dev/video1'): " webcam_suffix
echo; read -p "Frequency of the capture cron job (minutes): " cron_time
echo; read -p "Framerate (seconds): " frame_rate
echo; read -p "TTL of video chunk files (hours): " ttl_time
echo; read -p "Frames per job: " frame_number
echo; read -p "Video quality (l/m): " vid_quality
echo; read -p "Source node (last 4 digits): " src_node
echo; read -p "Destination node (last 4 digits):" dest_node

webcam_suffix="${webcam_suffix:-5}"
cron_time="${cron_time:-10}"
frame_rate="${frame_rate:-0.25}"
ttl_time="${ttl_time:-24}"
frame_number="${frame_number:-65}"
vid_quality="${vid_quality:-"l"}"
src_node="${src_node:-"aaaa"}"
dest_node="${dest_node:-"bbbb"}"


if  [ "$vid_quality" != "l" -a "$vid_quality" != "m" ]; then
    vid_quality="l"
fi
webcam_dev="/dev/video"$webcam_suffix
config_file="config"
rm $config_file
touch $config_file
ttl_time=$((ttl_time*3600))
echo "webcam_dev="$webcam_dev >> $config_file
echo "cron_time="$cron_time >> $config_file
echo "frame_rate="$frame_rate >> $config_file
echo "ttl_time="$ttl_time >> $config_file
echo "frame_number="$frame_number >> $config_file
echo "vid_quality="$vid_quality >> $config_file
echo "src_node="$src_node >> $config_file
echo "dest_node="$dest_node >> $config_file

cron_pull_path="$PWD"/src_cron_ack_pull.sh  
cron_vid_path="$PWD"/src_cron_video.sh  

(crontab -l ; echo "*/$cron_time * * * * $cron_pull_path")| crontab -
(crontab -l ; echo "*/$cron_time * * * * $cron_vid_path")| crontab -

# ./setup.sh shm jsvm/JSVM
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
