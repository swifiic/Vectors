sh install_shvc.sh
(crontab -l ; echo "*/10 * * * * scripts/src_cron_ack_pull.sh")| crontab -