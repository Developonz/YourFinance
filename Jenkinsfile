// Декларативный пайплайн для мобильного приложения на базе Gradle в среде Windows
pipeline {
    // Используем любой доступный агент, но команды должны быть адаптированы для Windows
    agent any

    // Устанавливаем переменную окружения ANDROID_SDK_ROOT, 
    // чтобы Gradle знал, где найти SDK.
    environment {
         // !!! ПУТЬ К SDK !!!
         ANDROID_SDK_ROOT = 'C:\\Users\\zapru\\AppData\\Local\\Android\\Sdk' 
    }
    
    stages {
        stage('Checkout') {
            steps {
                echo 'Исходный код получен.'
            }
        }

        stage('Run Unit Tests') {
            steps {
                echo 'Запуск модульных (unit) тестов...'
                // Убедитесь, что Java и gradlew.bat доступны в PATH агента.
                bat '.\\gradlew.bat clean testDebugUnitTest'
            }
        }

        stage('Build Application') {
            steps {
                echo 'Сборка отладочной (debug) версии приложения...'
                bat '.\\gradlew.bat assembleDebug'
            }
        }

        stage('Run Integration Tests (Требуется эмулятор/устройство!)') {
            // !!! ЭТАП АКТИВИРОВАН. Добавлены шаги для работы с USB-устройством !!!
            steps {
                echo 'Настройка ADB для подключения устройства...'
                
                // --- 1. Явно убиваем ADB сервер для чистого старта ---
                // Используем полный путь к adb.exe
                bat '"%ANDROID_SDK_ROOT%\\platform-tools\\adb.exe" kill-server'

                // --- 2. Запускаем ADB и проверяем подключенные устройства ---
                // Если здесь в логе нет вашего устройства, Jenkins его не видит (проблема с правами/кабелем)
                bat '"%ANDROID_SDK_ROOT%\\platform-tools\\adb.exe" devices'
                
                echo 'Запуск инструментальных (интеграционных/androidTest) тестов...'
                // Команда, которая запускает тесты на подключенных устройствах.
                bat '.\\gradlew.bat connectedDebugAndroidTest'
            }
        }

        stage('Publish Results & Artifacts') {
            steps {
                echo 'Публикация отчетов о тестах и архивирование артефактов...'

                // 1. Публикация отчетов модульных тестов
                junit '**/build/test-results/testDebugUnitTest/**/*.xml'

                // 2. Публикация отчетов инструментальных тестов (если запускались)
                junit '**/build/outputs/androidTest-results/connected/*.xml'

                // 3. Архивация собранного APK (для скачивания)
                archiveArtifacts artifacts: 'app/build/outputs/apk/debug/app-debug.apk', fingerprint: true, onlyIfSuccessful: true
            }
        }
    }
    
    // Действия после завершения пайплайна
    post {
        always {
            echo 'Пайплайн завершен.'
        }
        failure {
            echo 'Сборка не удалась! Проверьте логи.'
        }
        success {
            echo 'Сборка и тесты успешно завершены!'
        }
    }
}
