<?php
$id = $_GET['id'];
$mode = $_GET['mode'];
@$voltage = $_GET['voltage'];
if(empty($voltage)) $file = system("sudo python gpio_set.py $id $mode");
else $file = system("sudo python gpio_set.py $id $mode $voltage");
?>