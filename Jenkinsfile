pipeline {
    // 1. ОБЩИЙ АГЕНТ: НАШ "СЕРВИС СБОРКИ" (Микросервис №1)
    // Вместо 'agent any' мы указываем Jenkins, что все стадии (по умолчанию)
    // должны выполняться внутри Docker-контейнера, созданного из нашего образа.
    agent {
        docker {
            // Указываем наш образ, который мы загрузили на Docker Hub
            image 'kayanoterse/my-android-builder:1.1' 
            alwaysPull true // Всегда скачивать свежую версию (важно для тестов)
            
            // 2. ПОДКЛЮЧЕНИЕ DOCKER VOLUMES (Тома)
            // Это необходимо для кэширования Gradle.
            // Jenkins создаст том 'jenkins-gradle-cache' и "пробросит" его
            // в папку /root/.gradle/caches внутри контейнера.
            // При следующих сборках все зависимости будут взяты из кэша,
            // что ускорит сборку в 5-10 раз.
            
            volumes {
                volume 'jenkins-gradle-cache', '/root/.gradle/caches', true
            }
        }
    }

    // Параметры больше не нужны, так как эмулятор тоже в Docker
    parameters {
        string(name: 'AVD_NAME', defaultValue: 'DEPRECATED', description: 'This parameter is no longer used.')
    }
    
    // Секция 'environment' удалена, т.к. ANDROID_HOME и Java теперь "зашиты" в Docker-образ

    stages {
        
        stage('Run Unit Tests') {
            steps {
                echo 'Running Unit Tests in Build Service (Container 1)...'
                // 3. ИЗМЕНЕНИЕ КОМАНД: BAT -> SH
                // Контейнер работает на Linux, поэтому используем 'sh'
                sh 'chmod +x ./gradlew' // Даем права на выполнение (важно!)
                sh './gradlew clean testDebugUnitTest'
            }
        }

        stage('Build Application') {
            steps {
                echo 'Building Debug Application in Build Service (Container 1)...'
                sh './gradlew assembleDebug'
            }
        }

        // 4. МИКРОСЕРВИСНАЯ СТАДИЯ (Два контейнера)
        stage('Run Integration Tests') {
            steps {
                script {
                    // Это имя будет использоваться как сетевой адрес (hostname) 
                    // для нашего второго контейнера.
                    def emulatorServiceName = "android-emulator-service"
                    
                    try {
                        // 4.1. ЗАПУСК "СЕРВИСА ТЕСТИРОВАНИЯ" (Микросервис №2)
                        // Мы запускаем второй контейнер (эмулятор) в фоновом режиме (-d).
                        // Он будет в той же Docker-сети, что и наш контейнер сборки.
                        echo "Starting Emulator Service (Container 2: budtmo/docker-android-x86-emulator)..."
                        sh "docker run --name ${emulatorServiceName} -d --privileged -p 5554:5554 budtmo/docker-android-x86-emulator:latest -e DEVICE=\"Samsung Galaxy S10\" -no-audio"
                        
                        // 4.2. ОПИСАНИЕ СВЯЗИ МЕЖДУ КОНТЕЙНЕРАМИ
                        // Мы ждем, пока эмулятор запустится, и подключаемся к нему
                        // по его СЕТЕВОМУ ИМЕНИ (emulatorServiceName).
                        echo "Waiting for Emulator Service to connect..."
                        def connected = false
                        timeout(time: 5, unit: 'MINUTES') {
                            while (!connected) {
                                try {
                                    // Контейнер №1 (сборщик) подключается к Контейнеру №2 (эмулятор)
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

                        // 4.3. ЗАПУСК ТЕСТОВ
                        // Теперь, когда связь установлена, запускаем тесты,
                        // указывая серийный номер (адрес) эмулятора.
                        echo 'Running Instrumentation Tests...'
                        sh "./gradlew :app:connectedDebugAndroidTest --stacktrace --info --rerun-tasks -Dconnected.device.serial=${emulatorServiceName}:5554"
                    
                    } finally {
                        // 5. ОЧИСТКА (POST-SCRIPT)
                        // Этот блок 'finally' гарантирует, что мы остановим и удалим
                        // контейнер эмулятора, даже если тесты провалятся.
                        echo "Stopping and removing Emulator Service (Container 2)..."
                        // '|| true' нужен, чтобы сборка не упала, если контейнер уже остановлен.
                        sh "docker stop ${emulatorServiceName} || true" 
                        sh "docker rm ${emulatorServiceName} || true" 
                    }
                }
            }
        }

        stage('Publish Results & Artifacts') {
            steps {
                echo 'Publishing Test Reports & Artifacts...'
                // Эти шаги остаются без изменений, т.к. Jenkins
                // все еще имеет доступ к файлам в 'workspace'.
                junit '**/build/test-results/testDebugUnitTest/**/*.xml'
                junit 'app/build/outputs/androidTest-results/connected/debug/*.xml'
                archiveArtifacts artifacts: 'app/build/outputs/apk/debug/app-debug.apk', fingerprint: true, onlyIfSuccessful: true
            }
        }
    }
    
    // 6. СЕКЦИЯ POST (ОЧИСТКА)
    // Секция 'post' упрощена. Нам больше не нужны 'taskkill'[cite: 73, 74].
    // Docker сам удалит контейнер сборки (agent).
    // Очистка эмулятора происходит в 'finally' на стадии тестов.
    post {
        always {
            echo 'Pipeline finished.'
            // Очистка рабочего пространства
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