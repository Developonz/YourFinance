// Декларативный пайплайн для мобильного приложения на базе Gradle в среде Windows
pipeline {
    agent any

    // Устанавливаем переменные окружения, чтобы Jenkins знал,
    // где найти SDK и AVD файлы.
    environment {
        // Путь к SDK
        ANDROID_SDK_ROOT = 'C:\\Users\\zapru\\AppData\\Local\\Android\\Sdk' // Используем двойной слэш для путей в Groovy
        // Путь к директории, где хранятся INI-файлы AVD
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
                // ИСПРАВЛЕНИЕ: Добавлена смена кодировки для корректного вывода кириллицы.
                // РЕКОМЕНДАЦИЯ: Команда clean удалена, чтобы не замедлять последующие шаги.
                bat 'chcp 65001 && .\\gradlew.bat testDebugUnitTest'
            }
        }

        stage('Build Application') {
            steps {
                echo 'Сборка отладочной (debug) версии приложения...'
                bat 'chcp 65001 && .\\gradlew.bat assembleDebug'
            }
        }

        stage('Run Integration Tests') {
            steps {
                script {
                    // УЛУЧШЕНИЕ: Используем объект env Jenkins для доступа к переменным окружения.
                    // Это более надежный и "Groovy-way" подход.
                    def adbPath = "${env.ANDROID_SDK_ROOT}\\platform-tools\\adb.exe"
                    def emulatorPath = "${env.ANDROID_SDK_ROOT}\\emulator\\emulator.exe"
                    
                    def avdName = 'Medium_Phone_API_36.1'
                    def emulatorSerial = 'emulator-5554'

                    echo "Настройка и запуск эмулятора: ${avdName}"
                    
                    // --- 1. Остановка ADB сервера на всякий случай ---
                    bat "\"${adbPath}\" kill-server"

                    // --- 2. Запуск эмулятора в фоновом режиме ---
                    echo "Запуск эмулятора ${avdName}..."
                    // start /b запускает процесс в фоновом режиме, не блокируя пайплайн
                    bat "start /b \"\" \"${emulatorPath}\" -avd ${avdName} -no-audio -no-window -no-snapshot-load"

                    // --- 3. Ожидание полной загрузки эмулятора (до 5 минут) ---
                    timeout(time: 5, unit: 'MINUTES') {
                        echo "Ожидание появления устройства ${emulatorSerial} в ADB..."
                        bat "\"${adbPath}\" -s ${emulatorSerial} wait-for-device"
                        
                        echo "Ожидание полной загрузки Android OS..."
                        
                        // УЛУЧШЕНИЕ: Использование `waitUntil` делает код более читаемым и идиоматичным
                        waitUntil {
                            try {
                                def bootStatus = bat(
                                    script: "\"${adbPath}\" -s ${emulatorSerial} shell getprop sys.boot_completed",
                                    returnStdout: true
                                ).trim()
                                
                                if (bootStatus == '1') {
                                    echo "ОС полностью загружена."
                                    return true // Выход из цикла waitUntil
                                } else {
                                    echo "Устройство еще не готово. Статус: ${bootStatus}. Ожидание 5 секунд..."
                                    sleep(time: 5, unit: 'SECONDS')
                                    return false // Продолжение цикла
                                }
                            } catch (e) {
                                echo "Ошибка при проверке статуса загрузки: ${e.getMessage()}. Повторная попытка через 5 секунд..."
                                sleep(time: 5, unit: 'SECONDS')
                                return false // Продолжение цикла
                            }
                        }

                        // Разблокируем экран
                        bat "\"${adbPath}\" -s ${emulatorSerial} shell input keyevent 82"
                        echo "Эмулятор готов к работе."
                        
                        // Дополнительная пауза для стабилизации
                        echo "Дополнительная пауза 15 секунд для стабилизации ОС..."
                        sleep(time: 15, unit: 'SECONDS')
                    }

                    // --- 4. Запуск тестов ---
                    echo 'Запуск инструментальных (androidTest) тестов...'
                    // РЕКОМЕНДАЦИЯ: --rerun-tasks полезен для отладки, но для CI его лучше убрать, чтобы использовать кэш Gradle.
                    bat "chcp 65001 && .\\gradlew.bat :app:connectedDebugAndroidTest --stacktrace --info -Dconnected.device.serial=${emulatorSerial}"
                }
            }
        }

        stage('Publish Results & Artifacts') {
            // always() гарантирует, что этот шаг будет выполнен, даже если тесты провалятся,
            // что позволит нам увидеть отчеты о неудачных тестах.
            post {
                always {
                    echo 'Публикация отчетов о тестах и архивирование артефактов...'
                    // ИСПРАВЛЕНИЕ: Используем glob-шаблоны (**/) для поиска XML-отчетов во всех подмодулях.
                    junit '**/build/test-results/testDebugUnitTest/*.xml'
                    
                    // ИСПРАВЛЕНИЕ: Указан корректный путь и шаблон для файлов отчетов.
                    junit 'app/build/outputs/androidTest-results/connected/debug/*.xml'
                    
                    // Архивация собранного APK
                    archiveArtifacts artifacts: 'app/build/outputs/apk/debug/app-debug.apk', fingerprint: true, onlyIfSuccessful: true
                }
            }
        }
    }
    
    // Действия после завершения пайплайна
    post {
        always {
            echo 'Пайплайн завершен. Запуск очистки...'
            script {
                echo 'Остановка эмулятора...'
                def adbPath = "${env.ANDROID_SDK_ROOT}\\platform-tools\\adb.exe"
                def emulatorSerial = 'emulator-5554'

                // УЛУЧШЕНИЕ: Сначала "вежливо" просим эмулятор выключиться через ADB.
                bat(
                    script: "\"${adbPath}\" -s ${emulatorSerial} emu kill",
                    returnStatus: true
                )
                
                // Пауза, чтобы дать процессу завершиться
                sleep(time: 5, unit: 'SECONDS')

                // УЛУЧШЕНИЕ: Силовой метод через taskkill оставлен как запасной вариант.
                bat(
                    script: 'taskkill /F /IM qemu-system-x86_64.exe',
                    returnStatus: true
                )
                bat(
                    script: 'taskkill /F /IM emulator.exe',
                    returnStatus: true
                )
                echo 'Очистка завершена.'
            }
        }
        failure {
            echo 'Сборка не удалась! Проверьте логи.'
        }
        success {
            echo 'Сборка и тесты успешно завершены!'
        }
    }
}