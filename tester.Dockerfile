# ==================================================================
# Dockerfile для ТЕСТИРУЮЩЕГО контейнера (tester) - ФИНАЛЬНАЯ ВЕРСИЯ v2
# ==================================================================

FROM my-android-builder:latest

# Шаг 2: Устанавливаем системные зависимости для эмулятора.
# ИСПРАВЛЕНИЕ: libasound2 заменен на libasound2t64 для совместимости
# с новыми версиями Ubuntu (24.04+).
RUN apt-get update && \
    apt-get install -y --no-install-recommends qemu-kvm libx11-6 libnss3 libpulse0 libasound2t64 && \
    rm -rf /var/lib/apt/lists/*

# Шаг 3: Устанавливаем компоненты SDK, необходимые для эмуляции.
RUN sdkmanager "emulator" "system-images;android-34;google_apis;x86_64"

# Шаг 4: Заранее создаем виртуальное устройство (AVD).
RUN echo "no" | avdmanager create avd \
    --name "Medium_Phone_API_34" \
    --package "system-images;android-34;google_apis;x86_64" \
    --device "pixel_6" \
    --force