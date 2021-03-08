#FROM adoptopenjdk/openjdk8
FROM 11.0.9.1-oraclelinux7
MAINTAINER sunlingfeng & litao build v1

WORKDIR /opt/app/


#ENV LANG C.UTF-8
#ENV JAVA_HOME /usr/lib/jvm/java-1.8-openjdk/jre
#ENV PATH $PATH:/usr/lib/jvm/java-1.8-openjdk/jre/bin:/usr/lib/jvm/java-1.8-openjdk/bin
##ENV JAVA_VERSION 8u20

#=====add sources================
#RUN mkdir -p /tmp/logs

#=====yum install pack==========

#=====mava install pack==========

#=====deplory java jar pack=========
COPY ./marketdata/target/ccme-marketdata-2.2.0-standalone.jar /opt/app/application.jar
#ADD quickfixj-server.cfg /opt/app/quickfixj-server.cfg


#EXPOSE 9876,9877,9878,9879,9880,9881,8080,8000

CMD["java","-cp /ccme/marketdata/target/ccme-marketdata-2.2.0-standalone.jar quickfix.examples.executor.MarketDataServer"]
#ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -jar /opt/app/application.jar" ]
