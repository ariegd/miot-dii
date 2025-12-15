# Objetivo del ejemplo

Creación un Dockerfile basado en `ghcr.io/foundry-rs/foundry:stable` y un script `flow.sh` que automatiza todo el flujo:
* creación de la wallet de Bob
* verificación de saldo inicial
* envío de 5 ETH desde Alice
* verificación de saldo final
Utilizando `docker exec` contra el contenedor red-local

## Dockerfile

```Dockerfile
# Imagen base oficial de Foundry
FROM ghcr.io/foundry-rs/foundry:stable

# Establecemos un directorio de trabajo
WORKDIR /workspace

# Copiamos el script al contenedor (opcional, por si quieres ejecutarlo dentro)
COPY flow.sh /workspace/flow.sh

# Damos permisos de ejecución
# RUN chmod +x /workspace/flow.sh

# Comando por defecto (Anvil)
CMD ["anvil", "--host", "0.0.0.0", "--allow-origin", "*"]
```

---

## Script Bash (flow.sh)

Este script asume que:
- El contenedor ya está corriendo con nombre `red-local`
- Anvil está escuchando en `http://127.0.0.1:8545`

```bash
#!/usr/bin/env bash
set -e

# ==============================
# Variables de entorno
# ==============================

# Clave privada por defecto de Anvil (Cuenta 0 - Alice)
export PRIV_KEY_ALICE=0xac0974bec39a17e36ba4a6b4d238ff944bacb478cbed5efcae784d7bf4f2ff80
export RPC_URL=http://127.0.0.1:8545

# ==============================
# 1. Crear wallet de Bob
# ==============================

echo "Creando wallet para Bob..."
BOB_OUTPUT=$(docker exec -i red-local cast wallet new)

echo "$BOB_OUTPUT"

# Extraer la dirección usando grep/sed
ADDR_BOB=$(echo "$BOB_OUTPUT" | grep Address | awk '{print $2}')

if [ -z "$ADDR_BOB" ]; then
  echo "No se pudo extraer la dirección de Bob"
  exit 1
fi

echo "Dirección de Bob: $ADDR_BOB"

# ==============================
# 2. Verificar saldo inicial
# ==============================

echo "Saldo inicial de Bob (debe ser 0):"
docker exec -it red-local cast balance "$ADDR_BOB" --rpc-url "$RPC_URL"

# ==============================
# 3. Enviar 5 ETH de Alice a Bob
# ==============================

echo "Enviando 5 ETH de Alice a Bob..."
docker exec -it red-local cast send "$ADDR_BOB" \
  --value 5ether \
  --private-key "$PRIV_KEY_ALICE" \
  --rpc-url "$RPC_URL"

# ==============================
# 4. Verificar saldo final
# ==============================

echo "Saldo final de Bob (5 ETH en Wei):"
docker exec -it red-local cast balance "$ADDR_BOB" --rpc-url "$RPC_URL"

# ==============================
# Fin
# ==============================

echo "Flujo completado correctamente"
```

---

## Uso recomendado

0. Permisos
```bash
chmod 777 flow.sh
```

1. Construir la imagen (opcional):
```bash
docker build -t foundry-local .
```

2. Levantar Anvil:
```bash
docker run -d --rm --network host --name red-local foundry-local
```

3. Ejecutar el flujo:
```bash
source flow.sh
```

