# Práctica 1: Contador de visitas

## Requisitos:
1. Existen imágenes que incluyen PHP y Apache (p.e. php:8.2-apache), pero en
este lab vamos a realizar la instalació n de PHP y apache en la imagen
debian:bookworm.
```
#Dockerfile
FROM debian:bookworm
```

2. La imagen que creemos a partir de debian:bookworm debe llamarse contador-
visitas
```
#consola
~$ docker build -t contador-visitas .
```

3. La instalación de los paquetes necesarios ( apache2, php, libapache2-mod-php)
debe realizarse dentro del Dockerfile.
```
#Dockerfile
RUN apt update && \
    apt install -y apache2  php  libapache2-mod-php && \
    apt clean
```
    
4. El contador de visitas debe implementarse en un único fichero PHP llamado
index.php ubicado dentro del contenedor en /var/www/html/.
```
#index.php
<?php
	$file = '/var/tmp/visits.txt';
	$count = 0;

	if (file_exists($file)) {
		$count = (int)file_get_contents($file);
	}
	$count++;

	file_put_contents($file, $count, LOCK_EX);
?>
```

5. El contador debe guardar su valor en el fichero /var/tmp/visits.txt del
contenedor. Este fichero debe residir en un volumen gestionado de Docker
montado en /var/tmp del contenedor y llamado contador-visitas-data.
```
#Dockerfile
VOLUME  /var/tmp/

#consola
~$docker exec -it contador-visitas-demo /bin/bash
~$ chown -R www-data:www-data /var/tmp/

#consola NO
~$docker volume contador-visitas-data
```

6. El servidor Apache debe ejecutarse en foreground usando apachectl -D FO-
REGROUND. Esto permite que el contenedor permanezca en ejecució n.
```
#Dockerfile
CMD ["/usr/sbin/apache2ctl", "-D", "FOREGROUND"]
```

7. La imagen debe exponer el puerto 80 y el contenedor debe ejecutarse
publicando ese puerto en el 8080 del host
```
#consola 
# -p 8080:80
~$ docker run --name contador-visitas-demo --rm -d -p 8080:80 -v $HOST_SITE:$CONTAINER_SITE:rw contador-visitas
```

8. Al detener y eliminar el contenedor y volver a ejecutarlo sin borrar el
volumen, el contador debe mantener el valor anterior.
```
#consola 
# -v $HOST_SITE:$CONTAINER_SITE:rw
~$ docker run --name contador-visitas-demo --rm -d -p 8080:80 -v $HOST_SITE:$CONTAINER_SITE:rw contador-visitas
```

9. Si no existe o se elimina el volumen gestionado, el contador se reinicia a 0.
```
#index.php
file_put_contents($file, $count);
```
