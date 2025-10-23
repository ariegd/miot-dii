<?php
        require 'db.php';

        if ($_SERVER["REQUEST_METHOD"] == "POST" && !empty($_POST['nombre']) && !empty($_POST['email'])) {
                    $nombre = $_POST['nombre'];
                    $email = $_POST['email'];

                    $stmt = $conn->prepare("INSERT INTO estudiantes (nombre, email) VALUES (?, ?)");
                    $stmt->bind_param("ss", $nombre, $email);
                    
                    if ($stmt->execute()) {
                                $mensaje = "Estudiante '$nombre' registrado con éxito.";
                    } else {
                                $mensaje = "Error al registrar el estudiante: " . $stmt->error;
                    }

                    $stmt->close();
                    $conn->close();
                    
                    header("Ubicación: index.php?msg=" . urlencode($mensaje));
                    exit();
        } else {
                    header("Ubicación: index.php");
                    exit();
        }
?>
