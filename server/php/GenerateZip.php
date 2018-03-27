<?php

$zip = new ZipArchive;
if ($zip->open($zipFolder, ZipArchive::OVERWRITE) === TRUE)
{
    for ($x = 0; $x <= ($copyCount/2); $x++) {
        $toProcess = explode(".",$fileName);
        $toProcess[0] .= ("_" . $x);
        $destinationFile = implode($toProcess);
        $zip->addFile($fileBase . "/" . $fileName, $destinationFile);
    }

    $zip->close();
}

?>
