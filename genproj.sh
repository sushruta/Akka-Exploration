#!/bin/sh
set -x

if [ "$#" -ne 2 ] || [ -d "$1/$2" ]; then
  echo "Usage: $0 PROJECT_TYPE NEW_DIRECTORY" >&2
  echo "PROJECT_TYPE can be usage|tutorial|app"
  echo "usage - how to use a certain feature"
  echo "tutorial - writing a toy program to demonstrate features"
  echo "app - writing a complete application"
  exit 1
fi

export ROOT_BUILD_DIR=$PWD
export PROJDIR=$1/$2

mkdir -p $PROJDIR/src/{main,test}/{resources,scala}
mkdir $PROJDIR/project $PROJDIR/target

## CREATE an initial build.sbt file
echo "name := \"Akka - $1 - $2\"
version := \"1.0\"
scalaVersion := \"2.11.8\"

libraryDependencies ++= Seq(
  \"com.typesafe.akka\" %% \"akka-http\" % \"10.0.10\",
  \"com.typesafe.akka\" %% \"akka-actor\" % \"2.5.4\",
  \"com.typesafe.akka\" %% \"akka-stream\" % \"2.5.4\",
  \"com.typesafe.akka\" %% \"akka-testkit\" % \"2.5.4\" % Test
)" > $PROJDIR/build.sbt

echo "# general scala stuff
*.class
*.log

# any jars
*.jar

# sbt stuff
project/
target/" > $PROJDIR/.gitignore
