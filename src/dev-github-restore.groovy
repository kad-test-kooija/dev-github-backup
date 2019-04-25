#!/usr/bin/env groovy

def buildEnv = [
      backupHostName:    env.BACKUP_HOSTNAME,
      credentialsId:     env.CREDENTIALS_ID,
      dockerLabel:       env.DOCKER_LABEL,
      githubHostName:    env.GITHUB_HOSTNAME,
      githubRestoreHost: env.GITHUB_RESTOREHOST,
      snapshotId:        env.SNAPSHOT_ID,
      dockerImage:       env.DOCKER_IMAGE
    ]
    
node {
  stage('Perform Restore') {
    docker.withTool(buildEnv['dockerLabel']) {
      docker.withServer("tcp://${buildEnv['backupHostName']}:2376", "${buildEnv['credentialsId']}") {
        sh """ 
            docker run --rm \
            -e GHE_HOSTNAME=${buildEnv['githubHostName']} \
            -e GHE_RESTORE_HOST=${buildEnv['githubRestoreHost']} \
            -e GHE_DATA_DIR=/github_backup \
            -e GHE_NUM_SNAPSHOTS=10 \
            -e GHE_EXTRA_SSH_OPTS='-i /ghe-ssh/id_rsa -o StrictHostKeyChecking=no' \
            -v /var/appdata/github_backup:/github_backup \
            -v /var/appdata/github_backup/.ssh:/ghe-ssh \
            ${buildEnv['dockerImage']} ghe-restore -f -c -s ${buildEnv['snapshotId']}
           """
      }
    }
  }
}
