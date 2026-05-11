FROM maven:3-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY chinesecheckers-backend/pom.xml chinesecheckers-backend/
COPY chinesecheckers-frontend/pom.xml chinesecheckers-frontend/
COPY chinesecheckers-frontend/package.json chinesecheckers-frontend/package-lock.json chinesecheckers-frontend/
COPY chinesecheckers-backend/src chinesecheckers-backend/src
COPY chinesecheckers-frontend/src chinesecheckers-frontend/src
COPY chinesecheckers-frontend/index.html chinesecheckers-frontend/
COPY chinesecheckers-frontend/vite.config.ts chinesecheckers-frontend/
COPY chinesecheckers-frontend/tsconfig.json chinesecheckers-frontend/tsconfig.app.json chinesecheckers-frontend/tsconfig.node.json chinesecheckers-frontend/
COPY chinesecheckers-frontend/eslint.config.js chinesecheckers-frontend/
RUN mvn package -DskipTests -q

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/chinesecheckers-backend/target/*.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]
