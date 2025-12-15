# Objetivo Arquitectura IoT + Blockchain con Docker
Resumen del Flujo de Ejecución
1. Docker: anvil está corriendo (tu contenedor red-local).
2. Docker: eclipse-mosquitto está corriendo en el puerto 1883.
3. Contrato: Has ejecutado el script DeploySensor y tienes la dirección.
4. Python: Ejecutas python bridge.py.
5. ESP32: Enciendes el microcontrolador.
Lo que verás:
1. El ESP32 publica un JSON en sensor/data.
2. El script de Python lo recibe, firma una transacción con la clave privada de Anvil y la envía al nodo Docker.
3. Anvil mina el bloque instantáneamente.
4. El script de Python obtiene el Tx Hash y lo publica en sensor/confirm.
5. El ESP32 recibe el mensaje y muestra el Hash en el Monitor Serie: "🔗 Confirmación Blockchain recibida! Tx Hash: 0x..."

## Dockerfile (Foundry + Anvil)

```Dockerfile
FROM ghcr.io/foundry-rs/foundry:latest

WORKDIR /code

# Copiamos scripts y contratos
COPY src ./src
COPY script ./script
COPY setup_and_run.sh /setup_and_run.sh

RUN chmod +x /setup_and_run.sh

# Anvil como proceso principal
CMD ["anvil", "--host", "0.0.0.0", "--allow-origin", "*"]
```

---

## Script Bash: setup_and_run.sh

Este script:
1. Inicializa Forge
2. Instala forge-std
3. Configura foundry.toml
4. Compila y ejecuta el script DeploySensor.s.sol

```bash
#!/usr/bin/env bash
set -e

RPC_URL=http://127.0.0.1:8545

cd /code

if [ ! -f foundry.toml ]; then
  echo "Inicializando proyecto Forge..."
  forge init --force .
fi

if [ ! -d lib/forge-std ]; then
  echo "Instalando forge-std..."
  forge install foundry-rs/forge-std --no-commit
fi

cat > foundry.toml <<EOF
[profile.default]
src = "src"
out = "out"
libs = ["lib"]
remappings = [
  "forge-std/=lib/forge-std/src/"
]
EOF

echo "Compilando contratos..."
forge build

echo "Ejecutando script de interacción..."
forge script script/DeploySensors.sol:InteractScript \
  --rpc-url $RPC_URL \
  --broadcast

echo "Flujo completo ejecutado correctamente"
```
---

## Contrato: src/SensorData.sol

```Solidity

// SPDX-License-Identifier: MIT
pragma solidity ^0.8.13;

contract SensorData {
    // Estructura de datos
    struct Reading {
        uint256 timestamp;
        uint256 temperature;
        uint256 humidity;
    }

    Reading[] public readings;
    
    // Evento para validar la transacción externamente
    event DataStored(uint256 indexed id, uint256 timestamp, uint256 temperature, uint256 humidity);

    // Función para guardar datos (simulamos que temp y hum son enteros multiplicados por 100)
    function storeData(uint256 _temp, uint256 _hum) public {
        readings.push(Reading(block.timestamp, _temp, _hum));
        emit DataStored(readings.length - 1, block.timestamp, _temp, _hum);
    }

    function getReadingCount() public view returns (uint256) {
        return readings.length;
    }
}
```

---

## Script Foundry: script/DeploySensor.s.sol

```solidity
// SPDX-License-Identifier: MIT
pragma solidity ^0.8.13;

import "forge-std/Script.sol";
import "../src/SensorData.sol";

contract DeploySensor is Script {
    function run() public {
        // Usamos la Private Key (0) de Anvil
        uint256 deployerKey = 0xac0974bec39a17e36ba4a6b4d238ff944bacb478cbed5efcae784d7bf4f2ff80;

        vm.startBroadcast(deployerKey);
        SensorData sensor = new SensorData();
        console.log("SensorData Contract Address:", address(sensor));
        vm.stopBroadcast();
    }
}
```

---

## Uso

```bash
# Construir imagen
docker build -t piggybank-foundry .

# Ejecutar Anvil
docker run -it --rm --network host \
  -v $(pwd):/code \
  --name red-local piggybank-foundry

# En otra terminal, ejecutar flujo
docker exec -it red-local /setup_and_run.sh
```

---

# Arquitectura IoT + Blockchain con Docker

## 1️⃣ Dockerfile – Blockchain (Anvil + Foundry)

**Dockerfile.anvil**

```Dockerfile
FROM ghcr.io/foundry-rs/foundry:latest

WORKDIR /code

COPY src ./src
COPY script ./script
COPY foundry.toml ./foundry.toml

CMD ["anvil", "--host", "0.0.0.0", "--allow-origin", "*"]
```

**Ejecución**
```bash
docker build -t anvil-chain -f Dockerfile.anvil .

docker run -it --rm --network host \
  -v $(pwd):/code \
  --name red-local anvil-chain
```

---

## 2️⃣ Dockerfile – Broker MQTT (Mosquitto)

**Dockerfile.mqtt**

```Dockerfile
FROM eclipse-mosquitto:latest

EXPOSE 1883 9001
```

**Ejecución**
```bash
docker build -t mqtt-broker -f Dockerfile.mqtt .

docker run -it --rm \
  -p 1883:1883 -p 9001:9001 \
  --name mqtt mqtt-broker
```

---

## 3️⃣ Script SH – Middleware Python (bridge.sh)

Este script levanta el **oráculo IoT → Blockchain**.

---

## Middleware en Python (bridge.py)

```Python
import json
import time
from web3 import Web3
import paho.mqtt.client as mqtt

# --- CONFIGURACIÓN BLOCKCHAIN ---
ANVIL_RPC = "http://127.0.0.1:8545" # URL de tu Docker Anvil
CONTRACT_ADDRESS = "PEGAR_DIRECCION_DEL_CONTRATO_AQUI"
PRIVATE_KEY = "0xac0974bec39a17e36ba4a6b4d238ff944bacb478cbed5efcae784d7bf4f2ff80" # Cuenta 0

# ABI Mínimo del contrato SensorData
ABI = '[{"inputs":[{"internalType":"uint256","name":"_temp","type":"uint256"},{"internalType":"uint256","name":"_hum","type":"uint256"}],"name":"storeData","outputs":[],"stateMutability":"nonpayable","type":"function"},{"anonymous":false,"inputs":[{"indexed":true,"internalType":"uint256","name":"id","type":"uint256"},{"indexed":false,"internalType":"uint256","name":"timestamp","type":"uint256"},{"indexed":false,"internalType":"uint256","name":"temperature","type":"uint256"},{"indexed":false,"internalType":"uint256","name":"humidity","type":"uint256"}],"name":"DataStored","type":"event"}]'

# Conexión Web3
w3 = Web3(Web3.HTTPProvider(ANVIL_RPC))
contract = w3.eth.contract(address=CONTRACT_ADDRESS, abi=ABI)
account = w3.eth.account.from_key(PRIVATE_KEY)

# --- CALLBACKS MQTT ---
def on_connect(client, userdata, flags, rc):
    print("Conectado al Broker MQTT")
    client.subscribe("sensor/data")

def on_message(client, userdata, msg):
    try:
        payload = json.loads(msg.payload.decode())
        temp = int(payload['temp'] * 100) # Solidity no usa decimales
        hum = int(payload['hum'] * 100)
        
        print(f"Recibido: Temp {payload['temp']}, Hum {payload['hum']}")
        
        # Construir Transacción
        tx = contract.functions.storeData(temp, hum).build_transaction({
            'from': account.address,
            'nonce': w3.eth.get_transaction_count(account.address),
            'gas': 2000000,
            'gasPrice': w3.to_wei('1', 'gwei')
        })
        
        # Firmar y Enviar
        signed_tx = w3.eth.account.sign_transaction(tx, PRIVATE_KEY)
        tx_hash = w3.eth.send_raw_transaction(signed_tx.raw_transaction)
        
        hash_hex = w3.to_hex(tx_hash)
        print(f"✅ TX Enviada: {hash_hex}")
        
        # Devolver el hash al ESP32
        client.publish("sensor/confirm", hash_hex)
        
    except Exception as e:
        print(f"Error procesando: {e}")

# --- INICIO ---
client = mqtt.Client()
client.on_connect = on_connect
client.on_message = on_message

client.connect("localhost", 1883, 60) # Conecta al Mosquitto local
print("Puente IoT-Blockchain iniciado...")
client.loop_forever()

```


**bridge.sh**
```bash
#!/usr/bin/env bash
set -e

VENV=.venv

if [ ! -d "$VENV" ]; then
  python3 -m venv $VENV
fi

source $VENV/bin/activate

pip install --upgrade pip
pip install web3 paho-mqtt

echo "Iniciando Bridge IoT → Blockchain"
python bridge.py
```

**Uso**
```bash
chmod +x bridge.sh
./bridge.sh
```

---

## 4️⃣ Script SH – ESP32 (ESP-IDF build & flash)

Asume:
- ESP-IDF instalado
- Proyecto ESP32 en `esp32/`

---

## Código para el ESP32
- Carga esto en tu ESP32 usando ESP-IDF.
- Librería necesaria: PubSubClient (instálala desde el gestor de librerías).

```C++

#include <WiFi.h>
#include <PubSubClient.h>

// --- CONFIGURACIÓN ---
const char* ssid = "TU_WIFI";
const char* password = "TU_PASSWORD";
// ¡IMPORTANTE! Usa la IP local de tu PC (ej. 192.168.1.X), no localhost
const char* mqtt_server = "192.168.1.XXX"; 

WiFiClient espClient;
PubSubClient client(espClient);

void setup() {
  Serial.begin(115200);
  WiFi.begin(ssid, password);

  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.println("\nWiFi conectado");

  client.setServer(mqtt_server, 1883);
  client.setCallback(callback);
}

void callback(char* topic, byte* message, unsigned int length) {
  String messageTemp;
  for (int i = 0; i < length; i++) {
    messageTemp += (char)message[i];
  }
  // Recibimos el Hash de la Blockchain
  if (String(topic) == "sensor/confirm") {
    Serial.println("🔗 Confirmación Blockchain recibida!");
    Serial.print("Tx Hash: ");
    Serial.println(messageTemp);
  }
}

void reconnect() {
  while (!client.connected()) {
    if (client.connect("ESP32Client")) {
      Serial.println("MQTT conectado");
      client.subscribe("sensor/confirm"); // Suscribirse para recibir el hash
    } else {
      delay(5000);
    }
  }
}

void loop() {
  if (!client.connected()) {
    reconnect();
  }
  client.loop();

  // Enviar datos cada 10 segundos
  static unsigned long lastMsg = 0;
  unsigned long now = millis();
  if (now - lastMsg > 10000) {
    lastMsg = now;
    
    // Simulación de datos
    float temp = 24.5 + (random(0,50)/10.0);
    float hum = 60.0 + (random(0,20)/10.0);

    String payload = "{\"temp\": " + String(temp) + ", \"hum\": " + String(hum) + "}";
    Serial.print("Enviando: ");
    Serial.println(payload);
    
    client.publish("sensor/data", payload.c_str());
  }
}
```


**esp32_run.sh**
```bash
#!/usr/bin/env bash
set -e

ESP_PORT=${ESP_PORT:-/dev/ttyUSB0}

cd esp32

echo "Configurando proyecto ESP32"
idf.py set-target esp32

idf.py build

idf.py -p $ESP_PORT flash monitor
```

**Uso**
```bash
chmod +x esp32_run.sh
ESP_PORT=/dev/ttyUSB0 ./esp32_run.sh
```

---

## 🔄 Flujo Completo de Arranque

```bash
# 1. Blockchain
docker run -it --rm --network host --name red-local anvil-chain

# 2. MQTT
docker run -it --rm -p 1883:1883 --name mqtt mqtt-broker

# 3. Deploy contrato
docker exec -it red-local forge script script/DeploySensor.s.sol:DeploySensor \
  --rpc-url http://127.0.0.1:8545 --broadcast

# 4. Middleware
./bridge.sh

# 5. ESP32
./esp32_run.sh
```

---

## ✅ Resultado Final

- ESP32 publica datos → MQTT
- Python firma y envía TX → Anvil
- Smart Contract guarda datos inmutables
- Hash vuelve al ESP32

Este diseño es **idéntico a un sistema IoT industrial real**, pero con blockchain local determinista.

