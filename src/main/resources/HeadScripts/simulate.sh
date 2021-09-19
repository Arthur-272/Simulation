#!/bin/bash
cd /shared && sudo docker run --rm -v $(pwd):/shared yixinhu/tetwild --ideal-edge-length 1 --input /shared/MengerSponge.stl --output /shared/MengerSponge.msh

