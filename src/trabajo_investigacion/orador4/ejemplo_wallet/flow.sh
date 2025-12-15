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

