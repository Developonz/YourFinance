pipeline {
    // 1. АГЕНТ: НАШ "СЕРВИС СБОРКИ" (Микросервис №1)
    agent {
        docker {
            image 'kayanoterse/my-android-builder:1.1' 
            alwaysPull true 
            
            // ФИНАЛЬНОЕ ИСПРАВЛЕНИЕ: Передаем Docker Volumes через args.
            // Это решает проблему "Invalid config option volumes".
            // -v jenkins-gradle-cache:/root/.gradle/caches - это команда монтирования Volume.
            args '-v jenkins-gradle-cache:/root/.gradle/caches'
        }
    }
    
    // Параметры удалены
    
    stages {
        
        stage('Run Unit Tests') {
            steps {
                echo 'Running Unit Tests in Build Service (Container 1)...'
                // Команды Linux (sh)
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

        // 2. МИКРОСЕРВИСНАЯ СТАДИЯ: Взаимодействие Контейнеров
        stage('Run Integration Tests') {
            steps {
                script {
                    // Имя, которое будет использоваться как СЕТЕВОЙ АДРЕС для Контейнера 2.
                    def emulatorServiceName = "android-emulator-service"
                    
                    try {
                        // 2.1. ЗАПУСК "СЕРВИСА ТЕСТИРОВАНИЯ" (Микросервис №2)
                        echo "Starting Emulator Service (Container 2: budtmo/docker-android-x86-emulator)..."
                        // Запускаем второй контейнер, который является нашим вторым микросервисом.
                        sh "docker run --name ${emulatorServiceName} -d --privileged -p 5554:5554 budtmo/docker-android-x86-emulator:latest -e DEVICE=\"Samsung Galaxy S10\" -no-audio"
                        
                        // 2.2. ОПИСАНИЕ СВЯЗИ: Ожидание подключения по сетевому имени
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

                        // 2.3. ЗАПУСК ТЕСТОВ
                        echo 'Running Instrumentation Tests...'
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
    
    // 3. ОЧИСТКА (POST): Docker сам удалит контейнер сборки.
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