#FROM adoptopenjdk/openjdk8
FROM openjdk:11.0.9.1
MAINTAINER sunlingfeng & litao build v1
ENV LANG C.UTF-8
#=====deplory java jar pack=========
COPY ./marketdata/target/ccme-marketdata-2.2.0-standalone.jar /app/application.jar