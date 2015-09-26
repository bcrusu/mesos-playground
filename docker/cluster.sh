#!/bin/bash

SCRIPTDIR=$(cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
CLUSTERDIR=${SCRIPTDIR}/cluster
TARGETJAR=${SCRIPTDIR}/../target/mesosTest-1.0-SNAPSHOT.jar

run_maven() {
	mvn verify -f $SCRIPTDIR/../pom.xml -q -Dmaven.test.skip=true

	if [ $? -ne 0 ]; then
		exit 1
	fi
}

start_cluster() {
	run_maven

	if [ ! -f "$TARGETJAR" ]; then
		echo "Could not find target jar at: $TARGETJAR..."
		exit 1
	fi

	if [ ! -d "$CLUSTERDIR" ]; then
		echo "Creating cluster work dir at: $CLUSTERDIR..."
		mkdir "$CLUSTERDIR"
	fi

	cp -u "$TARGETJAR" "$CLUSTERDIR/mesosTest.jar"

	#TODO: automate adding IP 10.0.0.33 to eth0

	docker-compose up -d --no-recreate
}

stop_cluster() {
	docker-compose kill
	docker-compose rm -f
}

clean_cluster_dir() {			
	echo "Removing cluster work dir at: $CLUSTERDIR..."
	sudo rm -rf "$CLUSTERDIR"
}


if [ -z "$SCRIPTDIR" ]; then
	echo "Could not detect current script dir..."
	exit 1
fi

case "$1" in
	start)
		echo "Starting..."
		start_cluster
		echo "Done."
		;;
	stop)
		echo "Stopping..."
		stop_cluster
		echo "Done."
		;;
	clean)
		echo "Cleaning cluster dir..."
		clean_cluster_dir
		echo "Done."
		;;
	restart)
		echo "Restarting..."
		stop_cluster
	
		if [ "$2" = "-c" ]; then
			clean_cluster_dir
		fi

		start_cluster
		echo "Done."
		;;
	*)
		echo "Usage: cluster {start|stop|restart|clean}"
		exit 1
		;;
esac

