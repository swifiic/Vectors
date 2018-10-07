svn export https://hevc.hhi.fraunhofer.de/svn/svn_SHVCSoftware/tags/SHM-12.4/ SHM-12.4
cd SHM-12.4/build/linux
make
cd ../../..
git clone https://github.com/floriandejonckheere/jsvm.git
cd jsvm/JSVM/H264Extension/build/linux
make
