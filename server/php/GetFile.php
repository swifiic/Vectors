<?php

    include 'Constants.php';

    global $fileBase;
    global $copyCounts;

    $existingFilesString = $_GET['FilesList'];

    $existingFilesList = explode(",",$existingFilesString);

    // Create connection
    $conn = new mysqli($servername, $username, $password, $dbname);
    if ($conn->connect_error) {
        die("Connection failed: " . $conn->connect_error);
    }
    global $conn;

    // check for new file and insert them into DB if any.
    include 'NewFileCheck.php';

    $sql = "SELECT FileName, CopyCount FROM AvailableFiles ORDER BY CopyCount DESC, FileName DESC;";

    $result = $conn->query($sql);
    $fileToSend = "";
    if ($result->num_rows > 0) {
        // output data of each row
        while($row = $result->fetch_assoc()) {
            if(!(in_array($row["FileName"],$existingFilesList))){
                $fileToSend = $row["FileName"];
                break;
            }
        }
    }


    $absPath = $fileBase . "/" . $fileToSend;
    if (file_exists($absPath)) {
        header('Content-Description: File Transfer');
        header('Content-Type: application/octet-stream');
        header('Content-Disposition: attachment; FileName="'.basename($absPath).'"');
        header('Expires: 0');
        header('Cache-Control: must-revalidate');
        header('Pragma: public');
        header('Content-Length: ' . filesize($absPath));
        readfile($absPath);
    }

    $conn->close();
?>
