## Paso 1: Instalación de Cordova
```
# Debian
sudo apt install nodejs npm
sudo npm install -g cordova
```
## Paso 2: Hola mundo con Cordova
1. Crear el proyecto
```
cordova create hello_world es.ucm.hello HelloWorld
```
2. Inspeccionar los ficheros creados
```
cd hello_world
```
3. Añadir plataformas a usar
```
cordova platform add browser ios android
cordova platform ls
```
4. Editar index.html
```
<!DOCTYPE html>
<html>
    <head>
        <title>Hola Mundo</title>
    </head>
    <body>
        <h1>¡Hola Mundo desde Cordova!</h1>
        <script src="cordova.js"></script>
        <script src="js/index.js"></script>
    </body>
</html>
```
5. Comprobar el proyecto
```
cordova requirements
```
6. Probar la plataforma browser
```
cordova build browser
cordova run browser
```
## Paso 3: El cliente Cordova funciona  en plataforma browser: permite listar e insertar asistentes en el backend REST.
 1. Modificar www/index.html del cliente Cordova
```
<!DOCTYPE html>
<html>
<head>
      <title>Gestión de Asistentes</title>
</head>
<body>
    <h1>¡Hola Mundo desde Cordova!</h1>
    <script src="cordova.js"></script>
    <script src="js/index.js"></script>
    
  <h2>Gestión de Asistentes</h2>

  <label>URL del servidor:</label><br>
  <input type="text" id="serverUrl" value="http://localhost:5000/asistentes" size="50"><br><br>

  <h3>Registrar nuevo asistente</h3>
  <form id="formRegistro" onsubmit="event.preventDefault(); registrar();">
    <label>Nombre:</label><br>
    <input type="text" id="nombre" required><br><br>
    <label>Email:</label><br>
    <input type="email" id="email" required><br><br>
    <button type="submit">Registrar</button>
  </form>

  <h3>Listado de asistentes</h3>
  <button onclick="listar()">Listar asistentes</button>
  <ul id="listaAsistentes"></ul>

  <script>
    async function listar() {
      const url = document.getElementById('serverUrl').value;
      try {
        const respuesta = await fetch(url);
        if (!respuesta.ok) throw new Error('Error al listar');
        const asistentes = await respuesta.json();

        const lista = document.getElementById('listaAsistentes');
        lista.innerHTML = ''; // limpiar lista anterior

        asistentes.forEach(a => {
          const li = document.createElement('li');
          li.textContent = `${a.nombre} (${a.email})`;
          lista.appendChild(li);
        });
      } catch (error) {
        alert('No se pudo obtener la lista: ' + error.message);
      }
    }

    async function registrar() {
      const url = document.getElementById('serverUrl').value;
      const nombre = document.getElementById('nombre').value.trim();
      const email = document.getElementById('email').value.trim();

      if (!nombre || !email) {
        alert('Por favor, completa todos los campos.');
        return;
      }

      const datos = { nombre, email };

      try {
        const respuesta = await fetch(url, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(datos)
        });

        if (!respuesta.ok) throw new Error('Error al registrar');

        alert('Asistente registrado correctamente.');
        document.getElementById('formRegistro').reset();
        listar(); // actualizar lista
      } catch (error) {
        alert('Error al registrar: ' + error.message);
      }
    }
  </script>
</body>
</html>
```
