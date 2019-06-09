FROM openjdk:8-jre
MAINTAINER Dominic Wood <dominicwood1988@googlemail.com>

ENTRYPOINT ["java", "-jar", "/usr/share/kiwi/kiwi.jar"]

ARG JAR_FILE
ADD target/${JAR_FILE} /usr/share/kiwi/kiwi.jar
