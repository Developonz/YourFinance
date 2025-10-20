// Декларативный пайплайн для мобильного приложения на базе Gradle в среде Windows
pipeline {
    // Используем любой доступный агент, но команды должны быть адаптированы для Windows
    agent any

    // Устанавливаем переменную окружения ANDROID_SDK_ROOT, 
    // чтобы Gradle знал, где найти SDK.
    environment {
         // !!! ПУТЬ К SDK, который уже был корректен в предыдущих логах !!!
         ANDROID_SDK_ROOT = 'C:\\Users\\zapru\\AppData\\Local\\Android\\Sdk' 
         ANDROID_AVD_HOME = 'C:\\Users\\zapru\\.android\\avd'
    }
    
    stages {
        stage('Checkout') {
            steps {
                echo 'Исходный код получен.'
            }
        }

        stage('Run Unit Tests') {
            steps {
                echo 'Запуск модульных (unit) тестов...'
                // Убедитесь, что Java и gradlew.bat доступны в PATH агента.
                bat '.\\gradlew.bat clean testDebugUnitTest'
            }
        }

        stage('Build Application') {
            steps {
                echo 'Сборка отладочной (debug) версии приложения...'
                bat '.\\gradlew.bat assembleDebug'
            }
        }

        stage('Run Integration Tests (Требуется эмулятор/устройство!)') {
            steps {
                script {
                    def adbPath = "%ANDROID_SDK_ROOT%\\platform-tools\\adb.exe"
                    def emulatorPath = "%ANDROID_SDK_ROOT%\\emulator\\emulator.exe"
                    
                    // !!! ВАЖНО: ЗАМЕНИТЕ ЭТОТ PLACEHOLDER НА ТОЧНОЕ ИМЯ ВАШЕГО AVD (Действие 1) !!!
                    def avdName = 'Medium_Phone_API_36.1' 
                    
                    // Серийный номер, который будет использовать ADB для таргетирования.
                    // Для первого запущенного эмулятора это почти всегда emulator-5554.
                    def emulatorSerial = 'emulator-5554'

                    echo "Настройка и запуск эмулятора: ${avdName}"
                    
                    // --- 1. Очистка и запуск ADB сервера ---
                    bat "\"${adbPath}\" kill-server"
                    bat "\"${adbPath}\" devices" // Просто для диагностики

                    // --- 2. Запуск эмулятора в фоновом режиме (поток) ---
                    echo "Запуск эмулятора ${avdName}..."
                    try {
                        // Используем start /b "" для стабильного запуска в фоновом режиме на Windows
                        bat "start /b \"\" \"${emulatorPath}\" -avd ${avdName} -no-audio -no-window"
                    } catch (e) {
                        echo "Не удалось запустить эмулятор. Убедитесь, что AVD с именем '${avdName}' существует."
                        error "Сбой при запуске эмулятора."
                    }


                    // --- 3. Ожидание полной загрузки эмулятора (до 5 минут) ---
                    echo "Ожидание полной загрузки устройства: ${emulatorSerial}..."
                    timeout(time: 5, unit: 'MINUTES') {
                        // Явно ждем только нужный нам серийный номер
                        bat "\"${adbPath}\" -s ${emulatorSerial} wait-for-device"
                        // Разблокируем экран
                        bat "\"${adbPath}\" -s ${emulatorSerial} shell input keyevent 82" 
                        echo "Эмулятор готов к работе."
                    }

                    // --- 4. Запуск тестов, явно указывая серийный номер для избежания "more than one device" ---
                    echo 'Запуск инструментальных (интеграционных/androidTest) тестов...'
                    // -Dconnected.device.serial=${emulatorSerial} заставляет Gradle таргетировать только этот эмулятор.
                    bat ".\\gradlew.bat connectedDebugAndroidTest -Dconnected.device.serial=${emulatorSerial}"

                    // --- 5. Остановка эмулятора ---
                    echo 'Остановка эмулятора...'
                    // Отправляем команду на завершение работы эмулятора
                    bat "\"${adbPath}\" -s ${emulatorSerial} emu kill"

                    sleep(time: 10, unit: 'SECONDS')
                }
            }
        }

        stage('Publish Results & Artifacts') {
            steps {
                echo 'Публикация отчетов о тестах и архивирование артефактов...'

                // 1. Публикация отчетов модульных тестов
                junit '**/build/test-results/testDebugUnitTest/**/*.xml'

                // 2. Публикация отчетов инструментальных тестов (если запускались)
                junit '**/build/outputs/androidTest-results/connected/*.xml'

                // 3. Архивация собранного APK (для скачивания)
                archiveArtifacts artifacts: 'app/build/outputs/apk/debug/app-debug.apk', fingerprint: true, onlyIfSuccessful: true
            }
        }
    }
    
    // Действия после завершения пайплайна
    post {
        always {
            echo 'Пайплайн завершен.'
        }
        failure {
            echo 'Сборка не удалась! Проверьте логи.'
        }
        success {
            echo 'Сборка и тесты успешно завершены!'
        }
    }
}
