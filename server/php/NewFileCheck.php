<?php

    $filesInFolder = array_diff(scandir($fileBase), array('.', '..'));
    $sql = "SELECT FileName FROM AvailableFiles";
    $result = $conn->query($sql);

    $filesInDB = array();

    while ($row = $result->fetch_assoc()) {
        array_push($filesInDB,$row["FileName"]);
    }

    $filesArray=array_values($filesInFolder);
    // adding new files to DB
    for($x = 0; $x < count($filesArray); $x++) {
        if(in_array($filesArray[$x], $filesInDB)) continue;
        $toProcess = $filesArray[$x];
        $copyCountAtDelete = 0;
        // $fileNameExtract = explode("_", $toProcess);
        $currentCount = 2;
        if (strpos($toProcess,".md"))
            $currentCount = $copyCounts["md"];
        else if (strpos($toProcess,"L0T1"))
            $currentCount = $copyCounts["L0T1"];
        else if (strpos($toProcess,"L0T2"))
            $currentCount = $copyCounts["L0T2"];
        else if (strpos($toProcess,"L0T3"))
            $currentCount = $copyCounts["L0T3"];
        else if (strpos($toProcess,"L0T4"))
            $currentCount = $copyCounts["L0T4"];
        else if (strpos($toProcess,"L0T5"))
            $currentCount = $copyCounts["L0T5"];
        else if (strpos($toProcess,"L0T6"))
            $currentCount = $copyCounts["L0T6"];
        else if (strpos($toProcess,"L1"))
            $currentCount = $copyCounts["L1"];
        else if (strpos($toProcess,"L2"))
            $currentCount = $copyCounts["L2"];
        else if (strpos($toProcess,"L3"))
            $currentCount = $copyCounts["L3"];
        else if (strpos($toProcess,"L4"))
            $currentCount = $copyCounts["L4"];
        else
            echo "File ${toProcess} doesn't contain Layer or Temporal ID";


        $sql = "INSERT INTO AvailableFiles (FileName, CopyCount) VALUES ('$toProcess', '$currentCount')";
        if ($conn->query($sql) != TRUE) {
                 echo "Error: " . $sql . "<br>" . $conn->error;
        }
    }


    for($x = 0; $x < count($filesInDB); $x++) {
        if(in_array($filesInDB[$x], $filesArray))
             continue;
        $toProcess = $filesInDB[$x];
        error_log("Dropping File not found " . $toProcess);
        $sql = "SELECT CopyCount FROM AvailableFiles WHERE FileName='$toProcess'";
        if ($result = $conn->query($sql) ) {
            $row = $result->fetch_assoc();
        } else  {
             error_log( "Error: " . $sql . "<br>" . $conn->error);
             continue;
        }
        $copyCountAtDelete = $row['CopyCount'];

        $sql = "DELETE FROM AvailableFiles WHERE FileName='$toProcess'";
        if ($conn->query($sql) != TRUE) {
                error_log( "Error deleting record: " . $sql . " " . $conn->error);
        }

        $sql = "INSERT INTO DroppedFiles (FileName, DeleteTime, DeleteReason, CopyCountAtDelete) VALUES ('$toProcess', NOW(), 'unknown/external', '$copyCountAtDelete')";
        if ($conn->query($sql) != TRUE) {
             error_log( "Error in insert for DroppedFiles: " . $sql . "<br>" . $conn->error);
        }
    }
?>
