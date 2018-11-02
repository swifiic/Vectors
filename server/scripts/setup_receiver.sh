sh setup_asroot.sh
sh download_libs.sh
sudo bash setup.sh $PWD/shm $PWD/jsvm/JSVM

(crontab -l ; echo "*/10 * * * * "$PWD"/rcvr_cron.sh")| crontab -