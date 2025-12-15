
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
forge script script/Interact.s.sol:InteractScript \
  --rpc-url $RPC_URL \
  --broadcast

echo "Flujo completo ejecutado correctamente"
