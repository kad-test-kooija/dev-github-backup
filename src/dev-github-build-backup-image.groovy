#!/usr/bin/env groovy

// Globals
def buildEnv = [
			backupHostName: env.BACKUP_HOSTNAME,
			credentialsId:  env.CREDENTIALS_ID,
			dockerLabel:    env.DOCKER_LABEL,
			githubImage:    env.GITHUB_IMAGE
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
					echo "Docker Build Host: ${buildEnv['backupHostName']}"
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
						docker.withServer("tcp://${buildEnv['backupHostName']}:2376", buildEnv['credentialsId']) {
							buildImage = docker.build(buildEnv['githubImage'], "--build-arg http_proxy=${http_proxy} .")
						}
					}
				}
			}
		}
	}
}
