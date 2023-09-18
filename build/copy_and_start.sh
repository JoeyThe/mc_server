scp mc_server.jar joey@192.168.0.69:/home/joey/mc_server/automation_java/mc_server.jar

ssh -t joey@192.168.0.69 sudo systemctl start mc_server.service
