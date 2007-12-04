#!/bin/sh

# Computes the absolute path of eXo
UNIX_DIR=`dirname "$0"`
JONAS_ROOT=$UNIX_DIR/../..

# Sets some variables
LOG_OPTS="-Dorg.apache.commons.logging.Log=org.apache.commons.logging.impl.SimpleLog"
EXO_OPTS="-Dexo.webui.reloadable.template=true"
BONITA_HOME = $JONAS_ROOT/bonita
export JAVA_OPTS="-Xshare:auto -Xms128m -Xmx512m $LOG_OPTS $EXO_OPTS $BONITA_HOME"

# Launches the server
cd $UNIX_DIR
exec $UNIX_DIR/jonas "$@"
