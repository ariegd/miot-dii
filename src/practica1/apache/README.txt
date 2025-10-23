Máster IoT, curso 25-26
├──Diseño de Insfraestructura Inteligente IoT
└── Fernando Carlos López Hernández <fclh@ucm.es>
 	└── Practica 1: Contador de visitas/ Autores
		├── Ariel Gámez Díaz <arielg01@ucm.es>
 		└── Jose Villacres Zumaeta <jvillacr@ucm.es>

# Pasos para crear el proyecto
-------------------------------------
Indice
├── 1) Crear directorios, archivos y variables de entorno
├── 2) Crear imagen y contenedor con volumen compartido
├── 3) Abrir la consola del contenedor y dar permiso a /var/tmp
└── 4) Verificar dentro del contenedor, parar y borrar

# 1) Crear directorios, archivos y variables de entorno

~$ mv visits.txt /var/tmp | mv index.php /var/www/html
~$ export HOST_DATA=/var/tmp
~$ export HOST_SITE=/var/www/html
~$ export CONTAINER_DATA=/var/tmp/
~$ export CONTAINER_SITE=/var/www/html
~$ mkdir -p visits_site/html visits_site/contador | cat > visits.txt | cat > index.php 
~$nano  index.php 
```
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
```

# 2) Crear imagen y contenedor con volumen compartido

~$docker build -t contador-visitas .
~$docker run --name contador-visitas-demo --rm -d -p 8080:80 \
                                                -v $HOST_DATA:$CONTAINER_DATA:rw \
                                                -v $HOST_SITE:$CONTAINER_SITE:ro \
                                                contador-visitas


# 3) Abrir la consola del contenedor y dar permiso a /var/tmp

~$docker exec -it contador-visitas-demo /bin/bash
~$chown -R www-data:www-data /var/tmp/


# 4) Verificar dentro del contenedor, parar y borrar

~$docker stop contador-visitas-demo
