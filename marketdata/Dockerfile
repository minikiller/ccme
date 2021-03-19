FROM openjdk:11.0.9.1
MAINTAINER sunlingfeng & litao build v1
ENV LANG C.UTF-8
ENV server.port 9880
EXPOSE 9880
WORKDIR /opt/app

# Run the image as a non-root user
RUN adduser -D myuser
USER myuser

#=====deplory java jar pack=========
COPY target/ccme-marketdata-2.2.0-standalone.jar /opt/app/ccme-marketdata-2.2.0-standalone.jar
ENTRYPOINT ["java", "-jar", "/opt/app/ccme-marketdata-2.2.0-standalone.jar" ]