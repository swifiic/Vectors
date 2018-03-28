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
