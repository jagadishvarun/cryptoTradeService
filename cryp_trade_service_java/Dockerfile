FROM ubuntu:20.04
RUN apt-get update && apt-get -y install openjdk-11-jdk-headless && rm -rf /var/lib/apt
ARG JAR_FILE=target/tradeService.jar
ADD ${JAR_FILE} tradeService.jar
EXPOSE 9030
ENTRYPOINT ["java","-jar","/tradeService.jar"]