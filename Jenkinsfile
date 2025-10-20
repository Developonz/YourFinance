// Декларативный пайплайн для мобильного приложения на базе Gradle в среде Windows
pipeline {
    // Используем любой доступный агент, но команды должны быть адаптированы для Windows
    agent any

    // Настройка переменных среды (если необходимо, например, для SDK)
    // РАСКОММЕНТИРУЙТЕ И ИЗМЕНИТЕ ПУТЬ, если Gradle не может найти Android SDK
    // environment {
    //     ANDROID_SDK_ROOT = 'C:\\Users\\jenkins\\AppData\\Local\\Android\\sdk'
    // }

    stages {
        stage('Checkout') {
            steps {
                // Предполагается, что код проекта уже загружен с GitHub конфигурацией Job
                echo 'Исходный код получен.'
                // На Windows для вызова пакетного файла Gradle используем 'bat'
            }
        }

        stage('Run Unit Tests') {
            steps {
                echo 'Запуск модульных (unit) тестов...'
                // Команда Gradle для запуска только модульных тестов.
                // Мы используем 'clean' для чистоты, 'testDebugUnitTest' для запуска тестов.
                // Убедитесь, что Java и gradlew.bat доступны в PATH агента.
                bat '.\\gradlew.bat clean testDebugUnitTest'
            }
        }

        stage('Build Application') {
            steps {
                echo 'Сборка отладочной (debug) версии приложения...'
                // Создаем отладочный APK или AAB.
                bat '.\\gradlew.bat assembleDebug'
            }
        }

        stage('Run Integration Tests (Требуется эмулятор/устройство!)') {
            // !!! ВАЖНО: Если вы не настроили эмулятор или устройство на агенте Jenkins, 
            // !!! закомментируйте этот этап, чтобы избежать сбоев.
            // when { expression { return false } } // <-- Раскомментируйте, чтобы пропустить этап
            steps {
                echo 'Запуск инструментальных (интеграционных/androidTest) тестов...'
                // Команда, которая запускает тесты на подключенных устройствах.
                bat '.\\gradlew.bat connectedDebugAndroidTest'
            }
        }

        stage('Publish Results & Artifacts') {
            steps {
                echo 'Публикация отчетов о тестах и архивирование артефактов...'

                // 1. Публикация отчетов модульных тестов
                // Jenkins будет искать JUnit XML файлы, созданные Gradle.
                junit '**/build/test-results/testDebugUnitTest/**/*.xml'

                // 2. Публикация отчетов инструментальных тестов (если запускались)
                // Результаты Android Instrumented Tests
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
            // Отправка уведомлений в случае сбоя
            echo 'Сборка не удалась! Проверьте логи.'
        }
        success {
            // Действия для успешного выполнения (например, деплой для CD)
            echo 'Сборка и тесты успешно завершены!'
        }
    }
}
