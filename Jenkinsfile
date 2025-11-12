pipeline {
    // 1. АГЕНТ: НАШ "СЕРВИС СБОРКИ" (Микросервис №1)
    agent {
        docker {
            image 'kayanoterse/my-android-builder:1.1' 
            alwaysPull true 
            
            // ИСПРАВЛЕНИЕ: Force Jenkins to use a Linux-style path for the workspace
            customWorkspace '/app' 
            
            // ИСПРАВЛЕНИЕ: Reuse the container for the entire execution (canonical fix for Windows path issues)
            reuseNode true 
            
            // Pass volume for caching
            args '-v jenkins-gradle-cache:/root/.gradle/caches'
        }
    }
    
    stages {
        
        // 2. ИЗМЕНЕНИЯ В СТАДИЯХ: Удаляем стадию 'Prepare Workspace'
        // SCM checkout теперь произойдет автоматически в начале execution flow, 
        // и благодаря reuseNode/customWorkspace, workspace будет доступен как /app.

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

        // 3. МИКРОСЕРВИСНАЯ СТАДИЯ: Взаимодействие Контейнеров
        stage('Run Integration Tests') {
            steps {
                script {
                    def emulatorServiceName = "android-emulator-service"
                    
                    try {
                        // 3.1. ЗАПУСК "СЕРВИСА ТЕСТИРОВАНИЯ" (Микросервис №2)
                        echo "Starting Emulator Service (Container 2: budtmo/docker-android-x86-emulator)..."
                        sh "docker run --name ${emulatorServiceName} -d --privileged -p 5554:5554 budtmo/docker-android-x86-emulator:latest -e DEVICE=\"Samsung Galaxy S10\" -no-audio"
                        
                        // 3.2. ОПИСАНИЕ СВЯЗИ
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

                        // 3.3. ЗАПУСК ТЕСТОВ
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
    
    // 4. ОЧИСТКА (POST): Чистый блок, без sh команд.
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