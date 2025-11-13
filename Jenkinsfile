// ==================================================================
// Jenkinsfile для CI/CD Android (v4 - Ручной checkout внутри контейнера)
// Автор: kayanoterse (с помощью AI)
// Архитектура: 2 контейнера (builder, tester) из Docker Hub
// Агент: Windows с Docker Desktop (WSL 2)
// ==================================================================
pipeline {
    agent none

    // === ИСПРАВЛЕНИЕ 1: Отключаем автоматический checkout ===
    options {
        // Это предотвратит попытку Jenkins сделать checkout на Windows-хосте в несуществующую папку.
        skipDefaultCheckout(true)
    }

    parameters {
        string(name: 'AVD_NAME', defaultValue: 'Medium_Phone_API_34', description: 'AVD emulator name created in Docker image')
    }

    stages {
        // ==================================================================
        // СТАДИЯ 1: Сборка и Юнит-тесты
        // ==================================================================
        stage('Build & Unit Tests') {
            agent {
                docker {
                    image 'kayanoterse/my-android-builder:latest'
                    customWorkspace '/app'
                }
            }
            steps {
                // === ИСПРАВЛЕНИЕ 2: Выполняем checkout вручную ВНУТРИ контейнера ===
                echo 'Checking out source code inside the container...'
                checkout scm

                echo 'Building APK and running Unit Tests...'
                sh 'chmod +x ./gradlew'
                sh './gradlew -g .gradle clean testDebugUnitTest assembleDebug'

                echo 'Stashing artifacts for later stages...'
                stash includes: 'app/build/outputs/apk/**/*.apk', name: 'apks'
                stash includes: '**/build/test-results/testDebugUnitTest/**/*.xml', name: 'unit-test-results'
            }
        }

        // ==================================================================
        // СТАДИЯ 2: Инструментальные тесты
        // ==================================================================
        stage('Run Integration Tests') {
            agent {
                docker {
                    image 'kayanoterse/my-android-tester:latest'
                    customWorkspace '/app'
                }
            }
            steps {
                // Контейнер новый, ему тоже нужен код.
                echo 'Checking out source code inside the container...'
                checkout scm

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
                            if (bootStatus == '1') { bootCompleted = true; echo "OS fully booted." } 
                            else { echo "Device not ready yet. Waiting 10 seconds..."; sleep(time: 10, unit: 'SECONDS') }
                        }
                        
                        sh "$ANDROID_HOME/platform-tools/adb -s ${emulatorSerial} shell input keyevent 82"
                        echo "Emulator is ready for tests."
                        sleep(time: 15, unit: 'SECONDS')
                    }

                    echo 'Running Instrumentation Tests...'
                    sh './gradlew -g .gradle :app:connectedDebugAndroidTest'

                    echo 'Stashing instrumentation test results...'
                    stash name: 'instrumentation-test-results', includes: 'app/build/outputs/androidTest-results/connected/**/*.xml'
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
                
                unstash 'instrumentation-test-results'
                junit 'app/build/outputs/androidTest-results/connected/**/*.xml'
                
                unstash 'apks'
                // Архивируем только главный APK
                archiveArtifacts artifacts: 'app/build/outputs/apk/debug/app-debug.apk', fingerprint: true
            }
        }
    }

    // POST-БЛОК остается без изменений
    post {
        always {
            echo 'Pipeline finished. Cleaning up emulator...'
            script {
                try {
                    docker.image('kayanoterse/my-android-tester:latest').inside {
                        sh "$ANDROID_HOME/platform-tools/adb emu kill"
                    }
                    echo 'Emulator stopped successfully.'
                } catch (e) {
                    echo "Could not stop the emulator, it might have already been stopped. Error: ${e.getMessage()}"
                }
            }
        }
        success { echo 'Build and all tests completed successfully!' }
        failure { echo 'Pipeline failed. Check the logs for details.' }
    }
}