@Library('shared-library') _
import com.davidleonm.PythonHelloWorldVariables

pipeline {
  agent { label 'slave' }

  stages {
    stage('Prepare Python ENV') {
      steps {
        script {
          SetBuildStatus('pending', "${PythonHelloWorldVariables.RepositoryName}")

          // Clean & Prepare new python environment
          sh 'rm -rf ENV'
          sh 'python3 -m venv ENV'

          sh 'ENV/bin/pip install --upgrade pip'
          sh "ENV/bin/pip install -r ${WORKSPACE}/PythonHelloWorld/requirements.txt"
        }
      }
    }

    stage('Execute unit tests') {
      steps {
        script {
          sh "ENV/bin/python -m unittest discover -s ${WORKSPACE}/PythonHelloWorld"
        }
      }
    }

    stage('Execute CI tests') {
      steps {
        script {
          executePythonHelloWorldCITest()
        }
      }
    }
  }
  post {
    success {
      script {
        SetBuildStatus('success', "${PythonHelloWorldVariables.RepositoryName}")
      }
    }

    failure {
      script {
        SetBuildStatus('failure', "${PythonHelloWorldVariables.RepositoryName}")
      }
    }
  }
}
