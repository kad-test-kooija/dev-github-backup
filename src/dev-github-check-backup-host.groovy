#!/usr/bin/env groovy

def buildEnv = [
      backupHostName: env.BACKUP_HOSTNAME,
      credentialsId:  env.CREDENTIALS_ID,
      dockerLabel:    env.DOCKER_LABEL
]

node {
  stage('Check Backup Directories') {
    docker.withTool("${buildEnv['dockerLabel']}") {
      docker.withServer("tcp://${buildEnv['backupHostName']}:2376", "${buildEnv['credentialsId']}") {
        sh """
        docker run --rm  \
        -v /var/appdata/github_backup:/github_backup \
        busybox:latest sh -c 'ls -al /github_backup; \
        df -h /github_backup ' 
        """
      }
    }
  }
}