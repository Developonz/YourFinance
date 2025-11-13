// ==================================================================
// Jenkinsfile для CI/CD Android с использованием Docker-контейнеров (v2 - Исправлено)
// Автор: kayanoterse (с помощью AI)
// Архитектура: 2 контейнера (builder, tester) из Docker Hub
// Агент: Windows с Docker Desktop (WSL 2)
// ==================================================================
pipeline {
    agent none

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
                    image 'kayanoterse/my-android-builder:latest'
                }
            }
            steps {
                echo 'Building APK and running Unit Tests inside builder container...'
                sh 'chmod +x ./gradlew'
                sh './gradlew -g .gradle clean testDebugUnitTest assembleDebug'

                echo 'Stashing APKs for the next stage...'
                stash includes: 'app/build/outputs/apk/**/*.apk', name: 'apks'
                
                echo 'Stashing unit test results...'
                stash includes: '**/build/test-results/testDebugUnitTest/**/*.xml', name: 'unit-test-results'
            }
        }

        // ==================================================================
        // СТАДИЯ 2: Инструментальные тесты в контейнере с эмулятором
        // ==================================================================
        stage('Run Integration Tests') {
            agent {
                docker {
                    image 'kayanoterse/my-android-tester:latest'
                }
            }
            steps {
                echo 'Unstashing APKs...'
                unstash 'apks'

                script {
                    def emulatorSerial = 'emulator-5554'

                    echo "Starting emulator: ${params.AVD_NAME}"
                    sh "$ANDROID_HOME/emulator/emulator -avd ${params.AVD_NAME} -no-window -no-snapshot -no-audio -gpu swiftshader_indirect &"

                    timeout(time: 5, unit: 'MINUTES') {
                        echo "Waiting for device to appear..."
                        sh "$ANDROID_HOME/platform-tools/adb wait-for-device"
                        
                        echo "Waiting for Android OS to fully boot..."
                        def bootCompleted = false
                        while (!bootCompleted) {
                            def bootStatus = sh(script: "$ANDROID_HOME/platform-tools/adb -s ${emulatorSerial} shell getprop sys.boot_completed", returnStdout: true).trim()
                            if (bootStatus == '1') {
                                bootCompleted = true
                                echo "OS fully booted."
                            } else {
                                echo "Device not ready yet. Waiting 10 seconds..."
                                sleep(time: 10, unit: 'SECONDS')
                            }
                        }
                        
                        sh "$ANDROID_HOME/platform-tools/adb -s ${emulatorSerial} shell input keyevent 82"
                        echo "Emulator is ready for tests."
                        sleep(time: 15, unit: 'SECONDS')
                    }

                    echo 'Running Instrumentation Tests...'
                    sh './gradlew -g .gradle :app:connectedDebugAndroidTest'
                }
            }
        }

        // ==================================================================
        // СТАДИЯ 3: Публикация результатов
        // ==================================================================
        stage('Publish Results & Artifacts') {
            agent any
            steps {
                echo 'Publishing Test Reports & Artifacts...'
                
                unstash 'unit-test-results'
                junit '**/build/test-results/testDebugUnitTest/**/*.xml'
                
                junit 'app/build/outputs/androidTest-results/connected/**/*.xml'
                
                archiveArtifacts artifacts: 'app/build/outputs/apk/debug/app-debug.apk', fingerprint: true
            }
        }
    }

    // ==================================================================
    // POST-БЛОК: Действия после завершения (ИСПРАВЛЕННАЯ ВЕРСИЯ)
    // ==================================================================
    post {
        always {
            echo 'Pipeline finished. Cleaning up emulator...'
            // ИСПОЛЬЗУЕМ SCRIPT БЛОК И docker.image.inside ВМЕСТО STAGE
            script {
                try {
                    // Эта команда запустит контейнер, выполнит внутри него команду
                    // для остановки эмулятора и сразу же завершится.
                    docker.image('kayanoterse/my-android-tester:latest').inside {
                        sh "$ANDROID_HOME/platform-tools/adb emu kill"
                    }
                    echo 'Emulator stopped successfully.'
                } catch (e) {
                    // Игнорируем ошибку, если, например, эмулятор уже был остановлен
                    echo "Could not stop the emulator, it might have already been stopped. Error: ${e.getMessage()}"
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