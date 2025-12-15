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

