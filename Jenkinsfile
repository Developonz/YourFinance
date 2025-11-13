// Jenkinsfile для CI/CD Android-приложения на Windows-агенте с WSL2/Docker Desktop
pipeline {
    // Определяем, что среда выполнения будет настраиваться для каждого этапа индивидуально.
    agent none

    // Параметр для гибкой настройки имени эмулятора.
    parameters {
        string(name: 'AVD_NAME', defaultValue: 'Medium_Phone_API_34', description: 'AVD name created inside the tester Docker image')
    }

    stages {
        // ==================================================================
        // ЭТАП 1: Сборка и Юнит-тесты в легком 'builder' контейнере
        // ==================================================================
        stage('Build & Unit Test') {
            agent {
                docker {
                    image 'kayanoterse/my-android-builder:latest'
                    // Кэширование Gradle. ${WORKSPACE} - переменная Jenkins, указывающая на папку проекта.
                    // Создаем папку .gradle внутри нее и "пробрасываем" в контейнер.
                    // Это работает и на Windows, Docker Desktop корректно обработает пути.
                    args '-v ${WORKSPACE}/.gradle:/root/.gradle'
                }
            }
            steps {
                script {
                    echo "--- Running in Builder Container ---"
                    // Делаем скрипт Gradle исполняемым внутри Linux-контейнера.
                    sh 'chmod +x ./gradlew'
                    
                    echo "Running Unit Tests and assembling the APKs..."
                    // Выполняем все задачи сборки в одной команде.
                    sh './gradlew clean testDebugUnitTest assembleDebug assembleAndroidTest'

                    echo "Stashing artifacts for later stages..."
                    // Сохраняем артефакты для передачи между контейнерами.
                    stash name: 'app-apk', includes: 'app/build/outputs/apk/debug/app-debug.apk'
                    stash name: 'test-apk', includes: 'app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk'
                    stash name: 'unit-test-results', includes: 'app/build/test-results/testDebugUnitTest/**/*.xml'
                }
            }
        }

        // ==================================================================
        // ЭТАП 2: Инструментальные тесты в 'tester' контейнере
        // ==================================================================
        stage('Run Integration Tests') {
            agent {
                docker {
                    image 'kayanoterse/my-android-tester:latest'
                    // ВАЖНО: Аргументы для KVM (--privileged, -v /dev/kvm) удалены, 
                    // так как они несовместимы с Windows-хостом.
                    // Кэширование Gradle оставляем, оно по-прежнему полезно.
                    args '-v ${WORKSPACE}/.gradle:/root/.gradle'
                }
            }
            steps {
                script {
                    echo "--- Running in Tester Container on Windows/WSL2 ---"
                    sh 'chmod +x ./gradlew'
                    unstash 'app-apk'
                    unstash 'test-apk'

                    echo "Starting emulator: ${params.AVD_NAME} with SwiftShader GPU"
                    // КЛЮЧЕВОЕ ИЗМЕНЕНИЕ ДЛЯ WINDOWS:
                    // Добавлен флаг '-gpu swiftshader_indirect' для использования программного рендеринга.
                    sh "emulator -avd ${params.AVD_NAME} -no-audio -no-window -no-snapshot-load -no-boot-anim -gpu swiftshader_indirect &"
                    
                    // Ожидание полной загрузки эмулятора (до 5 минут).
                    timeout(time: 5, unit: 'MINUTES') {
                        echo "Waiting for device to appear..."
                        sh "adb wait-for-device"
                        
                        echo "Waiting for Android OS to fully boot..."
                        // Надежный способ проверить, что ОС загрузилась.
                        sh "while [[ \"\$(adb shell getprop sys.boot_completed | tr -d '\\r')\" != \"1\" ]] ; do sleep 1; done"
                        
                        // Разблокировка экрана.
                        sh "adb shell input keyevent 82"
                        echo "Emulator is ready."
                    }

                    echo 'Running Instrumentation Tests...'
                    sh "./gradlew :app:connectedDebugAndroidTest"
                    
                    echo "Stashing integration test results..."
                    stash name: 'integration-test-results', includes: 'app/build/outputs/androidTest-results/connected/**/*.xml'
                }
            }
        }

        // ==================================================================
        // ЭТАП 3: Публикация результатов
        // ==================================================================
        stage('Publish Results & Artifacts') {
            // Этот этап может выполняться на любом агенте, т.к. он просто работает с файлами.
            agent any
            steps {
                script {
                    echo "--- Publishing Results ---"
                    unstash 'unit-test-results'
                    unstash 'integration-test-results'
                    unstash 'app-apk'

                    // Публикация отчетов в Jenkins UI.
                    junit allowEmptyResults: true, testResults: 'app/build/test-results/testDebugUnitTest/**/*.xml'
                    junit allowEmptyResults: true, testResults: 'app/build/outputs/androidTest-results/connected/**/*.xml'
                    
                    // Архивирование финального APK.
                    archiveArtifacts artifacts: 'app/build/outputs/apk/debug/app-debug.apk', fingerprint: true
                }
            }
        }
    }

    // Блок POST для действий после завершения пайплайна.
    post {
        always {
            echo 'Pipeline finished. Cleaning up workspace...'
            // Стандартная функция очистки рабочего пространства Jenkins.
            // Убивать эмулятор не нужно, он "умирает" вместе с контейнером.
            cleanWs()
        }
        success {
            echo 'Build and tests completed successfully!'
        }
        failure {
            echo 'Build failed! Check logs.'
        }
    }
}