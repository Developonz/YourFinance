// ==================================================================
// Jenkinsfile для CI/CD Android (v9 - Исправлены концы строк в gradlew)
// Автор: kayanoterse (с помощью AI)
// Решение: Добавлена команда sed для удаления Windows-символов (\r)
// из скрипта gradlew перед его выполнением.
// ==================================================================
pipeline {
    agent any

    parameters {
        string(name: 'AVD_NAME', defaultValue: 'Medium_Phone_API_34', description: 'AVD emulator name created in Docker image')
    }

    stages {
        stage('Build & Unit Tests') {
            steps {
                echo 'Running build and unit tests via direct docker run command...'
                // ИСПРАВЛЕНИЕ: Добавляем sed для исправления концов строк
                // ВАЖНО: Внутри bat блока нужен двойной слэш: \\r
                bat '''
                    docker run --rm ^
                    -v "%WORKSPACE%:/app" ^
                    -w /app ^
                    kayanoterse/my-android-builder:latest ^
                    sh -c "sed -i 's/\\r$//' ./gradlew && sh ./gradlew -g .gradle clean testDebugUnitTest assembleDebug"
                '''
                
                echo 'Stashing artifacts...'
                stash includes: 'app/build/outputs/apk/**/*.apk', name: 'apks'
                stash includes: '**/build/test-results/testDebugUnitTest/**/*.xml', name: 'unit-test-results'
            }
        }

        stage('Run Integration Tests') {
            steps {
                unstash 'apks'
                echo 'Running instrumentation tests via direct docker run command...'
                // ИСПРАВЛЕНИЕ: Добавляем sed и здесь
                bat '''
                    docker run --rm --privileged ^
                    -v "%WORKSPACE%:/app" ^
                    -w /app ^
                    kayanoterse/my-android-tester:latest ^
                    sh -c "sed -i 's/\\r$//' ./gradlew && emulator -avd ${params.AVD_NAME} -no-window -no-snapshot -no-audio -gpu swiftshader_indirect & adb wait-for-device && sleep 45 && adb shell input keyevent 82 && sh ./gradlew -g .gradle :app:connectedDebugUnitTest"
                '''

                echo 'Stashing instrumentation test results...'
                stash name: 'instrumentation-test-results', includes: 'app/build/outputs/androidTest-results/connected/**/*.xml'
            }
        }

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