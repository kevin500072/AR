# Build the Ktor API and its shared Kotlin module.
FROM eclipse-temurin:17-jdk-jammy AS build

ENV ANDROID_HOME=/opt/android-sdk
ENV ANDROID_SDK_ROOT=/opt/android-sdk
ENV PATH="$PATH:/opt/android-sdk/cmdline-tools/latest/bin:/opt/android-sdk/platform-tools"

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl unzip \
    && rm -rf /var/lib/apt/lists/* \
    && mkdir -p "$ANDROID_HOME/cmdline-tools" \
    && curl -fsSL https://dl.google.com/android/repository/commandlinetools-linux-15859902_latest.zip -o /tmp/android-cmdline-tools.zip \
    && unzip -q /tmp/android-cmdline-tools.zip -d "$ANDROID_HOME/cmdline-tools" \
    && mv "$ANDROID_HOME/cmdline-tools/cmdline-tools" "$ANDROID_HOME/cmdline-tools/latest" \
    && rm /tmp/android-cmdline-tools.zip \
    && yes | sdkmanager --licenses >/dev/null \
    && sdkmanager "platforms;android-37"

WORKDIR /workspace
COPY . .
RUN chmod +x ./gradlew && ./gradlew :server:installDist --no-daemon

FROM eclipse-temurin:17-jre-jammy AS runtime
ENV APP_ENV=production
WORKDIR /app
COPY --from=build /workspace/server/build/install/server-docker/ ./
EXPOSE 8080
CMD ["./bin/server"]
