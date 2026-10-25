# temp container to build using gradle
FROM gradle:7.6.3-jdk17 AS builder

COPY . /home/gradle/src

WORKDIR /home/gradle/src

RUN gradle fatJar --no-daemon --info

# package stage
FROM eclipse-temurin:17-jdk

RUN mkdir -p /srv
WORKDIR /srv
COPY --from=builder /home/gradle/src/build/libs/PlexInformationBot.jar /srv
ENV CONFIG_DIR=/config
EXPOSE 4546
VOLUME /config
CMD ["java", "-jar", "PlexInformationBot.jar"]
