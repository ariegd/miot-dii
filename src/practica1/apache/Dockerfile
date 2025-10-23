FROM debian:bookworm

VOLUME  /var/tmp/

RUN apt update && \
    apt install -y apache2  php  libapache2-mod-php && \
    apt clean
RUN echo "ServerName localhost"  >> /etc/apache2/apache2.conf

CMD ["/usr/sbin/apache2ctl", "-D", "FOREGROUND"]
