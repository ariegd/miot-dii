<?php
	$file = '/var/tmp/visits.txt';
	$count = 0;

	if (file_exists($file)) {
		$count = (int)file_get_contents($file);
	}
	$count++;

	file_put_contents($file, $count, LOCK_EX);
?>
<!DOCTYPE html>
<html lang="es">
	<head>
	    <title>Diseño de Insfraestructura Inteligente</title>
	</head>
	<style>
		.visits-counter {
		    background-color: #f4f4f4; 
		    color: #333;
		    border-radius: 8px;
		    padding: 10px 15px;
		    
		    /* Layout */
		    display: inline-flex; 
		    align-items: center; 
		    box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
		    font-family: Arial, sans-serif;
		}

		.visits-counter .icon {
		    font-size: 1.2em;
		    margin-right: 8px;
		}

		.visits-counter .label {
		    font-size: 0.8em;
		    font-weight: bold;
		    text-transform: uppercase;
		    margin-right: 12px;
		    color: #5d0202; 
		}

		.visits-counter .count {
		    font-size: 1.6em;
		    font-weight: 900; 
		    background-color: white; 
		    padding: 2px 8px;
		    border-radius: 4px;
		    border: 1px solid #ccc;
		}
	</style>
	<body>
		<div class="visits-counter">
		    <span class="icon">👁️</span>
		    <span class="label">VISITAS</span>
		    <span class="count"><?php echo $count; ?></span>
		</div>

	</body>
</html>
