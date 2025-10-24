## Desarrollo y prueba local usando Nginx y MariaDB<a name="id1"></a>
1. Código fuente de la aplicación (index.php, insert.php, y otros).
Tendremos tres archivos php con la lógica del sitio web
- db.php
- insert.php
- index.php

2. Capturas de pantalla que demuestren:
Las capturas de pantalla estarán en una carpeta img y en [GiHub](https://github.com/ariegd/miot-dii/tree/LAB2) y [YouTube]() historico de commit y videos.
* La aplicación funcionando en local (listado e inserción).
[video]()
* La BD con registros almacenados en local
[video]()

3. Instrucciones de despliegue breves (README), que incluyan:
* Instalación: de paquetes y servicios necesarios:
Los paquete necesarios se van a instalar ejecutando el script **instalar.sh**. Durante el proceso de instalación nos solicita el usuario root para el gestor de base de datos. Lo debemos anotar para pasarlo a la configuración del archivo db.php.
```
#consola
    echo "dos: Instalar Nginx (Servidor Web)"
    sudo apt install nginx -y
    echo "tres: Instalar MariaDB (Base de Datos)"
    sudo apt install mariadb-server -y
    echo "cuatro: configurar mariadb"
    sudo mysql_secure_installation
    echo "cinco: Instala PHP-FPM, la librería MySQL (mysqli) y otras comunes"
    sudo apt install php-fpm php-mysql php-cli -y
```

* Configuración: Pasos esenciales en local 
Una vez instalados los paquete necesarios se debe configurar y copiar los archivos al directorio de Nginx
```
#consola
    # 1. Configurar el archivo de Nginx 
    ~$ sudo nano /etc/nginx/sites-available/default

#default
        # pass PHP scripts to FastCGI server
        #
        location ~ \.php$ {
                include snippets/fastcgi-php.conf;
                include fastcgi.conf;
        #       # With php-fpm (or other unix sockets):
                fastcgi_pass unix:/run/php/php8.2-fpm.sock;
        #       # With php-cgi (or other tcp sockets):
        #       fastcgi_pass 127.0.0.1:9000;
    
    # 2. Agregar index.php
    index index.php index.html index.htm;

#consola
    #3. Copiar los archivos *.php al directorio y borrar  de Nginx
    ~$ cp index.php db.php insert.php /var/www/html/
    ~$ sudo rm /var/www/html/index.nginx-debian.html
    
    #4. Ajustar Permisos
    ~$ sudo chown -R www-data:www-data /var/www/html
    ~$ sudo chmod -R 755 /var/www/html
    
    # 5. Aplica los Cambios y Reinicia
    ~$ sudo nginx -t
    ~$ sudo systemctl reload nginx
```
  
4. Script SQL: Fichero .sql los comandos de creación de usuario, BD y tablas
Para crear las tablas y creación de usuarios ejecutar el script de sql
```
#consola
    ~$ mysql -u root -p webdb < ~/webdb.sql
```
