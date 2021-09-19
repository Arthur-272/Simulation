#!/bin/bash

bash ~/HeadScripts/installUtilities.sh
bash ~/HeadScripts/downloadObject.sh
#bash ~/HeadScripts/simulate.sh
sbatch ~/HeadScripts/submission.sbatch > job.txt
#bash ~/HeadScripts/uploadObject.sh
