FROM ghcr.io/foundry-rs/foundry:latest

WORKDIR /code

# Copiamos scripts y contratos
COPY src ./src
COPY script ./script
COPY setup_and_run.sh /setup_and_run.sh

# RUN chmod +x /setup_and_run.sh

# Anvil como proceso principal
CMD ["anvil", "--host", "0.0.0.0", "--allow-origin", "*"]
