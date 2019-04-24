# dev-github-backup
Jenkins pipeline scripts for backup and restore of a Github Enterprise instance.
## dev-github-build-backup-image.groovy
- builds a docker image loaded with github-backup-utils
- the image is used by the jenkins jobs dev-github-backup and dev-github-restore
- there is no dependency on an on-premise docker registry while running dev-github-backup or dev-github-restore
## dev-github-backup.groovy
- makes an online backup of the github server and writes it to the backup host
- creates a known_hosts file on the backup host with an entry for the github server during the first run
- uses the docker image github-backup:latest built by the job dev-github-build-backup-image
## dev-github-check-backup-host.groovy
- shows the data structure below /var/appdata/ on the backup host
- useful for selecting the right snapshot id for a restore
- shows the used and available storage on the backup host
## dev-github-restore.groovy
- for a restore the github server has to be in maintenance mode
- for the name of the snapshot id, see dev-github-check-backup-host.groovy
- restores a backup of all sourcecode repositories to the Github Enterprise instance
