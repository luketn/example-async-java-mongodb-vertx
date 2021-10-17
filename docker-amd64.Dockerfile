# Jlink a custom version of the JDK ('JRE')
FROM eclipse-temurin:16-jdk-alpine as jre-build
RUN apk add --no-cache binutils
RUN $JAVA_HOME/bin/jlink \
         --add-modules "$(java --list-modules | sed -e 's/@[0-9].*$/,/' | tr -d \\n)" \
         --strip-debug \
         --no-man-pages \
         --no-header-files \
         --compress=2 \
         --output /javaruntime

# Build our image
FROM alpine:3.14.2
ENV JAVA_HOME=/opt/openjdk
ENV PATH "${JAVA_HOME}/bin:${PATH}"
COPY --from=jre-build /javaruntime $JAVA_HOME

WORKDIR /opt
COPY target/example-async-mongodb-1.0.0-SNAPSHOT-fat.jar example-async-mongodb.jar
COPY prod-conf.json conf.json
CMD java -Dhttp_port="${HTTP_PORT:-8080}" \
         -Dconnection_string="${CONNECTION_STRING:-mongodb://mongo:27017}" \
         -jar example-async-mongodb.jar
