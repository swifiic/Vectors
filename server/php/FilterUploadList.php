<?php

    include 'Constants.php';

    $conn = new mysqli($servername, $username, $password, $dbname);

    // // Check connection
    if ($conn->connect_error) {
        die("Connection failed: " . $conn->connect_error);
    }

    $filesListArgs = $_GET['FilesList'];

    $filesList = explode(',',$filesListArgs);

    for ($i=0; $i < count($filesList); $i++) {
        $sql = "SELECT FileName FROM ReceivedFiles WHERE FileName = '${filesList[$i]}';";
        if ($conn->query($sql) != TRUE) {
            echo "Error selecting record: " . $sql . " " . $conn->error;
        }
        $result = $conn->query($sql);
        if(!($result->num_rows == 0)){
            unset($filesList[$i]);
        }
    }

    $finalUploadList = implode(',',$filesList);

    echo $finalUploadList;

    $conn->close();
?>
