# Шаг 1: Выбираем новый, более надежный базовый образ - Eclipse Temurin OpenJDK 11.
FROM eclipse-temurin:17-jdk

# Шаг 2: Устанавливаем переменные окружения.
ENV ANDROID_SDK_ROOT="/opt/android-sdk"
ENV ANDROID_HOME="/opt/android-sdk"
ENV PATH="${ANDROID_SDK_ROOT}/cmdline-tools/latest/bin:${ANDROID_SDK_ROOT}/platform-tools:${ANDROID_SDK_ROOT}/emulator:${PATH}"

# Шаг 3: Устанавливаем системные зависимости.
# Всегда делаем 'update' и 'install' в одной команде для уменьшения размера образа.
RUN apt-get update && \
    apt-get install -y wget unzip git qemu-kvm && \
    rm -rf /var/lib/apt/lists/*

# Шаг 4: Скачиваем и устанавливаем Android SDK Command-line Tools
RUN mkdir -p ${ANDROID_SDK_ROOT}/cmdline-tools && \
    wget -q "https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip" -O /tmp/cmdline-tools.zip && \
    unzip -q /tmp/cmdline-tools.zip -d ${ANDROID_SDK_ROOT}/cmdline-tools && \
    # Перемещаем содержимое, чтобы путь был /cmdline-tools/latest/bin
    mv ${ANDROID_SDK_ROOT}/cmdline-tools/cmdline-tools ${ANDROID_SDK_ROOT}/cmdline-tools/latest && \
    rm /tmp/cmdline-tools.zip

# Шаг 5: Принимаем лицензии и устанавливаем нужные пакеты SDK.
RUN yes | ${ANDROID_SDK_ROOT}/cmdline-tools/latest/bin/sdkmanager --licenses && \
    ${ANDROID_SDK_ROOT}/cmdline-tools/latest/bin/sdkmanager "platform-tools" "build-tools;34.0.0" "platforms;android-34"

# Шаг 6: Устанавливаем Gradle.
ENV GRADLE_VERSION=8.4
RUN wget -q "https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip" -O /tmp/gradle.zip && \
    unzip -d /opt /tmp/gradle.zip && \
    ln -s /opt/gradle-${GRADLE_VERSION}/bin/gradle /usr/bin/gradle && \
    rm /tmp/gradle.zip

# Шаг 7: Создаем директорию для нашего проекта внутри контейнера.
WORKDIR /app