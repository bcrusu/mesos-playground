#!/bin/bash

MESOS_CONTEXT="./mesos"
MESOSDEV_CONTEXT="./mesosdev"

docker build -t bcrusu/mesos:latest -f $MESOS_CONTEXT/dockerfile $MESOS_CONTEXT
docker build -t bcrusu/mesosdev:latest -f $MESOSDEV_CONTEXT/dockerfile $MESOSDEV_CONTEXT
