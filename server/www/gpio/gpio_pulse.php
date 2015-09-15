<?php
$id = $_GET['id'];
@$frequency = $_GET['frequency'];
@$lag = $_GET['lag'];
if(empty($frequency))
	$file = system("sudo python gpio_pulse.py $id");
else
	if(empty($lag))
		$file = system("sudo python gpio_pulse.py $id $frequency");
	else
		$file = system("sudo python gpio_pulse.py $id $frequency $lag");
?>