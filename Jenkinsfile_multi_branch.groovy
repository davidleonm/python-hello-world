pipeline {
  agent { label 'slave' }

  environment {
    GITHUB_TOKEN = credentials('github-token')
    FORBIDDEN_TEXT = 'Forbidden site!'
    HELLO_WORLD_TEXT = 'Hello World!'
    
    // The Jenkins server is the host of the environment so the URL for CI request is the same but different port
    CI_URL = "${JENKINS_URL}".replace('8080/', '9999')
  }

  stages {
    stage('Prepare Python ENV') {
      steps {
        script {
          SetBuildStatus('pending')

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

    stage('Build & Test deployment') {
      steps {
        script {
          def dockerImage = null
          def ciImageName = "python-hello-world-ci-image-${BUILD_ID}"

          try {
            dockerImage = docker.build("${ciImageName}", "--file ./Dockerfile ${WORKSPACE}")

            dockerImage.withRun('-p 9999:9999 --name=ci-container -d') {
              forbiddenResponse = sh(script: "curl ${CI_URL}", returnStdout: true)
              helloWorldResponse = sh(script: "curl ${CI_URL}/helloworld", returnStdout: true)

              if (forbiddenResponse != "${FORBIDDEN_TEXT}" || helloWorldResponse != "${HELLO_WORLD_TEXT}") {
                currentBuild.result = 'FAILURE'
                throw new Exception('Error in CI, got non-expected values')
              }
            }
          } finally {
            if (dockerImage != null) {
              sh "docker rmi -f ${ciImageName}"
            }
          }
        }
      }
    }
  }
  post {
    success {
      script {
        SetBuildStatus('success')
      }
    }

    failure {
      script {
        SetBuildStatus('failure')
      }
    }
  }
}

def SetBuildStatus(String status) {
  sh "curl -H 'Authorization: Bearer ${GITHUB_TOKEN}' \
      -H 'Content-Type: application/json' \
      -X POST 'https://api.github.com/repos/davidleonm/python-hello-world/statuses/${GIT_COMMIT}' \
      -d '{\"state\": \"${status}\",\"context\": \"continuous-integration/jenkins\", \"description\": \"Jenkins\", \"target_url\": \"${BUILD_URL}\"}'"
}