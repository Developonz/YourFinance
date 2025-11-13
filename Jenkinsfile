// ==================================================================
// Jenkinsfile для CI/CD Android с использованием Docker-контейнеров
// Автор: kayanoterse (с помощью AI)
// Архитектура: 2 контейнера (builder, tester) из Docker Hub
// Агент: Windows с Docker Desktop (WSL 2)
// ==================================================================
pipeline {
    // Агент не указывается глобально, а для каждой стадии свой.
    agent none

    // Параметры, как и раньше, для гибкости.
    // Имя AVD должно совпадать с тем, что мы создали в tester.Dockerfile
    parameters {
        string(name: 'AVD_NAME', defaultValue: 'Medium_Phone_API_34', description: 'AVD emulator name created in Docker image')
    }

    stages {
        // ==================================================================
        // СТАДИЯ 1: Сборка и Юнит-тесты в легком контейнере
        // ==================================================================
        stage('Build & Unit Tests') {
            agent {
                docker {
                    // Используем наш образ-сборщик из Docker Hub
                    image 'kayanoterse/my-android-builder:latest'
                    // args больше не нужны, кэширование делаем иначе
                }
            }
            steps {
                echo 'Building APK and running Unit Tests inside builder container...'
                
                // Важно: даем права на исполнение Gradle Wrapper для Linux
                sh 'chmod +x ./gradlew'

                // Запускаем сборку и тесты с помощью 'sh'
                // Флаг '-g .gradle' говорит Gradle хранить кэш в папке .gradle
                // внутри рабочей директории. Это решает проблему с путями Windows/Linux.
                sh './gradlew -g .gradle clean testDebugUnitTest assembleDebug'

                // Сохраняем собранные APK для использования на следующей стадии
                echo 'Stashing APKs for the next stage...'
                stash includes: 'app/build/outputs/apk/**/*.apk', name: 'apks'
                
                // Сохраняем отчеты юнит-тестов
                stash includes: '**/build/test-results/testDebugUnitTest/**/*.xml', name: 'unit-test-results'
            }
        }

        // ==================================================================
        // СТАДИЯ 2: Инструментальные тесты в контейнере с эмулятором
        // ==================================================================
        stage('Run Integration Tests') {
            agent {
                docker {
                    // Используем наш образ-тестировщик из Docker Hub
                    image 'kayanoterse/my-android-tester:latest'
                    // KVM-аргумент убран, т.к. агент - Windows
                }
            }
            steps {
                // Восстанавливаем APK, собранные на предыдущей стадии
                echo 'Unstashing APKs...'
                unstash 'apks'

                script {
                    def emulatorSerial = 'emulator-5554'

                    echo "Starting emulator: ${params.AVD_NAME}"
                    
                    // Запускаем эмулятор в фоновом режиме. '&' в конце отправляет процесс в фон.
                    // Добавлены опции для работы в CI/CD среде без GUI.
                    sh "$ANDROID_HOME/emulator/emulator -avd ${params.AVD_NAME} -no-window -no-snapshot -no-audio -gpu swiftshader_indirect &"

                    // Ожидание полной загрузки эмулятора
                    timeout(time: 5, unit: 'MINUTES') {
                        echo "Waiting for device to appear..."
                        sh "$ANDROID_HOME/platform-tools/adb wait-for-device"
                        
                        echo "Waiting for Android OS to fully boot..."
                        def bootCompleted = false
                        while (!bootCompleted) {
                            // Используем sh с returnStdout для получения вывода команды
                            def bootStatus = sh(script: "$ANDROID_HOME/platform-tools/adb -s ${emulatorSerial} shell getprop sys.boot_completed", returnStdout: true).trim()
                            if (bootStatus == '1') {
                                bootCompleted = true
                                echo "OS fully booted."
                            } else {
                                echo "Device not ready yet. Waiting 10 seconds..."
                                sleep(time: 10, unit: 'SECONDS')
                            }
                        }
                        
                        // Разблокируем экран
                        sh "$ANDROID_HOME/platform-tools/adb -s ${emulatorSerial} shell input keyevent 82"
                        echo "Emulator is ready for tests."
                        sleep(time: 15, unit: 'SECONDS')
                    }

                    // Запускаем инструментальные тесты, используя кэш в локальной папке
                    echo 'Running Instrumentation Tests...'
                    sh './gradlew -g .gradle :app:connectedDebugAndroidTest'
                }
            }
        }

        // ==================================================================
        // СТАДИЯ 3: Публикация результатов
        // ==================================================================
        stage('Publish Results & Artifacts') {
            // Эта стадия может выполняться на любом агенте, контейнер не нужен
            agent any
            steps {
                echo 'Publishing Test Reports & Artifacts...'
                
                // Восстанавливаем отчеты юнит-тестов
                unstash 'unit-test-results'
                junit '**/build/test-results/testDebugUnitTest/**/*.xml'

                // Публикация отчетов инструментальных тестов
                junit 'app/build/outputs/androidTest-results/connected/**/*.xml'
                
                // Архивирование APK
                archiveArtifacts artifacts: 'app/build/outputs/apk/debug/app-debug.apk', fingerprint: true
            }
        }
    }

    // ==================================================================
    // POST-БЛОК: Действия после завершения
    // ==================================================================
    post {
        always {
            echo 'Pipeline finished. Cleaning up...'
            // Команда 'adb emu kill' - кроссплатформенный способ остановить эмулятор
            // Она сработает внутри контейнера, если он еще жив, или на хосте, если adb есть в PATH
            // Мы выполним ее внутри tester-контейнера для надежности
            stage('Stop Emulator') {
                agent {
                    docker { image 'kayanoterse/my-android-tester:latest' }
                }
                steps {
                    sh "$ANDROID_HOME/platform-tools/adb emu kill"
                }
            }
        }
        success {
            echo 'Build and all tests completed successfully!'
        }
        failure {
            echo 'Pipeline failed. Check the logs for details.'
        }
    }
}