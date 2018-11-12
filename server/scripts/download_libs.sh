svn export https://hevc.hhi.fraunhofer.de/svn/svn_SHVCSoftware/tags/SHM-12.4/ shm || echo "Could not download SHM" && exit
# cd SHM-12.4/build/linux
# make
# cd ../../..
git clone https://github.com/floriandejonckheere/jsvm.git jsvm
# cd jsvm/JSVM/H264Extension/build/linux
# make
