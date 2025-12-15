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


