<?php

    include 'Constants.php';

    $conn = new mysqli($servername, $username, $password, $dbname);

    // // Check connection
    if ($conn->connect_error) {
        die("Connection failed: " . $conn->connect_error);
    }
    $fileArgs = $_GET['FileName'];

    $sql = "SELECT CopyCount FROM AvailableFiles WHERE FileName ='${fileArgs}' LIMIT 1;";

    $result = $conn->query($sql);
    if ($conn->query($sql) != TRUE) {
        echo "Error selecting record: " . $sql . " " . $conn->error;
    }
    $row = $result->fetch_assoc();

    $currentCopyCount = $row['CopyCount'];
    $jsonToSend= array('filename' => ${fileArgs},'copycount' => ${currentCopyCount});
    $jsonEncode = json_encode(${jsonToSend});

    // updating the table with the new copy count.
    $newCopyCount = ${currentCopyCount}/2;
    if (${newCopyCount} >= 1) {
        $sql = "UPDATE AvailableFiles SET CopyCount='${newCopyCount}' WHERE FileName='${fileArgs}' ;";
        if ($conn->query($sql) != TRUE) {
            echo "Error updating record: " . $conn->error;
        }

        header('Content-Type: application/json');
        echo $jsonEncode;
    }else{

        $sql = "UPDATE AvailableFiles SET CopyCount=0 WHERE FileName='${fileArgs}' ;";
        if ($conn->query($sql) != TRUE) {
            echo "Error updating record: " . $conn->error;
        }

        // delete the file if the copycount is 0
        $sql = "SELECT FileName FROM AvailableFiles WHERE CopyCount=0";
        $result = $conn->query($sql);

        if ($result->num_rows > 0) {

            $date = date('Y-m-d H:i:s');
            while($row = $result->fetch_assoc()) {
                $sql = "INSERT INTO DroppedFiles (FileName, DeleteTime, DeleteReason, CopyCountAtDelete) VALUES ('${row['FileName']}', '${date}', 'Copy Count 0', 0); ";
                if ($conn->query($sql) != TRUE) {
                     echo "Error: " . $sql . "<br>" . $conn->error;
                }

                // TODO : Need to delete the files from the folder whose copy count is 0.

                // if (!unlink($fileBase . ${row['FileName']})){
                //     echo ("Error deleting " . $fileBase . ${row['FileName']});
                // }
            }

            $sql = "DELETE FROM AvailableFiles WHERE CopyCount=0";
            if ($conn->query($sql) != TRUE) {
                echo "Error deleting record: " . $conn->error;
            }
        }
    }

    $conn->close();
?>
