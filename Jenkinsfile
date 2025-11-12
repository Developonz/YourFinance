pipeline {
    // 1. АГЕНТ: НАШ "СЕРВИС СБОРКИ" (Микросервис №1)
    agent {
        docker {
            image 'kayanoterse/my-android-builder:1.1' 
            alwaysPull true 
            
            // КРИТИЧЕСКОЕ ИСПРАВЛЕНИЕ: Принудительно задаем Linux-путь для рабочей директории (-w /app), 
            // Linux-оболочку (--entrypoint=/bin/bash) и Volumes в одной строке.
            // Это должно переопределить автоматические, ошибочные пути Windows, сгенерированные плагином.
            args '-w /app --entrypoint=/bin/bash -v jenkins-gradle-cache:/root/.gradle/caches'
            
            // Сохраняем customWorkspace и reuseNode для поддержки контекста, несмотря на баг.
            customWorkspace '/app' 
            reuseNode true 
        }
    }
    
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
                    def emulatorServiceName = "android-emulator-service"
                    
                    try {
                        // 2.1. ЗАПУСК "СЕРВИСА ТЕСТИРОВАНИЯ" (Микросервис №2)
                        echo "Starting Emulator Service (Container 2: budtmo/docker-android-x86-emulator)..."
                        sh "docker run --name ${emulatorServiceName} -d --privileged -p 5554:5554 budtmo/docker-android-x86-emulator:latest -e DEVICE=\"Samsung Galaxy S10\" -no-audio"
                        
                        // 2.2. ОПИСАНИЕ СВЯЗИ
                        echo "Waiting for Emulator Service to connect via ADB..."
                        def connected = false
                        timeout(time: 5, unit: 'MINUTES') {
                            while (!connected) {
                                try {
                                    sh "adb connect ${emulatorServiceName}:5554" 
                                    def devices = sh(script: "adb devices", returnStdout: true).trim()
                                    
                                    if (devices.contains("${emulatorServiceName}:5554\tdevice")) {
                                        connected = true
                                        echo "Emulator Service connected successfully! (Microservice 1 connected to Microservice 2)"
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
    
    // 3. ОЧИСТКА (POST)
    post {
        always {
            echo 'Pipeline finished.'
        }
        failure {
            echo 'Build failed! Check logs.'
        }
        success {
            echo 'Build and tests completed successfully!'
        }
    }
}