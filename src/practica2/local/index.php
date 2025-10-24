<?php
        require 'db.php';
?>
<!DOCTYPE html>
<html>
        <head>
                <title>Lista de Estudiantes</title>
        </head>
        <body>
                <?php
                            if (isset($_GET['msg'])) {
                                $mensaje_mostrado = urldecode($_GET['msg']);
                                $is_error = (strpos($mensaje_mostrado, 'Error') !== false);
                                $color = $is_error ? 'red' : 'green';
                                
                                echo "<p style='color: {$color}; border: 1px solid {$color}; padding: 10px; background-color: #f0f0f0; font-weight: bold;'>";
                                echo "Mensaje: " . htmlspecialchars($mensaje_mostrado);
                                echo "</p>";
                            }
                    ?>
                    <h1>Estudiantes Registrados</h1>
                    <h2>Registrar Nuevo Estudiante</h2>
                    <form action="insert.php" method="POST">
                                <label for="nombre">Nombre:</label>
                                <input type="text" id="nombre" name="nombre" required><br><br>                   
                                <label for="email">Email:</label>
                                <input type="email" id="email" name="email" required><br><br>
                                <button type="submit">Guardar Estudiante</button>
                    </form>
            
                <hr>
                    <h2>Lista Actual</h2>
                    <?php
                            $sql = "SELECT nombre, email FROM estudiantes ORDER BY id DESC";
                            $result = $conn->query($sql);
                            
                            if ($result->num_rows > 0) {
                                        echo "<table border='1'>";
                                        echo "<tr><th>Nombre</th><th>Email</th></tr>";
                                        
                                        while($row = $result->fetch_assoc()) {
                                                echo "<tr><td>" . htmlspecialchars($row["nombre"]) . "</td><td>" . htmlspecialchars($row["email"]) . "</td></tr>";
                                        }                                
                                        echo "</table>";
                            } else {
                                        echo "No hay estudiantes registrados.";
                            }
                            
                            $conn->close();
                    ?>
        </body>
</html>
