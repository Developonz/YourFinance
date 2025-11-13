pipeline {
    agent none

    parameters {
        string(name: 'AVD_NAME', defaultValue: 'Medium_Phone_API_34', description: 'AVD name created inside the tester Docker image')
    }

    stages {
        stage('Build & Unit Test') {
            agent {
                docker {
                    image 'kayanoterse/my-android-builder:latest'
                    args '-w /app -v ${WORKSPACE}/.gradle:/root/.gradle'
                }
            }
            steps {
                script {
                    echo "--- Running in Builder Container ---"
                    sh 'chmod +x ./gradlew'
                    echo "Running Unit Tests and assembling the APKs..."
                    sh './gradlew clean testDebugUnitTest assembleDebug assembleAndroidTest'
                    echo "Stashing artifacts for later stages..."
                    stash name: 'app-apk', includes: 'app/build/outputs/apk/debug/app-debug.apk'
                    stash name: 'test-apk', includes: 'app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk'
                    stash name: 'unit-test-results', includes: 'app/build/test-results/testDebugUnitTest/**/*.xml'
                }
            }
        }

        stage('Run Integration Tests') {
            agent {
                docker {
                    image 'kayanoterse/my-android-tester:latest'
                    args '-w /app -v ${WORKSPACE}/.gradle:/root/.gradle'
                }
            }
            steps {
                script {
                    echo "--- Running in Tester Container on Windows/WSL2 ---"
                    sh 'chmod +x ./gradlew'
                    unstash 'app-apk'
                    unstash 'test-apk'
                    echo "Starting emulator: ${params.AVD_NAME} with SwiftShader GPU"
                    sh "emulator -avd ${params.AVD_NAME} -no-audio -no-window -no-snapshot-load -no-boot-anim -gpu swiftshader_indirect &"
                    
                    timeout(time: 5, unit: 'MINUTES') {
                        echo "Waiting for device to appear..."
                        sh "adb wait-for-device"
                        echo "Waiting for Android OS to fully boot..."
                        sh "while [[ \"\$(adb shell getprop sys.boot_completed | tr -d '\\r')\" != \"1\" ]] ; do sleep 1; done"
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

        stage('Publish Results & Artifacts') {
            agent any
            steps {
                script {
                    echo "--- Publishing Results ---"
                    unstash 'unit-test-results'
                    unstash 'integration-test-results'
                    unstash 'app-apk'
                    junit allowEmptyResults: true, testResults: 'app/build/test-results/testDebugUnitTest/**/*.xml'
                    junit allowEmptyResults: true, testResults: 'app/build/outputs/androidTest-results/connected/**/*.xml'
                    archiveArtifacts artifacts: 'app/build/outputs/apk/debug/app-debug.apk', fingerprint: true
                }
            }
        }
    }

    // ==================================================================
    // ИЗМЕНЕНИЯ ВНЕСЕНЫ ЗДЕСЬ
    // ==================================================================
    post {
        always {
            // Мы не можем использовать 'agent' здесь.
            // Вместо этого мы используем 'node', чтобы получить агента для выполнения шагов.
            steps {
                node {
                    echo 'Pipeline finished. Cleaning up workspace...'
                    cleanWs()
                }
            }
        }
        success {
            steps {
                node {
                    echo 'Build and tests completed successfully!'
                }
            }
        }
        failure {
            steps {
                node {
                    echo 'Build failed! Check logs.'
                }
            }
        }
    }
}