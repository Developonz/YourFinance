# ==================================================================
# Dockerfile для СБОРОЧНОГО контейнера (builder)
# Задача: компиляция кода, сборка APK, запуск unit-тестов.
# ==================================================================

# Шаг 1: Используем официальный образ с Java 17.
FROM eclipse-temurin:17-jdk

# Шаг 2: Устанавливаем переменные окружения для Android SDK.
# Это стандартная практика для Android-сборок.
ENV ANDROID_SDK_ROOT="/opt/android-sdk"
ENV ANDROID_HOME="/opt/android-sdk"
# Добавляем инструменты SDK в системный PATH, чтобы их можно было вызывать напрямую.
ENV PATH="${ANDROID_SDK_ROOT}/cmdline-tools/latest/bin:${ANDROID_SDK_ROOT}/platform-tools:${PATH}"

# Шаг 3: Устанавливаем минимально необходимые системные зависимости.
# git - для работы с репозиториями (может понадобиться Jenkins)
# wget, unzip - для скачивания и распаковки SDK
# 'qemu-kvm' здесь НЕ нужен, так как этот контейнер не запускает эмулятор.
RUN apt-get update && \
    apt-get install -y --no-install-recommends wget unzip git && \
    # Очищаем кэш apt, чтобы уменьшить размер итогового образа.
    rm -rf /var/lib/apt/lists/*

# Шаг 4: Скачиваем и устанавливаем Android SDK Command-line Tools.
# Используем конкретную версию для воспроизводимости сборок.
# Ссылка актуальна на момент написания.
RUN mkdir -p ${ANDROID_SDK_ROOT}/cmdline-tools && \
    wget -q "https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip" -O /tmp/cmdline-tools.zip && \
    unzip -q /tmp/cmdline-tools.zip -d ${ANDROID_SDK_ROOT}/cmdline-tools && \
    # Google упаковывает инструменты в дополнительную папку, перемещаем их на уровень выше.
    mv ${ANDROID_SDK_ROOT}/cmdline-tools/cmdline-tools ${ANDROID_SDK_ROOT}/cmdline-tools/latest && \
    rm /tmp/cmdline-tools.zip

# Шаг 5: Принимаем лицензии и устанавливаем нужные пакеты SDK для СБОРКИ.
# Эмулятор и образы системы здесь не нужны.
RUN yes | sdkmanager --licenses > /dev/null && \
    sdkmanager "platform-tools" "build-tools;34.0.0" "platforms;android-34"

# Шаг 6: Создаем рабочую директорию для проекта.
# Jenkins будет монтировать код проекта в эту папку.
WORKDIR /app