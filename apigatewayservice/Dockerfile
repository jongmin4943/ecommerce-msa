FROM openjdk:17-ea-11-jdk-slim
VOLUME /tmp
COPY build/libs/apigatewayservice-0.0.1-SNAPSHOT.jar ApiGatewayService.jar
ENTRYPOINT ["java","-jar","ApiGatewayService.jar"]