FROM openjdk:8-jre-alpine

WORKDIR /app
COPY target/docker/* /app/
RUN chmod 777 /app/*.sh

CMD ["./entrypoint.sh"]
