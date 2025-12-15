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
