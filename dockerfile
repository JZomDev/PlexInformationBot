# temp container to build using gradle
FROM gradle:7.6.3-jdk17-alpine AS builder

COPY . /home/gradle/src

WORKDIR /home/gradle/src

RUN gradle fatJar --no-daemon --info

# package stage
FROM openjdk:17

RUN mkdir -p /srv
WORKDIR /srv
COPY --from=builder /home/gradle/src/build/libs/PlexInformationBot.jar /srv
CMD ["java", "-jar", "PlexInformationBot.jar"]
