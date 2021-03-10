FROM openjdk:11.0.9.1
MAINTAINER sunlingfeng & litao build v1
ENV LANG C.UTF-8
EXPOSE 8323
#=====deplory java jar pack=========
COPY ./target/ccme-mathcingengine-2.2.0.jar /opt/app/ccme-mathcingengine-2.2.0.jar
ENTRYPOINT ["java", "-jar", "/opt/app/ccme-mathcingengine-2.2.0-standalone.jar", "run_md_client" ]