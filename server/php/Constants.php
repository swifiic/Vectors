<?php

    $servername = "localhost";
    $username = "svc";
    $password = "svc123";
    $dbname = "SVC";

    $fileBase = "/var/www/video_out";

    $receivedFiles = "/var/www/video_in";

    $copyCounts=["md" => 32, "L0T1" => 32, "L0T2" => 16, "L0T3" => 16,
                "L0T4" => 8, "L0T5" => 8, "L0T6" => 8, "L1" => 6, "L2" => 5,
                "L3" => 4, "L4" => 3];

    global $servername;
    global $username;
    global $password;
    global $dbname;
    global $fileBase;
    global $copyCounts;
    global $receivedFiles;

?>
