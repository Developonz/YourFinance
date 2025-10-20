// Декларативный пайплайн для мобильного приложения на базе Gradle в среде Windows
pipeline {
    // Используем любой доступный агент, но команды должны быть адаптированы для Windows
    agent any

    // Устанавливаем переменную окружения ANDROID_SDK_ROOT, 
    // чтобы Gradle знал, где найти SDK.
    environment {
         // !!! ПУТЬ К SDK !!!
         ANDROID_SDK_ROOT = 'C:\\Users\\zapru\\AppData\\Local\\Android\\Sdk' 
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
            // Использование эмулятора - наиболее стабильный способ для CI.
            steps {
                script {
                    def adbPath = "%ANDROID_SDK_ROOT%\\platform-tools\\adb.exe"
                    def emulatorPath = "%ANDROID_SDK_ROOT%\\emulator\\emulator.exe"
                    def avdName = 'Medium_Phone_API_36.1' // !!! ЗАМЕНИТЕ НА ИМЯ ВАШЕГО AVD (например, Pixel_3_API_29) !!!

                    echo "Настройка и запуск эмулятора: ${avdName}"
                    
                    // --- 1. Очистка и запуск ADB сервера ---
                    bat "\"${adbPath}\" kill-server"
                    bat "\"${adbPath}\" devices" // Просто для диагностики

                    // --- 2. Запуск эмулятора в фоновом режиме (поток) ---
                    // 'start /b' запускает команду и сразу возвращает управление, чтобы пайплайн не завис.
                    // -no-audio -no-window уменьшают нагрузку
                    echo "Запуск эмулятора ${avdName}..."
                    // Используем try/catch на случай, если adb/emulator не найдены
                    try {
                        bat "start /b \"\" \"${emulatorPath}\" -avd ${avdName} -no-audio -no-window"
                    } catch (e) {
                        echo "Не удалось запустить эмулятор. Убедитесь, что AVD с именем '${avdName}' существует, и путь к SDK правильный."
                        error "Сбой при запуске эмулятора."
                    }


                    // --- 3. Ожидание полной загрузки эмулятора (до 5 минут) ---
                    echo "Ожидание полной загрузки устройства..."
                    // Ожидаем, пока ADB не увидит активное устройство
                    // Используем таймаут в 300 секунд (5 минут)
                    timeout(time: 5, unit: 'MINUTES') {
                        bat "\"${adbPath}\" wait-for-device"
                        // Проверяем готовность ОС Android
                        bat "\"${adbPath}\" shell input keyevent 82" // Разблокируем экран
                        echo "Эмулятор готов к работе."
                    }

                    // --- 4. Запуск тестов ---
                    echo 'Запуск инструментальных (интеграционных/androidTest) тестов...'
                    bat '.\\gradlew.bat connectedDebugAndroidTest'

                    // --- 5. Остановка эмулятора ---
                    echo 'Остановка эмулятора...'
                    // Отправляем команду на завершение работы эмулятора
                    bat "\"${adbPath}\" emu kill"

                    // Добавляем паузу для завершения процесса, хотя 'emu kill' должен быть быстрым
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
