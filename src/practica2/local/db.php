<?php
$host = getenv('DB_HOST') ?: 'localhost';
$user = getenv('DB_USER') ?: 'root'; 
$password = getenv('DB_PASSWORD') ?: 'clave123'; 

$database = getenv('DB_NAME') ?: 'webdb';

$conn = new mysqli($host, $user, $password, $database);

if ($conn->connect_error) {
        die("Conexión fallida: " . $conn->connect_error);
}
?>
