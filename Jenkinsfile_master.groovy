@Library('shared-library') _
import com.davidleonm.PythonHelloWorldVariables

pipeline {
  agent { label 'slave' }

  stages {
    stage('Prepare Python ENV') {
      steps {
        script {
          setBuildStatus('pending', "${PythonHelloWorldVariables.RepositoryName}")

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

    stage('SonarQube analysis') {
      environment {
        def scannerHome = tool 'Sonarqube'
      }

      steps {
        script {
          sh "ENV/bin/coverage run -m unittest discover -s ${WORKSPACE}/PythonHelloWorld"
          sh "ENV/bin/coverage xml -i"
        }

        withSonarQubeEnv('Sonarqube') {
          sh "${scannerHome}/bin/sonar-scanner"
        }

        timeout(time: 10, unit: 'MINUTES') {
          waitForQualityGate abortPipeline: true
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

    stage('Build & Deploy image') {
      steps {
        script {
          def dockerImage = null

          try {
            version = sh(script: 'cat VERSION', returnStdout: true)
            dockerImage = docker.build("${PythonHelloWorldVariables.DockerHubRegistryName}", "--file ./Dockerfile ${WORKSPACE}")

            docker.withRegistry('', 'docker-hub-login') {
              dockerImage.push("${version}")
              dockerImage.push('latest')
            }
          } finally {
            if (dockerImage != null) {
              sh """
                docker rmi -f ${PythonHelloWorldVariables.DockerHubRegistryName}:${version}
                docker rmi -f ${PythonHelloWorldVariables.DockerHubRegistryName}:latest
              """
            }
          }
        }
      }
    }
  }
  post {
    success {
      script {
        setBuildStatus('success', "${PythonHelloWorldVariables.RepositoryName}")
      }
    }

    failure {
      script {
        setBuildStatus('failure', "${PythonHelloWorldVariables.RepositoryName}")
      }
    }
  }