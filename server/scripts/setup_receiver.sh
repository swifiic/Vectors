sh install_shvc.sh
(crontab -l ; echo "*/10 * * * * "$PWD"/rcvr_cron.sh")| crontab -