FROM openjdk:8-jre
ADD ./target /target
WORKDIR /target
CMD ["java", "-jar", "organz-server-0.5.jar"]
