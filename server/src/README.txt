All code for SHM and related tools that are modified to be put here

************************
Download the source code
************************
The following command will download the svn repo into the folder named SHM-12.4.

svn export https://hevc.hhi.fraunhofer.de/svn/svn_SHVCSoftware/tags/SHM-12.4/ SHM-12.4

************************
Building source code
************************
cd /path/to/SHM-12.4/build/linux
make


We also use DownConvertStatic binary in JSVM to down scale the recorded sample.
To download and build the JSVM binaries.

************************
Download the source code
************************
The following command will download the svn repo into the folder named SHM-12.4.

git clone https://github.com/floriandejonckheere/jsvm.git

************************
Building source code
************************
cd /path/to/JSVM/H264Extension/build/linux
make
