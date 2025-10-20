
pipeline {
    agent any

    environment {
        // Используем переменную окружения для большей переносимости
        ANDROID_SDK_ROOT = "${System.env.USERPROFILE}\\AppData\\Local\\Android\\Sdk"
        ANDROID_AVD_HOME = "${System.env.USERPROFILE}\\.android\\avd"
    }

    stages {
        stage('Checkout') {
            steps {
                echo 'Исходный код получен.'
                // Шаг checkout SCM добавляется Jenkins автоматически, если Job настроен на получение из Git
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

        stage('Run Integration Tests') {
            steps {
                script {
                    def adbPath = "\"${env.ANDROID_SDK_ROOT}\\platform-tools\\adb.exe\""
                    def emulatorPath = "\"${env.ANDROID_SDK_ROOT}\\emulator\\emulator.exe\""
                    def avdName = 'Medium_Phone_API_36.1'
                    def emulatorSerial = 'emulator-5554'

                    echo "Настройка и запуск эмулятора: ${avdName}"
                    
                    // --- 1. Перезапуск ADB сервера ---
                    bat "${adbPath} kill-server"
                    bat "${adbPath} start-server"

                    // --- 2. Запуск эмулятора ---
                    echo "Запуск эмулятора ${avdName} в фоновом режиме..."
                    bat "start /b \"\" ${emulatorPath} -avd ${avdName} -no-audio -no-window -no-snapshot-load"

                    // --- 3. Ожидание полной загрузки эмулятора ---
                    timeout(time: 5, unit: 'MINUTES') {
                        echo "Ожидание появления устройства ${emulatorSerial} в ADB..."
                        bat "${adbPath} -s ${emulatorSerial} wait-for-device"
                        
                        echo "Ожидание полной загрузки Android OS..."
                        def bootCompleted = false
                        while (!bootCompleted) {
                            def bootStatus = bat(
                                script: "${adbPath} -s ${emulatorSerial} shell getprop sys.boot_completed", 
                                returnStdout: true
                            ).trim()

                            if (bootStatus == '1') {
                                bootCompleted = true
                                echo "ОС полностью загружена."
                            } else {
                                echo "Устройство еще не готово. Статус: ${bootStatus ?: 'не определен'}. Ожидание 5 секунд..."
                                sleep(time: 5, unit: 'SECONDS')
                            }
                        }

                        // --- 4. Разблокировка экрана и дополнительная пауза ---
                        bat "${adbPath} -s ${emulatorSerial} shell input keyevent 82"
                        echo "Эмулятор разблокирован. Дополнительная пауза 15 секунд для стабилизации."
                        sleep(time: 15, unit: 'SECONDS')
                    }

                    // --- 5. Запуск тестов ---
                    echo 'Запуск инструментальных (androidTest) тестов...'
                    bat ".\\gradlew.bat :app:connectedDebugAndroidTest --stacktrace -Dconnected.device.serial=${emulatorSerial}"
                }
            }
        }

        stage('Publish Results & Artifacts') {
            // Этот шаг выполняется всегда после тестов, даже если они упали, чтобы собрать отчеты
            post {
                always {
                    echo 'Публикация отчетов о тестах и архивирование артефактов...'
                    
                    // Публикация отчетов unit-тестов
                    junit '**/build/test-results/testDebugUnitTest/**/*.xml'
                    
                    // Публикация отчетов инструментальных тестов
                    junit 'app/build/outputs/androidTest-results/connected/debug/*.xml'
                    
                    // Архивация APK только в случае успеха
                    archiveArtifacts artifacts: 'app/build/outputs/apk/debug/app-debug.apk', fingerprint: true, onlyIfSuccessful: true
                }
            }
        }
    }
    
    post {
        always {
            echo 'Пайплайн завершен. Остановка эмулятора...'
            script {
                // Надежно останавливаем все процессы эмулятора.
                // returnStatus: true предотвращает ошибку, если процесс уже завершен.
                bat(
                    script: 'taskkill /F /IM emulator.exe',
                    returnStatus: true
                )
                bat(
                    script: '"%ANDROID_SDK_ROOT%\\platform-tools\\adb.exe" kill-server',
                    returnStatus: true
                )
                echo 'Очистка завершена.'
            }
        }
        success {
            echo 'Сборка и тесты успешно завершены!'
        }
        failure {
            echo 'Сборка не удалась! Проверьте логи.'
        }
    }
}
