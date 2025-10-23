FROM debian:bookworm

RUN apt update && \
    apt install -y nginx && \
    apt clean

CMD ["nginx", "-g", "daemon off;","-D", "FOREGROUND"]
