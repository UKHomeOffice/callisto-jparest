version=
branchName=test

if [ $1 = "tag" ]
then
  mkdir /root/.ssh/ && echo "$SSH_KEY" > /root/.ssh/id_rsa && chmod 0600 /root/.ssh/id_rsa
  ssh-keyscan github.com >> /root/.ssh/known_hosts && chmod 600 /root/.ssh/known_hosts
  printf  "Host github.com\n   Hostname github.com\n   IdentityFile /root/.ssh/id_rsa\n" > /root/.ssh/config
  chmod 0600 /root/.ssh/config
  git remote add tag-origin  git@github.com:UKHomeOffice/callisto-jparest.git
  git tag $version
  git push tag-origin $version

elif [ $1 = "sonar" ]
then
      mvn sonar:sonar -Dsonar.host.url=$${SONAR_HOST} -Dsonar.login=$${SONAR_TOKEN} -Dsonar.organization=ukhomeoffice -Dsonar.projectKey=callisto-jparest -Dsonar.branch.name=$branchName -Dsonar.projectName=callisto-jparest
fi


