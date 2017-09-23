#!/bin/sh
set -x

if [ "$#" -ne 2 ] || [ -d "$1" ]; then
  echo "Usage: $0 PROJECT_TYPE NEW_DIRECTORY" >&2
  echo "PROJECT_TYPE can be usage|tutorial|app"
  echo "usage - how to use a certain feature"
  echo "tutorial - writing a toy program to demonstrate features"
  echo "app - writing a complete application"
  exit 1
fi

export ROOT_BUILD_DIR=$PWD
export PROJDIR=$1/$2

mkdir -p $PROJDIR/src/{main,test}/{java,resources,scala}
mkdir $PROJDIR/lib $PROJDIR/project $PROJDIR/target

# create an initial build.sbt file
echo "name := \"Akka - $1 - $2\"
version := \"1.0\"
scalaVersion := \"2.11.8\"

libraryDependencies ++= Seq(
  \"com.typesafe.akka\" %% \"akka-actor\" % \"2.5.4\",
  \"com.typesafe.akka\" %% \"akka-testkit\" % \"2.5.4\" % Test
)" > $PROJDIR/build.sbt
