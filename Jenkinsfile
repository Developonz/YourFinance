pipeline {
    agent any

    environment {
        ANDROID_SDK_ROOT = 'C:\\Users\\zapru\\AppData\\Local\\Android\\Sdk'
        ANDROID_AVD_HOME = 'C:\\Users\\zapru\\.android\\avd'
        AVD_NAME = 'Medium_Phone_API_36.1'
        EMULATOR_SERIAL = 'emulator-5554'
        BOOT_TIMEOUT_MINUTES = '5'
        GRADLEW = '.\\gradlew.bat'
    }

    options {
        // ограничение времени для всего пайплайна при необходимости
        timeout(time: 60, unit: 'MINUTES')
        // если нужно — хранить побольше логов консоли
    }

    stages {
        stage('Prepare') {
            steps {
                checkout scm
                cleanWs() // чистим рабочую директорию от артефактов прошлых сборок
                echo "Workspace cleaned and repository checked out."
            }
        }

        stage('Run Unit Tests') {
            steps {
                echo 'Запуск unit тестов...'
                bat "${env.GRADLEW} clean testDebugUnitTest"
            }
            post {
                always {
                    junit '**/build/test-results/**/*.xml'
                }
            }
        }

        stage('Build APK') {
            steps {
                echo 'Сборка debug APK...'
                bat "${env.GRADLEW} assembleDebug"
            }
            post {
                success {
                    archiveArtifacts artifacts: 'app/build/outputs/apk/debug/app-debug.apk', fingerprint: true, onlyIfSuccessful: true
                }
            }
        }

        stage('Start Emulator and Run Integration Tests') {
            steps {
                script {
                    def adb = "${env.ANDROID_SDK_ROOT}\\platform-tools\\adb.exe"
                    def emulatorExe = "${env.ANDROID_SDK_ROOT}\\emulator\\emulator.exe"
                    echo "Start emulator ${env.AVD_NAME}..."
                    // start emulator headless; /MIN чтобы окно было минимизировано, start /b тоже работает
                    bat "start /MIN \"emulator\" \"${emulatorExe}\" -avd ${env.AVD_NAME} -no-audio -no-window"

                    // ensure adb server restarted
                    bat "\"${adb}\" kill-server"
                    bat "\"${adb}\" devices"

                    // wait for device and sys.boot_completed
                    timeout(time: Integer.parseInt(env.BOOT_TIMEOUT_MINUTES), unit: 'MINUTES') {
                        echo "Waiting for ${env.EMULATOR_SERIAL} to appear in adb..."
                        bat "\"${adb}\" -s ${env.EMULATOR_SERIAL} wait-for-device"

                        def booted = false
                        def maxChecks = 60 // 60 * 5s = 300s
                        def checks = 0
                        while (!booted && checks < maxChecks) {
                            def out = bat(script: "\"${adb}\" -s ${env.EMULATOR_SERIAL} shell getprop sys.boot_completed", returnStdout: true).trim()
                            echo "boot prop: '${out}'"
                            if (out == '1') {
                                booted = true
                                echo "Emulator booted."
                                break
                            }
                            checks++
                            sleep time: 5, unit: 'SECONDS'
                        }
                        if (!booted) {
                            error "Emulator didn't boot within timeout."
                        }

                        // small stabilization wait
                        sleep time: 10, unit: 'SECONDS'
                    }

                    // start capturing logcat to file (background)
                    def logcatFile = "emulator_${env.EMULATOR_SERIAL}_logcat.txt"
                    bat "\"${adb}\" -s ${env.EMULATOR_SERIAL} logcat -c" // clear
                    // start logcat in background using powershell Start-Process
                    bat "powershell -Command Start-Process -NoNewWindow -FilePath '${adb}' -ArgumentList '-s ${env.EMULATOR_SERIAL} logcat -v time > ${logcatFile}'"
                    
                    // run connected tests (pass device serial explicitly)
                    echo "Running instrumented tests on ${env.EMULATOR_SERIAL}..."
                    bat "${env.GRADLEW} :app:connectedDebugAndroidTest -Dconnected.device.serial=${env.EMULATOR_SERIAL} --stacktrace --info"
                }
            }
            post {
                always {
                    // pull adb bugreport and stop emulator gracefully, fallback to taskkill
                    script {
                        def adb = "${env.ANDROID_SDK_ROOT}\\platform-tools\\adb.exe"
                        def serial = env.EMULATOR_SERIAL
                        echo "Collecting logs/artifacts from device ${serial}..."
                        // copy additional test output (if any) is already performed by Android test runner
                        // grab bugreport (may be large) and logcat
                        bat returnStatus: true, script: "\"${adb}\" -s ${serial} logcat -d > emulator_${serial}_final_logcat.txt"
                        bat returnStatus: true, script: "\"${adb}\" -s ${serial} shell bugreport > emulator_${serial}_bugreport.txt"
                        // graceful shutdown
                        def killStatus = bat(returnStatus: true, script: "\"${adb}\" -s ${serial} emu kill")
                        if (killStatus != 0) {
                            echo "adb emu kill failed or device already gone; trying taskkill fallback..."
                            bat returnStatus: true, script: 'taskkill /F /IM qemu-system-x86_64.exe'
                            bat returnStatus: true, script: 'taskkill /F /IM emulator.exe'
                        } else {
                            echo "Sent emu kill to emulator."
                            // give it a moment to exit
                            sleep time: 5, unit: 'SECONDS'
                        }
                    }
                    // publish collected logs & test results
                    junit 'app/build/outputs/androidTest-results/connected/**/*.xml'
                    archiveArtifacts artifacts: "emulator_*_logcat*.txt, emulator_*_bugreport*.txt", allowEmptyArchive: true
                }
                failure {
                    echo "Integration stage failed — look at archived logcat/bugreport for details"
                }
            }
        }
    } // stages

    post {
        always {
            echo 'Pipeline finished — final cleanup.'
            // final safety: ensure emulator killed (best-effort)
            bat returnStatus: true, script: 'taskkill /F /IM qemu-system-x86_64.exe'
            bat returnStatus: true, script: 'taskkill /F /IM emulator.exe'
            cleanWs() // optional: keep only archived artifacts if desired
        }
        success {
            echo 'Success: build + tests OK.'
        }
        failure {
            echo 'Failure: check logs and artifacts.'
        }
    }
}
