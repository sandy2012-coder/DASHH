#!/bin/sh
##############################################################################
##
##  Gradle start up script for UN*X
##
##############################################################################

# Attempt to locate java
if [ -z "$JAVA_HOME" ]; then
  JAVA_CMD=$(which java)
else
  JAVA_CMD="$JAVA_HOME/bin/java"
fi

if [ ! -x "$JAVA_CMD" ]; then
  echo "ERROR: JAVA_HOME is not set correctly or Java could not be found." >&2
  exit 1
fi

CLASSPATH="gradle/wrapper/gradle-wrapper.jar"

exec "$JAVA_CMD" -cp "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
