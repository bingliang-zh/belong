<?php
$fp = fopen("/sys/class/thermal/thermal_zone0/temp","r");
$tem = (int)fgets($fp);
fclose($fp);
$arr = array('name'=>"temperature",'var'=>$tem);
echo json_encode($arr);
?>