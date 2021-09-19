#!/bin/bash
#SBATCH --job-name=Meshing
#SBATCH --ntasks=1
#SBATCH --output=%x_%j.out
module load intelmpi
mpirun sudo /shared/FloatTetwild_bin --lr 0.05 --input /shared/609a7b775590d2231c6abe3e/609a7ba55590d2231c6abe41/inputFiles/747-400.stl --output /shared/609a7b775590d2231c6abe3e/609a7ba55590d2231c6abe41/outputFiles/747-400.msh

