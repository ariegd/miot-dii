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

