#!/usr/bin/env groovy

// Globals
def buildEnv = [
      credentialsId:  env.CREDENTIALS_ID,
      dockerLabel:    env.DOCKER_LABEL,
      dockerImage:    env.DOCKER_IMAGE
]

pipeline {
  agent any

  options {
    buildDiscarder( logRotator( numToKeepStr: '10',
      artifactNumToKeepStr: '5'))
    timeout(time: 1, unit: 'HOURS')
  }

  stages {

    stage('Init github-backup image build') {
      steps {
        script {
          echo "Docker Build Host: ${env.DOCKER_BUILD_HOST}"
          echo "Docker Client Version: ${buildEnv['dockerLabel']}"

          checkout changelog: false,
                   poll: false,
                   scm: [
                     $class: 'GitSCM',
                     branches: [[name: '*/stable']],
                     doGenerateSubmoduleConfigurations: false,
                     extensions: [],
                     submoduleCfg: [],
                     userRemoteConfigs: [[url: 'https://github.com/github/backup-utils.git']]
                   ]
        }
      }
    }

    stage('Build github-backup image') {
      steps {
        script {
          docker.withTool(buildEnv['dockerLabel']) {
            docker.withServer("tcp://${env.DOCKER_BUILD_HOST}:2376", env.DOCKER_BUILD_HOST.tokenize('.')[0]) {
              buildImage = docker.build(buildEnv['dockerImage'], " --no-cache --build-arg http_proxy=${http_proxy} .")
              docker.withRegistry("", buildEnv['credentialsId']) {
                buildImage.push()
              }
            }
          }
        }
      }
    }
  }
}
