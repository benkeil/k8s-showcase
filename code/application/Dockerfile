FROM amazoncorretto:17 AS builder

WORKDIR /src

COPY .gradle .gradle
COPY gradle gradle
COPY gradlew gradlew
COPY build.gradle.kts build.gradle.kts
COPY settings.gradle.kts settings.gradle.kts
RUN ./gradlew wrapper

COPY . .
RUN ./gradlew code:application:installDist

FROM amazoncorretto:17

WORKDIR /application/
COPY --from=builder /src/code/application/build/install .

ENTRYPOINT ["application/bin/application"]
