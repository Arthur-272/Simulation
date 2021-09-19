#!/bin/bash

filename=$(basename "$1");
output=$filename."msh";

cat > ~/HeadScripts/downloadObject.sh << EOF
#!bin/bash

aws s3 cp $1 /shared
EOF

cat > ~/HeadScripts/uploadObject.sh << EOF
#!bin/bash

aws s3 cp /shared/$output $2
EOF

cat > ~/HeadScripts/simulate.sh << EOF
#!bin/bash

cd /shared && sudo docker run --rm -v \$(pwd):/shared yixinhu/tetwild --ideal-edge-length $3 --input /shared/$filename --output /shared/$output
EOF
