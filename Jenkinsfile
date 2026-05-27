/*
 See the documentation for more options:
 https://github.com/jenkins-infra/pipeline-library/
*/

// Custom pipeline to run mvn clean install before buildPlugin
pipeline {
  agent any
  
  tools {
    jdk 'jdk21'
    maven 'mvn'
  }
  
  stages {
    stage('Pre-Build: Maven Clean Install') {
      steps {
        checkout scm
        sh 'mvn clean install'
      }
    }
  }
}


