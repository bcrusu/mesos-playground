#!/bin/bash

echo "deb http://repos.mesosphere.io/ubuntu/ vivid main" > /etc/apt/sources.list.d/mesosphere.list
DISTRO=$(lsb_release -is | tr '[:upper:]' '[:lower:]')
CODENAME=$(lsb_release -cs)

apt-key adv --keyserver keyserver.ubuntu.com $CODENAME $DISTRO --recv E56151BF
apt-get -y update
apt-get -y install mesos