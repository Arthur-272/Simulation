#!/bin/bash

chmod 400 src/main/resources/ssh/server.pem

chmod +x src/main/resources/binaries/draco_decoder

chmod +x src/main/resources/binaries/draco_encoder

chmod +x src/main/resources/binaries/draco_decoder-1.4.1

chmod +x src/main/resources/binaries/draco_encoder-1.4.1

sudo systemctl start mongod

./mvnw -Pprod

