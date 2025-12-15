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

