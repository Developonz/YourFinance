// ==================================================================
// Jenkinsfile для CI/CD Android (v10 - Обход ошибки rename в sed)
// Автор: kayanoterse (с помощью AI)
// Решение: Вместо изменения gradlew "на месте" (sed -i), перенаправляем
// вывод sed в новый файл (gradlew.sh) и запускаем его.
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
                // ИСПРАВЛЕНИЕ: Перенаправляем вывод sed в новый файл gradlew.sh и запускаем его.
                bat '''
                    docker run --rm ^
                    -v "%WORKSPACE%:/app" ^
                    -w /app ^
                    kayanoterse/my-android-builder:latest ^
                    sh -c "sed 's/\\r$//' ./gradlew > ./gradlew.sh && sh ./gradlew.sh -g .gradle clean testDebugUnitTest assembleDebug"
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
                // ИСПРАВЛЕНИЕ: Делаем то же самое и здесь.
                bat '''
                    docker run --rm --privileged ^
                    -v "%WORKSPACE%:/app" ^
                    -w /app ^
                    kayanoterse/my-android-tester:latest ^
                    sh -c "sed 's/\\r$//' ./gradlew > ./gradlew.sh && sh ./gradlew.sh -g .gradle clean && emulator -avd ${params.AVD_NAME} -no-window -no-snapshot -no-audio -gpu swiftshader_indirect & adb wait-for-device && sleep 45 && adb shell input keyevent 82 && sh ./gradlew.sh -g .gradle :app:connectedDebugAndroidTest"
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