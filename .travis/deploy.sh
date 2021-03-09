#!/bin/bash

git config --global push.default simple # we only want to push one branch — master
# specify the repo on the live server as a remote repo, and name it 'production'
# <user> here is the separate user you created for deploying
git remote add production ssh://root@192.119.116.101/root/project/ccfe
git push production master # push our updates
#mvn clean package
