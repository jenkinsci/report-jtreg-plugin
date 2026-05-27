/*
 See the documentation for more options:
 https://github.com/jenkins-infra/pipeline-library/
*/

// Custom pipeline to run mvn clean install before buildPlugin
pipeline {
  agent any
  
  stages {
    stage('Pre-Build: Maven Clean Install') {
      steps {
        sh 'mvn clean install -DskipTests'
      }
    }
    
    stage('Build Plugin') {
      steps {
        script {
          buildPlugin(
            forkCount: '1C',
            useContainerAgent: true,
            configurations: [
              [platform: 'linux', jdk: 21],
              [platform: 'windows', jdk: 17],
            ]
          )
        }
      }
    }
  }
}

