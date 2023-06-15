#!/bin/sh
# -----------------------------------------------------------------------------
# Stop script for the OpenJMS server.
#
# $Id: stub.sh,v 1.1 2004/11/26 02:59:09 tanderson Exp $
# -----------------------------------------------------------------------------

# resolve links - $0 may be a softlink
PRG="$0"

while [ -h "$PRG" ] ; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '.*/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=`dirname "$PRG"`/"$link"
  fi
done
 
PRGDIR=`dirname "$PRG"`
EXECUTABLE=openjms.sh

# Check that target executable exists
if [ ! -x "$PRGDIR"/"$EXECUTABLE" ]; then
  echo "Cannot find $PRGDIR/$EXECUTABLE"
  echo "This is required to stop the OpenJMS server."
  exit 1
fi

exec "$PRGDIR"/"$EXECUTABLE" stop "$@"
