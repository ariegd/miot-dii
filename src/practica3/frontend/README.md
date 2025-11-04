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
  <script src="cordova.js"></script>
  <script src="js/index.js"></script>
</body>
</html>
```
## Paso 3: Instalar Android SDK y dependencias
 1. Instalación de Java y Gradle en Linux
 ```
 # Debian
sudo apt update
sudo apt install openjdk-17-jdk
sudo update-alternatives --config java
 ```
 2. Crear variables de entorno para Java
```
 En Debian:
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export PATH=$JAVA_HOME/bin:$PATH
```
3. Instalación de Android SDK
```
# descargamos de la web oficial:
https://developer.android.com/studio#command-line-tools-only

# descomprimimos
sudo mkdir -p /opt/android-sdk/cmdline-tools
cd Downloads
sudo unzip commandlinetools-linux-*.zip -d /opt/android-sdk/cmdline-tools

# Y mover
mkdir -p /opt/android-sdk/cmdline-tools/latest
sudo mv /opt/android-sdk/cmdline-tools/cmdline-tools /opt/android-sdk/cmdline-tools/latest
```
4. Configurar variables de entorno del Android SDK
```
# Android SDK
export ANDROID_HOME=/opt/android-sdk
export PATH=$ANDROID_HOME/platform-tools:$ANDROID_HOME/cmdline-tools/latest/bin:
$ANDROID_HOME/emulator:$PATH
```
5. Instalar el resto de componentes de Android SDK
```
yes | sdkmanager --licenses
sudo /opt/android-sdk/cmdline-tools/latest/bin/sdkmanager "platform-tools" "build-tools;35.0.0" "platforms;android-35"
```
## Paso 4: Ejecutar la aplicación Android en dispositivo físico
1. Dar acceso al dispositivo físico
* En Debian también recargamos las reglas udev y reiniciamos el servicio:
```
sudo apt install android-sdk-platform-tools-common
sudo udevadm control --reload-rules
sudo service udev restart
```
En Virtual Box activar Machine|Settings|USB|Add|Samsung
Y reiniciar.
```
adb devices
```
2. Compilar la aplicación
```
cordova clean
cordova platform rm android
cordova platform add android
cordova build android
cordova run android # <-- emulador
```
3. Ejecutar en dispositivo físico
```
cordova run android --device
```
4. Comprobar el proyecto
```
cordova requirements
```
## Paso 5. Forzar http:// también en el WebView (más directa)
1. Instala este plugin para permitir tráfico HTTP/HTTPS mixto dentro de Cordova:
```
cordova plugin add cordova-plugin-inappbrowser
```
2. Y muy importante: dentro de <widget>, agregar:
```
<content src="index.html" />
<preference name="Scheme" value="http" />
<preference name="Hostname" value="localhost" />
```
3. Impeccionar app
```
chrome://inspect/#devices
```
4. Configurar el config.xml
```
<?xml version='1.0' encoding='utf-8'?>
<widget id="es.ucm.hello" 
                version="1.0.0" 
                xmlns="http://www.w3.org/ns/widgets" 
                xmlns:cdv="http://cordova.apache.org/ns/1.0" 
                xmlns:android="http://schemas.android.com/apk/res/android">
    <name>HelloWorld</name>
    <description>Sample Apache Cordova App</description>
    <author email="dev@cordova.apache.org" href="https://cordova.apache.org">
        Apache Cordova Team
    </author>
    <content src="index.html" />
    <preference name="Scheme" value="http" />
    <preference name="Hostname" value="localhost" />
    <allow-navigation href="*" />
    <access origin="*" />
    <allow-intent href="http://*/*" />
    <allow-intent href="https://*/*" />
    
    <platform name="android">
        <edit-config file="app/src/main/AndroidManifest.xml" mode="merge" target="/manifest/application">
            <application android:usesCleartextTraffic="true"  android:networkSecurityConfig="@xml/network_security_config" />
        </edit-config>
        
        <resource-file src="res/xml/network_security_config.xml" target="app/src/main/res/xml/network_security_config.xml" />
        <allow-navigation href="http://192.168.1.39:5000/*" />
        <access origin="http://192.168.1.39:5000/*" />
    </platform>
</widget>
```
6. Crear un archivo de seguridad de red: res/xml/network_security_config.xml
```
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <base-config cleartextTrafficPermitted="true">
        <trust-anchors>
            <certificates src="system" />
        </trust-anchors>
    </base-config>

    <!-- Permite tu backend específico -->
    <domain-config cleartextTrafficPermitted="true">
        <domain includeSubdomains="true">192.168.1.39</domain>
    </domain-config>
</network-security-config>
```
7. Modificar el index.html
```
        <meta http-equiv="Content-Security-Policy"
              content="default-src * data: gap: https://ssl.gstatic.com 'unsafe-eval' 'unsafe-inline';
                       connect-src * http://192.168.1.39:5000 http://* https://*;
                       style-src * 'unsafe-inline';
                       media-src *;
                       img-src * data:;">
                       
# para no estar escribiendo en la app
  <input type="text" id="serverUrl" value="http://192.168.1.39:5000/asistentes" size="50">
```

## Paso 6: En tu movil
> En el Android en modo developer y habilitar la depuración por USB: Todo en el setting
Paso 1: Activar el Modo Desarrollador
1. Esta opción está oculta por defecto en Android y es necesaria para acceder a la configuración de depuración.
2. Abre la aplicación Ajustes (Settings) en tu teléfono.
3. Desplázate hacia abajo y entra en Acerca del teléfono (About Phone) o Información del Software (Software Information).
4. Busca la sección Número de Compilación (Build Number).
5. Toca repetidamente sobre el Número de Compilación (generalmente 7 veces seguidas).
6. Verás un mensaje emergente que dice: "¡Ya eres desarrollador!" o "Ahora está activado el modo desarrollador

🛠️ Paso 2: Habilitar la Depuración USB
1. Una vez que el Modo Desarrollador está activo, aparecerá un nuevo menú para habilitar la depuración.
2. Vuelve al menú principal de Ajustes.
3. Busca y entra en la nueva opción llamada Opciones de Desarrollador (Developer Options). Esta suele estar en la parte inferior o dentro de la sección Sistema o Ajustes Adicionales.
4. Dentro de las Opciones de Desarrollador, busca la opción Depuración USB (USB Debugging).
5. Activa el interruptor de Depuración USB.
