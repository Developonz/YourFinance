pipeline {
  agent any

  stages {
    stage('Test echo') {
      steps {
        echo 'CI/CD webhook test: build started'
        // Доп. командная строка для видимости в консоли
        sh 'echo "Hello from Jenkins — simple test pipeline" || true'
      }
    }
  }

  post {
    always {
      echo "Build finished: ${currentBuild.fullDisplayName} - result: ${currentBuild.currentResult}"
    }
  }
}
