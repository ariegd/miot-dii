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
        console.log("Cuenta 1 deposito 10 ETH");
        vm.stopBroadcast();

        console.log("Saldo banco:", bank.getBalance());

        vm.startBroadcast(adminKey);
        bank.sendToFriend(payable(beneficiary), 5 ether);
        console.log("Admin envio 5 ETH a Cuenta 2");
        vm.stopBroadcast();

        console.log("Saldo final banco:", bank.getBalance());
    }
}
