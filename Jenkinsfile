// Декларативный пайплайн для мобильного приложения на базе Gradle в среде Windows
pipeline {
    agent any

    // Параметры для настройки сборки
    parameters {
        string(name: 'AVD_NAME', defaultValue: 'Medium_Phone_API_36.1', description: 'AVD emulator name for running instrumentation tests')
    }

    // Переменные окружения для Android SDK и AVD
    environment {
        // ИСПРАВЛЕНИЕ: Используем ANDROID_HOME, так как Gradle/AGP на Windows часто требует именно эту переменную.
        // Используем переменную %USERPROFILE% для универсальности на любой машине Windows.
        ANDROID_HOME = '%USERPROFILE%\\AppData\\Local\\Android\\Sdk' 
        ANDROID_AVD_HOME = '%USERPROFILE%\\.android\\avd' 
    }
    
    stages {
        stage('Checkout') {
            steps {
                echo 'Checking out source code.'
                // Получение кода из SCM
                // checkout scm
            }
        }

        stage('Run Unit Tests') {
            steps {
                echo 'Running Unit Tests...'
                bat '.\\gradlew.bat clean testDebugUnitTest'
            }
        }

        stage('Build Application') {
            steps {
                echo 'Building Debug Application...'
                bat '.\\gradlew.bat assembleDebug'
            }
        }

        stage('Run Integration Tests') {
            steps {
                script {
                    // Используем ANDROID_HOME, которая теперь определена в блоке environment
                    def adbPath = "%ANDROID_HOME%\\platform-tools\\adb.exe"
                    def emulatorPath = "%ANDROID_HOME%\\emulator\\emulator.exe"
                    def emulatorSerial = 'emulator-5554'

                    echo "Setting up and starting emulator: ${params.AVD_NAME}"
                    
                    // Убиваем и перезапускаем ADB сервер
                    bat "\"${adbPath}\" kill-server"
                    bat "\"${adbPath}\" devices"

                    // Запускаем эмулятор в фоновом режиме
                    bat "start /b \"\" \"${emulatorPath}\" -avd ${params.AVD_NAME} -no-audio -no-window"

                    // Ожидание полной загрузки эмулятора (до 5 минут)
                    timeout(time: 5, unit: 'MINUTES') {
                        
                        echo "Waiting for device to appear in ADB..."
                        bat "\"${adbPath}\" -s ${emulatorSerial} wait-for-device"
                        
                        echo "Waiting for Android OS to boot (sys.boot_completed == 1)..."
                        def bootCompleted = false
                        while (!bootCompleted) {
                            def bootStatus = '0'
                            try {
                                def rawOutput = bat(
                                    script: "\"${adbPath}\" -s ${emulatorSerial} shell getprop sys.boot_completed", 
                                    returnStdout: true 
                                )
                                
                                def outputLines = rawOutput.split('\n').collect{ it.trim() }.findAll{ !it.isEmpty() }
                                bootStatus = outputLines.isEmpty() ? '0' : outputLines.last().replaceAll(/[^0-1]/, '').trim()
                                
                                if (bootStatus == '1') {
                                    bootCompleted = true
                                    echo "OS fully booted."
                                } else {
                                    echo "Device is not ready. Waiting 5 seconds..."
                                    sleep(time: 5, unit: 'SECONDS')
                                }
                            } catch (e) {
                                echo "Error during boot check: ${e.getMessage()}. Waiting 5 seconds..."
                                sleep(time: 5, unit: 'SECONDS')
                            }
                        }

                        // Разблокируем экран
                        bat "\"${adbPath}\" -s ${emulatorSerial} shell input keyevent 82" 
                        echo "Emulator is ready."
                        
                        // Дополнительная пауза для стабильности
                        sleep(time: 15, unit: 'SECONDS')
                    }

                    // Запуск инструментальных тестов
                    echo 'Running Instrumentation Tests...'
                    bat ".\\gradlew.bat :app:connectedDebugAndroidTest --stacktrace --info --rerun-tasks -Dconnected.device.serial=${emulatorSerial}"
                }
            }
        }

        stage('Publish Results & Artifacts') {
            steps {
                echo 'Publishing Test Reports & Artifacts...'
                // Публикация отчетов модульных тестов
                junit '**/build/test-results/testDebugUnitTest/**/*.xml'
                // Публикация отчетов инструментальных тестов
                junit 'app/build/outputs/androidTest-results/connected/debug/*.xml'
                // Архивирование собранного APK
                archiveArtifacts artifacts: 'app/build/outputs/apk/debug/app-debug.apk', fingerprint: true, onlyIfSuccessful: true
            }
        }
    }
    
    // Действия после завершения пайплайна
    post {
        always {
            echo 'Pipeline finished. Starting cleanup...'
            script {
                echo 'Stopping emulator and cleaning workspace...'
                // Очистка рабочего пространства
                bat '.\\gradlew.bat clean'
                // Принудительное завершение процессов эмулятора. returnStatus: true игнорирует ошибки, если процесс не найден.
                bat(
                    script: 'taskkill /F /IM qemu-system-x86_64.exe',
                    returnStatus: true
                )
                bat(
                    script: 'taskkill /F /IM emulator.exe',
                    returnStatus: true
                )
            }
        }
        failure {
            echo 'Build failed! Check logs.'
        }
        success {
            echo 'Build and tests completed successfully!'
        }
    }
}
