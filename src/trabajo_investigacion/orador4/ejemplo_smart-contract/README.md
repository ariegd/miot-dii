# Objetivo del ejemplo práctoco
✔ Dockerfile basado en ghcr.io/foundry-rs/foundry:latest
✔ Script Bash único (setup_and_run.sh) que:

1. Ejecuta forge init
2. Instala forge-std
3. Configura foundry.toml con los remappings correctos
4. Compila
5. Despliega e interactúa con el contrato PiggyBank usando las cuentas 0, 1 y 2 de Anvil
✔ Contrato PiggyBank.sol
✔ Script Interact.s.sol con vm.startBroadcast por actor
✔ Flujo reproducible 100% dentro de Docker

Este setup es ideal para:
* demos técnicas
* workshops
* CI/CD local
* pruebas determinísticas con Anvil

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
4. Compila y ejecuta el script Interact.s.sol

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
forge script script/Interact.s.sol:InteractScript \
  --rpc-url $RPC_URL \
  --broadcast

echo "Flujo completo ejecutado correctamente"
```

---

## Contrato: src/PiggyBank.sol

```solidity
// SPDX-License-Identifier: MIT
pragma solidity ^0.8.13;

contract PiggyBank {
    address public owner;

    event Deposit(address indexed sender, uint256 amount);
    event Withdraw(address indexed receiver, uint256 amount);

    constructor() {
        owner = msg.sender;
    }

    function deposit() public payable {
        emit Deposit(msg.sender, msg.value);
    }

    function sendToFriend(address payable _friend, uint256 _amount) public {
        require(msg.sender == owner, "No eres el dueno");
        require(address(this).balance >= _amount, "Fondos insuficientes");

        _friend.transfer(_amount);
        emit Withdraw(_friend, _amount);
    }

    function getBalance() public view returns (uint256) {
        return address(this).balance;
    }
}
```

---

## Script Foundry: script/Interact.s.sol

```solidity
// SPDX-License-Identifier: MIT
pragma solidity ^0.8.13;

import "forge-std/Script.sol";
import "forge-std/console.sol";
import "../src/PiggyBank.sol";

contract InteractScript is Script {
    function run() public {
        uint256 adminKey = 0xac0974bec39a17e36ba4a6b4d238ff944bacb478cbed5efcae784d7bf4f2ff80;
        uint256 donorKey = 0x59c6995e998f97a5a0044966f0945389dc9e86dae88c7a8412f4603b6b78690d;
        address beneficiary = 0x3C44CdDdB6a900fa2b585dd299e03d12FA4293BC;

        vm.startBroadcast(adminKey);
        PiggyBank bank = new PiggyBank();
        console.log("PiggyBank desplegado en:", address(bank));
        vm.stopBroadcast();

        vm.startBroadcast(donorKey);
        bank.deposit{value: 10 ether}();
        console.log("Cuenta 1 depositó 10 ETH");
        vm.stopBroadcast();

        console.log("Saldo banco:", bank.getBalance());

        vm.startBroadcast(adminKey);
        bank.sendToFriend(payable(beneficiary), 5 ether);
        console.log("Admin envió 5 ETH a Cuenta 2");
        vm.stopBroadcast();

        console.log("Saldo final banco:", bank.getBalance());
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

