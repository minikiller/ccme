FROM openjdk:11.0.9.1
MAINTAINER sunlingfeng & litao build v1
ENV LANG C.UTF-8
EXPOSE 8323
WORKDIR /opt/app

# Run the image as a non-root user
RUN adduser -D myuser
USER myuser

#=====deplory java jar pack=========
COPY ./target/ccme-mathcingengine-2.2.0-standalone.jar /opt/app/ccme-matchingengine-2.2.0.jar
ENTRYPOINT ["java", "-jar", "/opt/app/ccme-matchingengine-2.2.0.jar"]