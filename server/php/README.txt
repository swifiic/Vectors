*************************
Setting up the php server
*************************
The apache server in our case is run at /var/www/php location.
To configure the server to run at a different location.

we need to edit the following files:
- /etc/apache2/sites-enabled/000-default.conf
- /etc/apache2/apache2.conf

Edit the DocumentRoot option in both the files:
Change it to /var/www/php/ from /var/www/html/

<DocumentRoot /var/www/php/> (in our case)

Then restart the apache server:

sudo service apache2 restart

******************************
Files Description
******************************
Constants.php        - constants for DB access and copy counts etc.

# for Upload and ack after upload
FilterUploadList.php - Tells which files can be uloaded
PostFiles.php        - Allows file Upload
GetAck.php           - After each file is uploaded we GetAck

# for Download and fetch copy count after download
GetFile.php          - Accepts a Comma Serated list of files from device and downloads file which is missing on device
NewFileCheck.php     - Helper for GetFile.php - merges the folder content with DB
GetCopyCount.php     - After each file is downloaded we get CopyCount for it

README.txt           - This file

************************
Tables in the DB
************************

GRANT ALL PRIVILEGES ON *.* TO 'svc'@'localhost' IDENTIFIED BY 'svc123';

create database SVC;
use SVC;

-- AvailableFiles---
CREATE TABLE `AvailableFiles` (
 `FileName` varchar(255) NOT NULL,
 `CopyCount` int(11) NOT NULL,
 PRIMARY KEY (`FileName`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- DroppedFiles---
CREATE TABLE `DroppedFiles` (
 `FileName` varchar(255) NOT NULL,
 `DeleteTime` datetime NOT NULL,
 `DeleteReason` varchar(31) NOT NULL,
 `CopyCountAtDelete` int(11) NOT NULL,
 PRIMARY KEY (`FileName`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- ReceivedFiles---
CREATE TABLE `ReceivedFiles` (
 `FileName` varchar(255) NOT NULL,
 `ReceivedTime` datetime NOT NULL,
 PRIMARY KEY (`FileName`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


