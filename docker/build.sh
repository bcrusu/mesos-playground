#!/bin/bash

docker build -t bcrusu/mesos-master:latest -f /dockerfile-mesos-master /mesos-context

docker build -t bcrusu/mesos-slave:latest -f /dockerfile-mesos-slave /mesos-context