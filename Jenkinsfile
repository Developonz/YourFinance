// Скриптовый Пайплайн, чтобы обойти баг Windows/Linux путей в Declarative
pipeline {
    agent none // Отключаем глобальный агент
    
    stages {
        // SCM Checkout должен произойти на хосте перед запуском контейнера
        stage('Declarative: Checkout SCM') {
            agent any 
            steps {
                checkout scm
                echo "Source code checked out on Jenkins host."
            }
        }
        
        // Группируем стадии, которые должны выполняться внутри одного контейнера
        stage('Build, Unit Tests, and Integration Tests') {
            steps {
                script {
                    def imageName = 'kayanoterse/my-android-builder:1.1'
                    def containerArgs = "-w /app -v jenkins-gradle-cache:/root/.gradle/caches"
                    
                    // Используем метод inside() для входа в контейнер с нужными аргументами
                    docker.image(imageName).inside(containerArgs) {
                        
                        // 1. Run Unit Tests (Внутри контейнера)
                        echo 'Running Unit Tests in Build Service...'
                        sh 'chmod +x ./gradlew' 
                        sh './gradlew clean testDebugUnitTest'
                        
                        // 2. Build Application (Внутри контейнера)
                        echo 'Building Debug Application in Build Service...'
                        sh './gradlew assembleDebug'

                        // 3. Run Integration Tests (Внутри контейнера)
                        def emulatorServiceName = "android-emulator-service"
                        
                        try {
                            echo "Starting Emulator Service (Container 2: budtmo/docker-android-x86-emulator)..."
                            // Запускаем второй контейнер, команду 'docker run' выполняет наш первый контейнер (kayanoterse/...)
                            sh "docker run --name ${emulatorServiceName} -d --privileged -p 5554:5554 budtmo/docker-android-x86-emulator:latest -e DEVICE=\"Samsung Galaxy S10\" -no-audio"
                            
                            echo "Waiting for Emulator Service to connect via ADB..."
                            def connected = false
                            timeout(time: 5, unit: 'MINUTES') {
                                while (!connected) {
                                    try {
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

                            echo 'Running Instrumentation Tests...'
                            sh "./gradlew :app:connectedDebugAndroidTest --stacktrace --info --rerun-tasks -Dconnected.device.serial=${emulatorServiceName}:5554"
                        
                        } finally {
                            // Очистка: остановка и удаление фонового контейнера
                            sh 'echo Stopping and removing Emulator Service...'
                            sh "docker stop ${emulatorServiceName} || true" 
                            sh "docker rm ${emulatorServiceName} || true" 
                        }
                    }
                }
            }
        }
        
        stage('Publish Results & Artifacts') {
            agent any // Возвращаемся на хост Jenkins для публикации артефактов
            steps {
                echo 'Publishing Test Reports & Artifacts...'
                junit '**/build/test-results/testDebugUnitTest/**/*.xml'
                junit 'app/build/outputs/androidTest-results/connected/debug/*.xml'
                archiveArtifacts artifacts: 'app/build/outputs/apk/debug/app-debug.apk', fingerprint: true, onlyIfSuccessful: true
            }
        }
    }
    
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