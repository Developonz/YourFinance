// ==================================================================
// Jenkinsfile для CI/CD Android (v7 - Прямой вызов Docker CLI)
// Автор: kayanoterse (с помощью AI)
// Решение: Полный отказ от плагина docker-workflow для запуска контейнеров
// из-за бага с трансляцией путей на Windows.
// ==================================================================
pipeline {
    agent any // Все стадии выполняются на Windows-агенте

    parameters {
        string(name: 'AVD_NAME', defaultValue: 'Medium_Phone_API_34', description: 'AVD emulator name created in Docker image')
    }

    stages {
        // ==================================================================
        // СТАДИЯ 1: Сборка и Юнит-тесты
        // ==================================================================
        stage('Build & Unit Tests') {
            steps {
                echo 'Running build and unit tests via direct docker run command...'
                // Используем bat для выполнения Windows команд.
                // Символ ^ в конце строки позволяет переносить длинную команду.
                bat '''
                    docker run --rm ^
                    -v "%WORKSPACE%:/app" ^
                    -w /app ^
                    kayanoterse/my-android-builder:latest ^
                    sh -c "chmod +x ./gradlew && ./gradlew -g .gradle clean testDebugUnitTest assembleDebug"
                '''
                // После выполнения контейнера, артефакты (APK, отчеты) окажутся
                // в папке %WORKSPACE% на Windows-хосте, так как мы ее монтировали.
                echo 'Stashing artifacts...'
                stash includes: 'app/build/outputs/apk/**/*.apk', name: 'apks'
                stash includes: '**/build/test-results/testDebugUnitTest/**/*.xml', name: 'unit-test-results'
            }
        }

        // ==================================================================
        // СТАДИЯ 2: Инструментальные тесты
        // ==================================================================
        stage('Run Integration Tests') {
            steps {
                unstash 'apks'
                echo 'Running instrumentation tests via direct docker run command...'
                bat '''
                    docker run --rm --privileged ^
                    -v "%WORKSPACE%:/app" ^
                    -w /app ^
                    kayanoterse/my-android-tester:latest ^
                    sh -c "chmod +x ./gradlew && emulator -avd ${params.AVD_NAME} -no-window -no-snapshot -no-audio -gpu swiftshader_indirect & adb wait-for-device && sleep 45 && adb shell input keyevent 82 && ./gradlew -g .gradle :app:connectedDebugAndroidTest"
                '''
                // ВАЖНО: Сложный цикл ожидания загрузки эмулятора очень трудно реализовать в одной строке.
                // Здесь используется упрощенный подход: ждем появления устройства в adb, потом ждем
                // фиксированное время (45 секунд), чтобы ОС успела загрузиться. Это менее надежно,
                // но для отладки и лабораторной работы должно хватить.

                echo 'Stashing instrumentation test results...'
                stash name: 'instrumentation-test-results', includes: 'app/build/outputs/androidTest-results/connected/**/*.xml'
            }
        }

        // ==================================================================
        // СТАДИЯ 3: Публикация результатов
        // ==================================================================
        stage('Publish Results & Artifacts') {
            steps {
                echo 'Publishing Test Reports & Artifacts...'
                
                unstash 'unit-test-results'
                junit '**/build/test-results/testDebugUnitTest/**/*.xml'
                
                unstash 'instrumentation-test-results'
                junit 'app/build/outputs/androidTest-results/connected/**/*.xml'
                
                unstash 'apks'
                archiveArtifacts artifacts: 'app/build/outputs/apk/debug/app-debug.apk', fingerprint: true
            }
        }
    }
    
    post {
        always {
            // Очистка не требуется, т.к. контейнеры запускаются с флагом --rm
            // и автоматически удаляются после завершения работы.
            echo 'Pipeline finished.'
        }
        success {
            echo 'Build and all tests completed successfully!'
        }
        failure {
            echo 'Pipeline failed. Check the logs for details.'
        }
    }
}