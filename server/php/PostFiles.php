<?php

    include 'Constants.php';

    //connecting to database
    $conn = new mysqli($servername,$username,$password,$dbname) or die('Unable to connect');
    $value = print_r($_FILES,TRUE);
    error_log("Files value " . $value);
    // Loop through each file
      $tmpFilePath = $_FILES['upload']['tmp_name'];

      //Make sure we have a filepath
      if ($tmpFilePath != ""){
        //Setup our new file path
        $newFilePath = $receivedFiles . "/" . $_FILES['upload']['name'];

        //Upload the file into the temp dir
        if(move_uploaded_file($tmpFilePath, $newFilePath)) {
            error_log("File copies to " . ${newFilePath});
            $date = date('Y-m-d H:i:s');
            $clientName = $_FILES['upload']['name'];
            $sql = "INSERT INTO ReceivedFiles (FileName, ReceivedTime) VALUES ('${clientName}', '${date}'); ";
            if ($conn->query($sql) != TRUE) {
                 echo "Error: " . $sql . "<br>" . $conn->error;
            }
          }
          else {
              error_log("Failed to copy File to " . ${newFilePath});
          }

    }
    $conn->close();
?>
