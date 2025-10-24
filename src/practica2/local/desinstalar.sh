#!/bin/bash
echo "1. Deteniendo los servicios de Nginx, MariaDB y PHP-FPM..."
sudo systemctl stop nginx
sudo systemctl stop mariadb

if systemctl status php*-fpm 2>/dev/null | grep -q "active"; then
    sudo systemctl stop $(systemctl list-units --type=service --state=running --no-pager | grep 'php.*-fpm' | awk '{print $1}')
else
    echo "   Servicio PHP-FPM no encontrado o ya detenido."
fi

echo "2. Desinstalando Nginx, MariaDB y todos los paquetes PHP instalados con 'purge'..."
sudo apt purge -y nginx mariadb-server mariadb-client


PHP_PACKAGES=$(dpkg -l | grep 'php' | awk '{print $2}' | grep -E 'php([0-9]\.[0-9]|)-fpm|php([0-9]\.[0-9]|)-mysql|php([0-9]\.[0-9]|)-cli|php-fpm|php-mysql|php-cli')

if [ -n "$PHP_PACKAGES" ]; then
    echo "   Paquetes PHP encontrados para purgar: $PHP_PACKAGES"
    sudo apt purge -y $PHP_PACKAGES
else
    echo "   No se encontraron paquetes PHP-FPM/MySQL para purgar."
fi

echo "3. Eliminando archivos de configuración y datos residuales..."
sudo rm -rf /etc/nginx
sudo rm -rf /var/www/html/* 
sudo rm -rf /var/lib/mysql
sudo rm -rf /etc/php


echo "4. Ejecutando limpieza automática y eliminación de dependencias no usadas..."
sudo apt autoremove -y
sudo apt autoclean

echo "Desinstalación de LEMP completada. La máquina Debian está limpia."

