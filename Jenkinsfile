// ==================================================================
// Jenkinsfile для CI/CD Android (v8 - Запуск gradlew через sh)
// Автор: kayanoterse (с помощью AI)
// Решение: Обход ошибки 'Permission Denied' при chmod на смонтированном
// Windows-томе путем прямого вызова 'sh ./gradlew'.
// ==================================================================
pipeline {
    agent any

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
                // ИСПРАВЛЕНИЕ: Убираем 'chmod' и вызываем './gradlew' через 'sh'
                bat '''
                    docker run --rm ^
                    -v "%WORKSPACE%:/app" ^
                    -w /app ^
                    kayanoterse/my-android-builder:latest ^
                    sh -c "sh ./gradlew -g .gradle clean testDebugUnitTest assembleDebug"
                '''
                
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
                // ИСПРАВЛЕНИЕ: Здесь также убираем 'chmod' и используем 'sh ./gradlew'
                bat '''
                    docker run --rm --privileged ^
                    -v "%WORKSPACE%:/app" ^
                    -w /app ^
                    kayanoterse/my-android-tester:latest ^
                    sh -c "emulator -avd ${params.AVD_NAME} -no-window -no-snapshot -no-audio -gpu swiftshader_indirect & adb wait-for-device && sleep 45 && adb shell input keyevent 82 && sh ./gradlew -g .gradle :app:connectedDebugAndroidTest"
                '''

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