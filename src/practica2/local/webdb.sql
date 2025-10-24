DROP DATABASE IF EXISTS webdb;
CREATE DATABASE webdb;
USE webdb;

CREATE TABLE estudiantes (
    id INT AUTO_INCREMENT PRIMARY KEY, 
    nombre VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE, 
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP 
);

INSERT INTO estudiantes (nombre, email) VALUES
('Ariel Soto', 'ariel.soto@ucm.es'),
('Elena Garcés', 'elena-garces@uc3m.es'),
('Javier Ramos', 'javier_ramos@uam.es'),
('Sofía Martín', 'sofiamartin@uned.es'),
('Carlos Ruiz', 'carlosruiz@uclm.es'),
('Laura Pérez', 'laura-perez@uzo.es'),
('David López', 'david_lopez@uga.es'),
('Isabel Torres', 'isabeltorres@upv.es'),
('Pedro Núñez', 'pedro_nunez@uee.es'),
('María Gil', 'maria.gil@uan.es');
