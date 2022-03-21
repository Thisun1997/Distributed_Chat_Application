FROM openjdk:16-alpine3.13
WORKDIR /
COPY config.properties .
COPY chat-server-1.0-SNAPSHOT-shaded.jar .
EXPOSE 4000-6000
CMD java -jar chat-server-1.0-SNAPSHOT-shaded.jar