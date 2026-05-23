import os
import glob

files = glob.glob('/Users/gabrielcarauleanu/Desktop/CLM-platform/**/Dockerfile*', recursive=True)

for file in files:
    with open(file, 'r') as f:
        lines = f.readlines()
    
    with open(file, 'w') as f:
        for line in lines:
            if 'ARG CACHE_BUSTER' in line or 'RUN echo "$CACHE_BUSTER" > /dev/null' in line:
                continue
            f.write(line)
print("Finished removing CACHE_BUSTER from Dockerfiles.")
