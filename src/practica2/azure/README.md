# [Práctica 2: Creación de aplicación web en Azure](https://cvdof.ucm.es/moodle/mod/assign/view.php?id=412552)
```
Máster IoT, curso 25-26
├──Diseño de Insfraestructura Inteligente IoT
└── Fernando Carlos López <fclh@ucm.es>
 	└── Práctica 2: Creación de aplicación web en Azure/ Autores
		├── Ariel Gámez <arielg01@ucm.es>
 		└── Jose Villacres <jvillacr@ucm.es>
```

## Despliegue en Azure App Service y Azure Database for MySQL<a name="id2"></a>
1. Código fuente de la aplicación (index.php, insert.php, y otros).
- db.php
- insert.php
- index.php

2. Capturas de pantalla que demuestren:
* La aplicación funcionando en Azure (URL accesible).
[video](https://www.youtube.com/watch?v=Mk82p_wjyF8)
![Descripción de la imagen](https://github.com/ariegd/miot-dii/blob/LAB2/src/practica2/local/img/Captura%20desde%202025-10-25%2012-01-00.png)
* La BD con registros almacenados tanto en local como en Azure.
[video](https://www.youtube.com/watch?v=Mk82p_wjyF8)
![Descripción de la imagen](https://github.com/ariegd/miot-dii/blob/LAB2/src/practica2/local/img/Captura%20desde%202025-10-25%2012-01-00.png)

3. Instrucciones de despliegue breves (README), que incluyan:
Como en conferencia nos hemos creado una cuenta en Microsoft Azure. Pasamos directamente a registrar el servicio:
* Configuración: Pasos esenciales en local y en Azure

```
# Despliegue en Azure- Logarnos
az login
# Registrarnos en un proveedor de recursos (una vez por suscripción)
az provider list --output table
az provider list --output table | grep Microsoft.Web
az provider register --namespace Microsoft.Web
az provider show --namespace Microsoft.Web --query "registrationState"

#Desplegar
# Podemos consultar los runtime disponibles para Linux con:
az webapp list-runtimes --os Linux # Todos los que podeos utilizar
az webapp up --name basic-nginx-php-app --resource-group tutoriales --location "spaincentral" --sku F1 --runtime "PHP|8.2" --os-type Linux
# Ver los logs
az webapp log tail --name basic-nginx-php-app --resource-group tutoriales
```
* URL: de la aplicación web en Azure
```
https://basic-nginx-php-app.azurewebsites.net/
```

4. Script SQL: Fichero .sql los comandos de creación de usuario, BD y tablas
Este apartado se puede llevar a cabo de dos maneras, por la página web de Microsoft Azure y mediante lineas de comandos. Nosotros lo vamos a realizar mediante lineas de comando.
```
# 1. Iniciar sesión en Azure, si no lo hemos realizado aún
az login

# 2. Registrar el proveedor de recursos web (Si no lo has hecho ya)
az provider register --namespace Microsoft.DBforMySQL
az provider show --namespace Microsoft.DBforMySQL --query "registrationState" --output table

##  Azure Database for MySQL – Flexible Server
#--------------------------------------------------
# A. Comando válido para crear un servidor MySQL Flexible en Azure
az mysql flexible-server create \
  --name mi-mysql-server-77 \
  --resource-group tutoriales \
  --location spaincentral \
  --admin-user adminuser \
  --admin-password ClaveSegura123! \
  --sku-name Standard_B1ms \
  --tier Burstable \
  --version 8.0.21
 
 # B. Crear la base de datos webdb
 az mysql flexible-server db create \
  --resource-group tutoriales \
  --server-name mi-mysql-server-77  \
  --database-name webdb
 
 # C. Configurar el firewall para permitir conexiones
az mysql flexible-server firewall-rule create \
  --resource-group tutoriales \
  --name mi-mysql-server-77 \
  --rule-name permitirAzure \
  --start-ip-address 0.0.0.0 \
  --end-ip-address 255.255.255.255

## Llegado a este punto, desde local no ha sido posible el acceso para entrar
#--------------------------------------------------
# A. solución ejecutar en el Bash de la Web de Microsoft Azure y ejecutar el script webdb.sql
mysql -h mi-mysql-server-77.mysql.database.azure.com \
  -u adminuser@mi-mysql-server-77 \
  -p
 # o
 mysql -h mi-mysql-server-77.mysql.database.azure.com -P 3306 -u adminuser -p

# B. Si es necesario desde el host actualizar contraseña administrador
az mysql flexible-server update \
  --name mi-mysql-server-77 \
  --resource-group tutoriales \
  --admin-password Cl@ve-123

# C. Verifica que la base de datos esté creada
az mysql flexible-server db list \
  --resource-group tutoriales \
  --server-name mi-mysql-server-77
# Sino crear
az mysql flexible-server db create \
  --resource-group tutoriales \
  --server-name mi-mysql-server-77 \
  --database-name webdb

# D. Regla de firewall no activa
az mysql flexible-server firewall-rule list \
  --resource-group tutoriales \
  --name mi-mysql-server-77


## Azure CLI no interpreta bien los valores si no están entre comillas.
#--------------------------------------------------
# A. escapar correctamente los valores
az webapp config appsettings set \
  --name basic-nginx-php-app \
  --resource-group tutoriales \
  --settings \
  DB_HOST='mi-mysql-server-77.mysql.database.azure.com' \
  DB_USER='adminuser@mi-mysql-server-77' \
  DB_PASSWORD='Cl@veSegura2025!' \
  DB_NAME='webdb'
 
 # B. Verifica que se hayan aplicado correctamente
 az webapp config appsettings list \
  --name basic-nginx-php-app \
  --resource-group tutoriales \
  --output table


## Azure Database for MySQL Flexible Server usa por defecto el plugin caching_sha2_password, pero algunos entornos PHP con mysqli no lo soportan bien.
#--------------------------------------------------
# A. Crea un nuevo usuario con plugin compatible
CREATE USER 'arielweb'@'%' IDENTIFIED WITH mysql_native_password BY 'ClaveWeb2025!';
GRANT ALL PRIVILEGES ON webdb.* TO 'arielweb'@'%';
FLUSH PRIVILEGES;

# B. Actualiza las variables de entorno en Azure
az webapp config appsettings set \
  --name basic-nginx-php-app \
  --resource-group tutoriales \
  --settings \
  DB_USER='arielweb' \
  DB_PASSWORD='ClaveWeb2025!'
  
 # C. Reinicia la app
 az webapp restart \
  --name basic-nginx-php-app \
  --resource-group tutoriales


## Tu servidor MySQL Flexible en Azure tiene activado el parámetro require_secure_transport=ON, lo que obliga a que todas las conexiones usen SSL/TLS.
#--------------------------------------------------
# habilitar conexión SSL en tu app PHP
<?php
$host = getenv('DB_HOST');
$user = getenv('DB_USER');
$password = getenv('DB_PASSWORD');
$database = getenv('DB_NAME');

$conn = mysqli_init();
mysqli_ssl_set($conn, NULL, NULL, NULL, NULL, NULL);
mysqli_real_connect($conn, $host, $user, $password, $database, 3306, NULL, MYSQLI_CLIENT_SSL);

if (mysqli_connect_errno()) {
    die("Conexión fallida: " . mysqli_connect_error());
} else {
    echo "Conexión segura y exitosa a la base de datos.";
}
?>
```

![Descripción de la imagen](https://github.com/ariegd/miot-dii/blob/LAB2/src/practica2/local/img/Captura%20desde%202025-10-25%2012-01-00.png)
