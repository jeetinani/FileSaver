FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
RUN mkdir -p testing
COPY . .
RUN mvn clean package
FROM openjdk:17
WORKDIR /app
COPY --from=build /app/target/filesaver-application.jar .
#needs to commented if not present
#COPY config ./config
#needs to commented if not present
#COPY bank.db .
EXPOSE 8081
ENTRYPOINT [ "java", "-jar", "filesaver-application.jar"]