pipeline {
    // 1. ОБЩИЙ АГЕНТ: НАШ "СЕРВИС СБОРКИ" (Микросервис №1)
    agent {
        docker {
            // Jenkins запустит контейнер на основе нашего образа
            image 'kayanoterse/my-android-builder:1.1' 
            alwaysPull true 
            
            // 2. DOCKER VOLUMES (Тома) для кэширования Gradle. 
            // Это ускоряет повторные сборки.
            volumes {
                // ИСПРАВЛЕНИЕ ОШИБКИ: Правильный синтаксис для named volume
                volume('jenkins-gradle-cache', '/root/.gradle/caches') 
            }
        }
    }

    // Параметры удалены, т.к. больше не нужны.
    
    stages {
        
        stage('Run Unit Tests') {
            steps {
                echo 'Running Unit Tests in Build Service (Container 1)...'
                // Переход на команды Linux (sh)
                sh 'chmod +x ./gradlew' 
                sh './gradlew clean testDebugUnitTest'
            }
        }

        stage('Build Application') {
            steps {
                echo 'Building Debug Application in Build Service (Container 1)...'
                sh './gradlew assembleDebug'
            }
        }

        // 3. МИКРОСЕРВИСНАЯ СТАДИЯ (Два контейнера)
        stage('Run Integration Tests') {
            steps {
                script {
                    // Имя, которое будет использоваться как сетевой адрес для Контейнера 2.
                    def emulatorServiceName = "android-emulator-service"
                    
                    try {
                        // 3.1. ЗАПУСК "СЕРВИСА ТЕСТИРОВАНИЯ" (Микросервис №2)
                        // Запускаем второй контейнер (эмулятор) в фоновом режиме (-d).
                        echo "Starting Emulator Service (Container 2: budtmo/docker-android-x86-emulator)..."
                        sh "docker run --name ${emulatorServiceName} -d --privileged -p 5554:5554 budtmo/docker-android-x86-emulator:latest -e DEVICE=\"Samsung Galaxy S10\" -no-audio"
                        
                        // 3.2. ОПИСАНИЕ СВЯЗИ: Контейнер 1 (Build) подключается к Контейнеру 2 (Emulator)
                        echo "Waiting for Emulator Service to connect via ADB..."
                        def connected = false
                        timeout(time: 5, unit: 'MINUTES') {
                            while (!connected) {
                                try {
                                    // adb connect использует сетевое имя 'android-emulator-service'
                                    sh "adb connect ${emulatorServiceName}:5554" 
                                    def devices = sh(script: "adb devices", returnStdout: true).trim()
                                    
                                    if (devices.contains("${emulatorServiceName}:5554\tdevice")) {
                                        connected = true
                                        echo "Emulator Service connected successfully!"
                                    } else {
                                        echo "Not ready yet. Waiting 10 seconds..."
                                        sleep(time: 10, unit: 'SECONDS')
                                    }
                                } catch (e) {
                                    echo "Connection attempt failed. Retrying... (${e.getMessage()})"
                                    sleep(time: 10, unit: 'SECONDS')
                                }
                            }
                        }

                        // 3.3. ЗАПУСК ТЕСТОВ
                        sh 'echo Running Instrumentation Tests...'
                        sh "./gradlew :app:connectedDebugAndroidTest --stacktrace --info --rerun-tasks -Dconnected.device.serial=${emulatorServiceName}:5554"
                    
                    } finally {
                        // Очистка: остановка и удаление фонового контейнера эмулятора.
                        sh 'echo Stopping and removing Emulator Service...'
                        sh "docker stop ${emulatorServiceName} || true" 
                        sh "docker rm ${emulatorServiceName} || true" 
                    }
                }
            }
        }

        stage('Publish Results & Artifacts') {
            steps {
                echo 'Publishing Test Reports & Artifacts...'
                junit '**/build/test-results/testDebugUnitTest/**/*.xml'
                junit 'app/build/outputs/androidTest-results/connected/debug/*.xml'
                archiveArtifacts artifacts: 'app/build/outputs/apk/debug/app-debug.apk', fingerprint: true, onlyIfSuccessful: true
            }
        }
    }
    
    // 4. ОЧИСТКА (POST): Docker сам удалит контейнер сборки, нам нужна только чистка проекта.
    post {
        always {
            echo 'Pipeline finished.'
            sh './gradlew clean' 
        }
        failure {
            echo 'Build failed! Check logs.'
        }
        success {
            echo 'Build and tests completed successfully!'
        }
    }
}