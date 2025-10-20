Анализ и улучшение вашего Jenkins CI/CD пайплайна для Android

Здравствуйте! Вы проделали большую работу по настройке CI/CD процесса для вашего мобильного приложения. Пайплайн успешно проходит все стадии, включая запуск эмулятора и выполнение интеграционных тестов, что является нетривиальной задачей.

Проанализировав предоставленные логи и ваш скрипт Jenkinsfile, можно сказать, что в целом он рабочий и выполняет поставленные задачи. Однако есть несколько моментов, которые можно улучшить для повышения стабильности и читаемости.

Анализ логов и выполнения

Ваш последний запуск завершился успешно (Finished: SUCCESS), что говорит о корректной настройке основных шагов.

Unit-тесты: Все модульные тесты успешно пройдены.

Сборка: Debug-версия APK-файла была успешно собрана.

Интеграционные тесты: Эмулятор был запущен, и все 6 интеграционных тестов прошли успешно.

Публикация: Результаты тестов и артефакты (APK) были заархивированы.

Очистка: Скрипт корректно попытался завершить процессы эмулятора.

Несмотря на общий успех, в логах есть некоторые моменты, на которые стоит обратить внимание:

Предупреждения сборки (warnings): В процессе сборки Gradle выдает множество предупреждений, связанных с использованием устаревших (deprecated) методов, а также рекомендации по оптимизации работы с базой данных Room. Хотя они не приводят к ошибке сборки, в будущем рекомендуется исправить их в коде приложения.

Ошибки в post блоке: Команда taskkill /F /IM qemu-system-x86_64.exe завершилась с ошибкой "Не удается найти процесс". Это происходит потому, что на момент выполнения этого шага процесс с таким именем уже был завершен (возможно, как дочерний процесс emulator.exe). Ваша вторая команда taskkill /F /IM emulator.exe успешно справляется с задачей.

Оценка и улучшение Jenkinsfile

Ваш скрипт хорошо структурирован, но его можно сделать еще более надежным и лаконичным.

1. Секция environment

Определение путей к SDK и AVD в блоке environment — это правильный подход. Однако, чтобы сделать скрипт более переносимым, можно использовать переменную окружения %USERPROFILE% вместо жестко заданного пути C:\\Users\\zapru.

2. Запуск эмулятора и ожидание

Ваш механизм ожидания загрузки эмулятора через проверку свойства sys.boot_completed является надежным. Единственное небольшое улучшение — можно добавить начальную паузу после запуска эмулятора, чтобы дать ему время инициализироваться перед началом проверок.

3. Секция post (очистка)

Как показал лог, одна из команд taskkill избыточна. Можно оставить только одну, которая надежно завершает процесс. Кроме того, использование returnStatus: true — это правильное решение, чтобы пайплайн не завершался с ошибкой, если процесс уже был остановлен.

4. Публикация отчетов о тестах (junit)

Вы корректно указали пути к отчетам, однако можно сделать их более универсальными и добавить шаг для публикации отчетов по покрытию кода (если вы его настроите).

Рекомендуемый исправленный Jenkinsfile

Ниже представлен доработанный вариант вашего скрипта с комментариями, объясняющими изменения.

code
Groovy
download
content_copy
expand_less
// Улучшенный декларативный пайплайн для Android на Windows
pipeline {
    agent any

    environment {
        // Используем переменную окружения для большей переносимости
        ANDROID_SDK_ROOT = "${System.env.USERPROFILE}\\AppData\\Local\\Android\\Sdk"
        ANDROID_AVD_HOME = "${System.env.USERPROFILE}\\.android\\avd"
    }

    stages {
        stage('Checkout') {
            steps {
                echo 'Исходный код получен.'
                // Шаг checkout SCM добавляется Jenkins автоматически, если Job настроен на получение из Git
            }
        }

        stage('Run Unit Tests') {
            steps {
                echo 'Запуск модульных (unit) тестов...'
                bat '.\\gradlew.bat clean testDebugUnitTest'
            }
        }

        stage('Build Application') {
            steps {
                echo 'Сборка отладочной (debug) версии приложения...'
                bat '.\\gradlew.bat assembleDebug'
            }
        }

        stage('Run Integration Tests') {
            steps {
                script {
                    def adbPath = "\"${env.ANDROID_SDK_ROOT}\\platform-tools\\adb.exe\""
                    def emulatorPath = "\"${env.ANDROID_SDK_ROOT}\\emulator\\emulator.exe\""
                    def avdName = 'Medium_Phone_API_36.1'
                    def emulatorSerial = 'emulator-5554'

                    echo "Настройка и запуск эмулятора: ${avdName}"
                    
                    // --- 1. Перезапуск ADB сервера ---
                    bat "${adbPath} kill-server"
                    bat "${adbPath} start-server"

                    // --- 2. Запуск эмулятора ---
                    echo "Запуск эмулятора ${avdName} в фоновом режиме..."
                    bat "start /b \"\" ${emulatorPath} -avd ${avdName} -no-audio -no-window -no-snapshot-load"

                    // --- 3. Ожидание полной загрузки эмулятора ---
                    timeout(time: 5, unit: 'MINUTES') {
                        echo "Ожидание появления устройства ${emulatorSerial} в ADB..."
                        bat "${adbPath} -s ${emulatorSerial} wait-for-device"
                        
                        echo "Ожидание полной загрузки Android OS..."
                        def bootCompleted = false
                        while (!bootCompleted) {
                            def bootStatus = bat(
                                script: "${adbPath} -s ${emulatorSerial} shell getprop sys.boot_completed", 
                                returnStdout: true
                            ).trim()

                            if (bootStatus == '1') {
                                bootCompleted = true
                                echo "ОС полностью загружена."
                            } else {
                                echo "Устройство еще не готово. Статус: ${bootStatus ?: 'не определен'}. Ожидание 5 секунд..."
                                sleep(time: 5, unit: 'SECONDS')
                            }
                        }

                        // --- 4. Разблокировка экрана и дополнительная пауза ---
                        bat "${adbPath} -s ${emulatorSerial} shell input keyevent 82"
                        echo "Эмулятор разблокирован. Дополнительная пауза 15 секунд для стабилизации."
                        sleep(time: 15, unit: 'SECONDS')
                    }

                    // --- 5. Запуск тестов ---
                    echo 'Запуск инструментальных (androidTest) тестов...'
                    bat ".\\gradlew.bat :app:connectedDebugAndroidTest --stacktrace -Dconnected.device.serial=${emulatorSerial}"
                }
            }
        }

        stage('Publish Results & Artifacts') {
            // Этот шаг выполняется всегда после тестов, даже если они упали, чтобы собрать отчеты
            post {
                always {
                    echo 'Публикация отчетов о тестах и архивирование артефактов...'
                    
                    // Публикация отчетов unit-тестов
                    junit '**/build/test-results/testDebugUnitTest/**/*.xml'
                    
                    // Публикация отчетов инструментальных тестов
                    junit 'app/build/outputs/androidTest-results/connected/debug/*.xml'
                    
                    // Архивация APK только в случае успеха
                    archiveArtifacts artifacts: 'app/build/outputs/apk/debug/app-debug.apk', fingerprint: true, onlyIfSuccessful: true
                }
            }
        }
    }
    
    post {
        always {
            echo 'Пайплайн завершен. Остановка эмулятора...'
            script {
                // Надежно останавливаем все процессы эмулятора.
                // returnStatus: true предотвращает ошибку, если процесс уже завершен.
                bat(
                    script: 'taskkill /F /IM emulator.exe',
                    returnStatus: true
                )
                bat(
                    script: '"%ANDROID_SDK_ROOT%\\platform-tools\\adb.exe" kill-server',
                    returnStatus: true
                )
                echo 'Очистка завершена.'
            }
        }
        success {
            echo 'Сборка и тесты успешно завершены!'
        }
        failure {
            echo 'Сборка не удалась! Проверьте логи.'
        }
    }
}
