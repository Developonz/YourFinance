pipeline {
    agent any

    parameters {
        string(name: 'AVD_NAME', defaultValue: 'Medium_Phone_API_34', description: 'AVD emulator name created in Docker image')
    }

    stages {
        stage('Build & Unit Tests') {
            steps {
                echo 'Running build and unit tests with persistent cache...'
                bat '''
                    docker run --rm ^
                    -v "%WORKSPACE%:/app" ^
                    -v gradle-cache:/root/.gradle ^
                    -v gradle-build-cache:/tmp/.gradle-project-cache ^
                    -w /app ^
                    my-android-builder:latest ^
                    sh -c "sed 's/\\r$//' ./gradlew > ./gradlew.sh && export GRADLE_USER_HOME=/root/.gradle && sh ./gradlew.sh --build-cache --no-daemon --no-parallel --project-cache-dir /tmp/.gradle-project-cache testDebugUnitTest assembleDebug"
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
                bat """
                docker run --rm --privileged ^
                -v "%WORKSPACE%:/app" ^
                -v gradle-cache:/root/.gradle ^
                -v gradle-build-cache:/tmp/.gradle-project-cache ^
                -w /app ^
                my-android-tester:latest ^
                sh -c "sed 's/\\r\$//' ./gradlew > ./gradlew.sh && export GRADLE_USER_HOME=/root/.gradle && echo 'Starting emulator...' && \$ANDROID_HOME/emulator/emulator -avd ${params.AVD_NAME} -no-window -no-snapshot -no-audio -gpu swiftshader_indirect & echo 'Waiting for device...' && \$ANDROID_HOME/platform-tools/adb wait-for-device && echo 'Device connected. Waiting for boot_completed...' && count=0; while [ \\"\$(\$ANDROID_HOME/platform-tools/adb shell getprop sys.boot_completed ^| tr -d '\\r')\\" != \\"1\\" ]; do echo 'Still booting...'; sleep 5; count=\$((count+5)); if [ \$count -gt 300 ]; then echo 'Boot timeout'; exit 1; fi; done && echo 'Boot completed!' && echo 'Unlocking screen...' && \$ANDROID_HOME/platform-tools/adb shell input keyevent 82 && echo 'Running tests...' && sh ./gradlew.sh --build-cache --no-daemon --no-parallel --project-cache-dir /tmp/.gradle-project-cache :app:connectedDebugAndroidTest"
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
            bat 'copy app\\build\\outputs\\apk\\debug\\app-debug.apk C:\\k8s-shared-data\\app-debug.apk'
        }
        failure {
            echo 'Pipeline failed. Check the logs for details.'
        }
    }
}