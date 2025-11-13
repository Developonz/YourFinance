// ==================================================================
// Jenkinsfile для CI/CD Android (v16 - Фикс переменных и кэш)
// Автор: kayanoterse (с помощью AI)
// Решение: 1. Используем двойные кавычки для bat-блока для правильной
// подстановки Groovy-переменных. 2. Добавляем Docker Volume для кэша.
// ==================================================================
pipeline {
    agent any

    parameters {
        string(name: 'AVD_NAME', defaultValue: 'Medium_Phone_API_34', description: 'AVD emulator name created in Docker image')
    }

    stages {
        stage('Build & Unit Tests') {
            steps {
                echo 'Running build and unit tests with persistent cache...'
                // Добавляем именованный том для кэша Gradle
                bat '''
                    docker run --rm ^
                    -v "%WORKSPACE%:/app" ^
                    -v gradle-cache:/root/.gradle ^
                    -w /app ^
                    my-android-builder:latest ^
                    sh -c "sed 's/\\r$//' ./gradlew > ./gradlew.sh && export GRADLE_USER_HOME=/root/.gradle && sh ./gradlew.sh --no-daemon --project-cache-dir /tmp/.gradle-project-cache clean testDebugUnitTest assembleDebug"
                '''
                
                echo 'Stashing artifacts...'
                stash includes: 'app/build/outputs/apk/**/*.apk', name: 'apks'
                stash includes: '**/build/test-results/testDebugUnitTest/**/*.xml', name: 'unit-test-results'
            }
        }

        stage('Run Integration Tests') {
            steps {
                unstash 'apks'
                echo 'Running instrumentation tests with persistent cache...'
                // ИСПРАВЛЕНИЕ: Используем """ для bat-блока и ${params.AVD_NAME}
                // Экранируем \$// в sed, чтобы Groovy его не трогал.
                bat """
                    docker run --rm --privileged ^
                    -v "%WORKSPACE%:/app" ^
                    -v gradle-cache:/root/.gradle ^
                    -w /app ^
                    my-android-tester:latest ^
                    sh -c "sed 's/\\r\$//' ./gradlew > ./gradlew.sh && export GRADLE_USER_HOME=/root/.gradle && emulator -avd ${params.AVD_NAME} -no-window -no-snapshot -no-audio -gpu swiftshader_indirect & adb wait-for-device && sleep 45 && adb shell input keyevent 82 && sh ./gradlew.sh --no-daemon --project-cache-dir /tmp/.gradle-project-cache :app:connectedDebugAndroidTest"
                """

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