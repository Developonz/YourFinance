pipeline {
  agent any
  stages {
    stage('Test echo') {
      steps {
        echo 'CI/CD webhook test: build started'
        echo 'Hello from Jenkins — simple test pipeline (no sh/bat)'
      }
    }
  }
  post { always { echo "Build finished: ${currentBuild.fullDisplayName} - result: ${currentBuild.currentResult}" } }
}
