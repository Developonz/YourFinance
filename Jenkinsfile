// Декларативный пайплайн для мобильного приложения на базе Gradle в среде Windows
pipeline {
    agent any

    environment {
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
                    def adbPath = "${env.ANDROID_SDK_ROOT}\\platform-tools\\adb.exe"
                    def emulatorPath = "${env.ANDROID_SDK_ROOT}\\emulator\\emulator.exe"
                    def avdName = 'Medium_Phone_API_36.1'
                    def emulatorSerial = 'emulator-5554'

                    echo "Настройка и запуск эмулятора: ${avdName}"
                    
                    bat "\"${adbPath}\" kill-server"
                    echo "Запуск эмулятора ${avdName}..."
                    bat "start /b \"\" \"${emulatorPath}\" -avd ${avdName} -no-audio -no-window -no-snapshot-load"

                    timeout(time: 5, unit: 'MINUTES') {
                        echo "Ожидание появления устройства ${emulatorSerial} в ADB..."
                        bat "\"${adbPath}\" -s ${emulatorSerial} wait-for-device"
                        
                        echo "Ожидание полной загрузки Android OS..."
                        waitUntil {
                            try {
                                def bootStatus = bat(
                                    script: "\"${adbPath}\" -s ${emulatorSerial} shell getprop sys.boot_completed",
                                    returnStdout: true
                                ).trim()
                                
                                if (bootStatus == '1') {
                                    echo "ОС полностью загружена."
                                    return true
                                } else {
                                    echo "Устройство еще не готово. Статус: ${bootStatus ?: 'null/empty'}. Ожидание 5 секунд..."
                                    sleep(time: 5, unit: 'SECONDS')
                                    return false
                                }
                            } catch (e) {
                                echo "Ошибка при проверке статуса загрузки: ${e.getMessage()}. Повторная попытка через 5 секунд..."
                                sleep(time: 5, unit: 'SECONDS')
                                return false
                            }
                        }

                        bat "\"${adbPath}\" -s ${emulatorSerial} shell input keyevent 82"
                        echo "Эмулятор готов к работе."
                        
                        echo "Дополнительная пауза 15 секунд для стабилизации ОС..."
                        sleep(time: 15, unit: 'SECONDS')
                    }

                    echo 'Запуск инструментальных (androidTest) тестов...'
                    bat "chcp 65001 && .\\gradlew.bat :app:connectedDebugAndroidTest --stacktrace --info -Dconnected.device.serial=${emulatorSerial}"
                }
            }
        }
    }
    
    // ИСПРАВЛЕНИЕ: Все действия после сборки и тестов перенесены сюда.
    post {
        always {
            // --- 1. Публикация результатов ---
            echo 'Публикация отчетов о тестах и архивирование артефактов...'
            // Используем glob-шаблоны (**/) для поиска XML-отчетов во всех подмодулях.
            junit '**/build/test-results/testDebugUnitTest/*.xml'
            junit 'app/build/outputs/androidTest-results/connected/debug/*.xml'
            
            // --- 2. Архивация артефактов (только при успехе) ---
            // Этот шаг будет выполнен только если пайплайн дошел до конца без ошибок 'failure'.
            // Если тесты упадут, пайплайн будет 'unstable', и артефакт сохранится.
            archiveArtifacts artifacts: 'app/build/outputs/apk/debug/app-debug.apk', fingerprint: true, allowEmptyArchive: true

            // --- 3. Очистка ---
            echo 'Пайплайн завершен. Запуск очистки...'
            script {
                echo 'Остановка эмулятора...'
                def adbPath = "${env.ANDROID_SDK_ROOT}\\platform-tools\\adb.exe"
                def emulatorSerial = 'emulator-5554'

                bat(script: "\"${adbPath}\" -s ${emulatorSerial} emu kill", returnStatus: true)
                sleep(time: 5, unit: 'SECONDS')
                bat(script: 'taskkill /F /IM qemu-system-x86_64.exe', returnStatus: true)
                bat(script: 'taskkill /F /IM emulator.exe', returnStatus: true)
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