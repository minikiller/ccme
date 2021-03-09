#!/bin/bash

git config --global push.default simple # we only want to push one branch — master
# specify the repo on the live server as a remote repo, and name it 'production'
# <user> here is the separate user you created for deploying
git remote add production ssh://root@39.104.54.42/root/project/ccfe
git push production origin # push our updates
#mvn clean package
