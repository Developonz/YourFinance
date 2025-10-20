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
                        bat "\"${adbPath}\" -s ${emulatorSerial} wait-for-device"
                        
                        // 2. Ждем, пока ОС Android полностью загрузится (sys.boot_completed == 1)
                        // Используем корректную логику цикла IF/GOTO для Windows Batch
                        echo "Ожидание загрузки Android OS..."
                        bat '@echo off\n' +
                            ':WAIT_LOOP\n' +
                            "\"${adbPath}\" -s ${emulatorSerial} shell getprop sys.boot_completed > status.txt\n" +
                            'set /p BOOT_STATUS=<status.txt\n' +
                            'if "%BOOT_STATUS%"=="1" goto BOOT_COMPLETE\n' +
                            'echo Device not ready. Waiting 5 seconds...\n' +
                            'timeout /T 5 /NOBREAK >NUL\n' +
                            'goto WAIT_LOOP\n' +
                            ':BOOT_COMPLETE\n' +
                            'del status.txt\n' +
                            'set BOOT_STATUS=' // Очистка переменной
                        
                        // 3. Разблокируем экран
                        bat "\"${adbPath}\" -s ${emulatorSerial} shell input keyevent 82" 
                        echo "Эмулятор готов к работе."
                    }

                    // --- 4. Запуск тестов ---
                    echo 'Запуск инструментальных (интеграционных/androidTest) тестов...'
                    bat ".\\gradlew.bat connectedDebugAndroidTest -Dconnected.device.serial=${emulatorSerial}"

                    // --- 5. Остановка эмулятора ---
                    echo 'Остановка эмулятора...'
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