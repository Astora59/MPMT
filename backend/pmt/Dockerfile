# ---------- Étape 1 : compilation ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copier pom.xml seul d'abord : les dépendances sont mises en cache
COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src
# Les tests sont exécutés dans le job CI dédié
RUN mvn -B package -DskipTests

# ---------- Étape 2 : exécution ----------
FROM eclipse-temurin:21-jre
WORKDIR /app

# Utilisateur non-root
RUN groupadd --system app && useradd --system --gid app app

COPY --from=build /app/target/*.jar app.jar

USER app
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]