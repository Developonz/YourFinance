// Декларативный пайплайн для мобильного приложения на базе Gradle в среде Windows
pipeline {
    agent any

    // Устанавливаем переменные окружения, чтобы Jenkins знал, 
    // где найти SDK и AVD файлы.
    environment {
        // Путь к SDK
        ANDROID_SDK_ROOT = 'C:\\Users\\zapru\\AppData\\Local\\Android\\Sdk' 
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
                // Если у вас мультимодульный проект (как указано data\src\test\...), 
                // лучше использовать команду для всех модулей:
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
                    // Используем переменные окружения, доступные в Bat командах
                    def adbPath = "%ANDROID_SDK_ROOT%\\platform-tools\\adb.exe"
                    def emulatorPath = "%ANDROID_SDK_ROOT%\\emulator\\emulator.exe"
                    
                    def avdName = 'Medium_Phone_API_36.1' 
                    def emulatorSerial = 'emulator-5554'

                    echo "Настройка и запуск эмулятора: ${avdName}"
                    
                    // --- 1. Очистка и запуск ADB сервера ---
                    bat "\"${adbPath}\" kill-server"
                    bat "\"${adbPath}\" devices"

                    // --- 2. Запуск эмулятора в фоновом режиме (поток) ---
                    echo "Запуск эмулятора ${avdName}..."
                    // start /b "" для стабильного запуска в фоновом режиме на Windows
                    bat "start /b \"\" \"${emulatorPath}\" -avd ${avdName} -no-audio -no-window"

                    // --- 3. Ожидание полной загрузки эмулятора (до 5 минут) ---
                    echo "Ожидание полной загрузки устройства: ${emulatorSerial}..."
                    timeout(time: 5, unit: 'MINUTES') {
                        
                        // 1. Ждем, пока устройство появится в ADB
                        echo "Ожидание появления устройства в ADB..."
                        bat "\"${adbPath}\" -s ${emulatorSerial} wait-for-device"
                        
                        // 2. Ждем, пока ОС Android полностью загрузится (sys.boot_completed == 1)
                        echo "Ожидание загрузки Android OS (sys.boot_completed == 1) с помощью Groovy-цикла..."
                        def bootCompleted = false
                        while (!bootCompleted) {
                            try {
                                // Используем bat с returnStdout: true, чтобы получить вывод команды
                                def output = bat(
                                    script: "\"${adbPath}\" -s ${emulatorSerial} shell getprop sys.boot_completed", 
                                    returnStdout: true // Получаем вывод
                                ).trim()
                                
                                if (output == '1') {
                                    bootCompleted = true
                                    echo "OS fully booted."
                                } else {
                                    echo "Устройство еще не готово. Статус: ${output ?: 'null/empty'}. Ожидание 5 секунд..."
                                    sleep(time: 5, unit: 'SECONDS')
                                }
                            } catch (e) {
                                // Обработка возможной временной ошибки ADB, если команда getprop не сработала
                                echo "Ошибка при проверке загрузки: ${e.getMessage()}. Ожидание 5 секунд..."
                                sleep(time: 5, unit: 'SECONDS')
                            }
                        }

                        // 3. Разблокируем экран
                        bat "\"${adbPath}\" -s ${emulatorSerial} shell input keyevent 82" 
                        echo "Эмулятор готов к работе."
                        
                        // 4. Дополнительная пауза для стабильности
                        echo "Дополнительная пауза 15 секунд для стабилизации ОС..."
                        sleep(time: 15, unit: 'SECONDS')
                    }

                    // --- 4. Запуск тестов ---
                    echo 'Запуск инструментальных (интеграционных/androidTest) тестов...'
                    // ИСПРАВЛЕНИЕ: Добавлены --stacktrace, --info и --rerun-tasks для детальной диагностики
                    bat ".\\gradlew.bat :app:connectedDebugAndroidTest --stacktrace --info --rerun-tasks -Dconnected.device.serial=${emulatorSerial}"

                    // --- 5. Остановка эмулятора ---
                    echo 'Остановка эмулятора...'
                    // Выполняем 'adb emu kill' для чистого завершения
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
                // 2. Публикация отчетов инструментальных тестов
                junit '**/build/outputs/androidTest-results/connected/*.xml'
                // 3. Архивация собранного APK
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