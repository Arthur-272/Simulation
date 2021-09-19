#!/bin/bash

#sudo yum update -y

#sudo yum install docker -y
#sudo service docker start
#sudo docker pull yixinhu/tetwild
sudo aws s3 cp s3://pointbasisofficial/FloatTetwild_bin /shared && sudo chmod +x /shared/FloatTetwild_bin

