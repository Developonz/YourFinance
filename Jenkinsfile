pipeline {
    agent any

    stages {

        // --- CI STAGES ---

        stage('Static Analysis (Lint)') {
            steps {
                echo 'Запуск статического анализа кода...'
                // Запускается на любой ветке
                bat 'gradlew lintDebug'
            }
        }

        stage('Unit Tests') {
            steps {
                echo 'Запуск ЮНИТ-тестов...'
                // testDebugUnitTest запускает тесты в папке test
                bat 'gradlew testDebugUnitTest'
            }
        }

        stage('Build Debug APK') {
            // Условие: Запускать, если ветка 'dev'
            when {
                branch 'dev'
            }
            steps {
                echo 'Сборка отладочного APK...'
                bat 'gradlew assembleDebug'
            }
        }

        // --- CD STAGES ---

        stage('Integration Tests (Espresso)') {
            // Условие: Запускать, если ветка 'master' ИЛИ 'main'
            when {
                anyOf {
                    branch 'master'
                    branch 'main'
                }
            }
            steps {
                echo 'Запуск ИНТЕГРАЦИОННЫХ тестов (требует эмулятора)...'
                // connectedCheck запускает тесты в папке androidTest
                bat 'gradlew connectedCheck'
            }
        }

        stage('Build Release Artifact') {
            // Условие: Запускать, если ветка 'master' ИЛИ 'main'
            when {
                anyOf {
                    branch 'master'
                    branch 'main'
                }
            }
            steps {
                echo 'Сборка подписанного AAB/APK...'
                // bundleRelease для AAB (рекомендуется), assembleRelease для APK
                bat 'gradlew bundleRelease'
            }
        }

        stage('Archive & Deploy') {
            // Условие: Запускать, если ветка 'master' ИЛИ 'main'
            when {
                anyOf {
                    branch 'master'
                    branch 'main'
                }
            }
            steps {
                echo 'Архивирование артефакта и имитация доставки...'
                // 1. Сохранение артефакта в Jenkins (Artifacts)
                archiveArtifacts artifacts: '**/app-release.aab', onlyIfSuccessful: true

                // 2. Имитация доставки: копирование в "публичную" папку (нужно создать ее!)
                bat 'xcopy /y build\\outputs\\bundle\\release\\app-release.aab C:\\Jenkins_CD_Artifacts_Final\\'
            }
        }
    }
}