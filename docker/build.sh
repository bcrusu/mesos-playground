#!/bin/bash

MESOS_CONTEXT="./mesos"

docker build -t bcrusu/mesos:latest -f $MESOS_CONTEXT/dockerfile $MESOS_CONTEXT
