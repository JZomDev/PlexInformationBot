# temp container to build using gradle
FROM gradle:8.5-jdk21-alpine AS builder

COPY . /home/gradle/src

WORKDIR /home/gradle/src

RUN gradle fatJar --no-daemon --info

# package stage
FROM eclipse-temurin:21-jre

RUN mkdir -p /srv
WORKDIR /srv
COPY --from=builder /home/gradle/src/build/libs/PlexInformationBot.jar /srv
CMD ["java", "-jar", "PlexInformationBot.jar"]
