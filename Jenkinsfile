// pipeline {
//   agent any
//   stages {
//     stage('Test echo') {
//       steps {
//         echo 'CI/CD webhook test: build started'
//         echo 'Hello from Jenkins — simple test pipeline (no sh/bat)'
//       }
//     }
//   }
//   post { always { echo "Build finished: ${currentBuild.fullDisplayName} - result: ${currentBuild.currentResult}" } }
// }
pipeline {
    agent any // Указывает, что Job может выполняться на любом доступном агенте

    stages {
        
        // --- CI STAGES ---
        
        stage('Static Analysis (Lint)') {
            steps {
                echo 'Запуск статического анализа кода...'
                // Замените bat на sh, если ваш агент Jenkins - Linux.
                // В Windows используйте bat
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

        // Этот этап можно запускать только для ветки dev
        stage('Build Debug APK') {
            when {
                branch 'dev'
            }
            steps {
                echo 'Сборка отладочного APK...'
                bat 'gradlew assembleDebug'
            }
        }

        // --- CD STAGES ---
        
        // Эти этапы запускаются только для ветки master/main
        stage('Integration Tests (Espresso)') {
            when {
                branch 'master' || branch 'main'
            }
            steps {
                echo 'Запуск ИНТЕГРАЦИОННЫХ тестов (требует эмулятора)...'
                // connectedCheck запускает тесты в папке androidTest
                bat 'gradlew connectedCheck' 
            }
        }

        stage('Build Release Artifact') {
            when {
                branch 'master' || branch 'main'
            }
            steps {
                echo 'Сборка подписанного AAB/APK...'
                // bundleRelease для AAB, assembleRelease для APK
                bat 'gradlew bundleRelease' 
            }
        }

        stage('Archive & Deploy') {
            when {
                branch 'master' || branch 'main'
            }
            steps {
                echo 'Архивирование артефакта и имитация доставки...'
                // Сохранение артефакта в Jenkins (Artifacts)
                archiveArtifacts artifacts: '**/app-release.aab', onlyIfSuccessful: true
                
                // Имитация доставки: копирование в "публичную" папку
                bat 'copy build\\outputs\\bundle\\release\\app-release.aab C:\\Jenkins_CD_Artifacts_Final\\'
            }
        }
    }
}