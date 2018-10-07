sh scripts/install_shvc.sh
(crontab -l ; echo "*/10 * * * * scripts/rcvr_cron.sh")| crontab -