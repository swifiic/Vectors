<?php

    include 'Constants.php';

    $conn = new mysqli($servername, $username, $password, $dbname);
    if ($conn->connect_error) {
        die("Connection failed: " . $conn->connect_error);
    }

    $sql = "SELECT FileName, ReceivedTime FROM ReceivedFiles ORDER BY ReceivedTime DESC LIMIT 100;";

    $result = $conn->query($sql);
    $ack_json->type = "Acknowledgment";
    $ack_json->ack_time = time();
    $ack_json->items = array();
    $item_entry = array();
    if ($result->num_rows > 0) {
        // output data of each row
        while($row = $result->fetch_assoc()) {
            // parsing the rows.
            $item_entry = array( "filename"=>$row["FileName"], "time"=>strtotime($row["ReceivedTime"]));
            $ack_json->items[] = (object) $item_entry;
        }
    }
    echo json_encode($ack_json);

    // $absPath = $fileBase . "/" . $fileToSend;
    // if (file_exists($absPath)) {
    //     header('Content-Description: File Transfer');
    //     header('Content-Type: application/octet-stream');
    //     header('Content-Disposition: attachment; FileName="'.basename($absPath).'"');
    //     header('Expires: 0');
    //     header('Cache-Control: must-revalidate');
    //     header('Pragma: public');
    //     header('Content-Length: ' . filesize($absPath));
    //     readfile($absPath);
    // }

    $conn->close();

 ?>
