echo "uno: Actualizar el Sistema"
sudo apt update && sudo apt upgrade -y
echo "dos: Instalar Nginx (Servidor Web)"
sudo apt install nginx -y
echo "tres: Instalar MariaDB (Base de Datos)"
sudo apt install mariadb-server -y
echo "cuatro: configurar mariadb"
sudo mysql_secure_installation
echo "cinco: Instala PHP-FPM, la librería MySQL (mysqli) y otras comunes"
sudo apt install php-fpm php-mysql php-cli -y
