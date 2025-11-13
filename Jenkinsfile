// ==================================================================
// Jenkinsfile для CI/CD Android (v11 - Использование локальных образов)
// Автор: kayanoterse (с помощью AI)
// Решение: Заменены имена образов с Docker Hub на локальные для
// ускорения сборки и экономии трафика.
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
                // ИСПРАВЛЕНИЕ: Используем локальный образ 'my-android-builder:latest'
                bat '''
                    docker run --rm ^
                    -v "%WORKSPACE%:/app" ^
                    -w /app ^
                    my-android-builder:latest ^
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
                // ИСПРАВЛЕНИЕ: Используем локальный образ 'my-android-tester:latest'
                bat '''
                    docker run --rm --privileged ^
                    -v "%WORKSPACE%:/app" ^
                    -w /app ^
                    my-android-tester:latest ^
                    sh -c "sed 's/\\r$//' ./gradlew > ./gradlew.sh && emulator -avd ${params.AVD_NAME} -no-window -no-snapshot -no-audio -gpu swiftshader_indirect & adb wait-for-device && sleep 45 && adb shell input keyevent 82 && sh ./gradlew.sh -g .gradle :app:connectedDebugAndroidTest"
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