#!/bin/sh

DIRNAME=$(cd "$(dirname "$0")" && pwd)
CLASSPATH=$DIRNAME/gradle/wrapper/gradle-wrapper.jar

exec "$JAVA_HOME/bin/java" -Xmx64m -Xms64m -Dorg.gradle.appname=gradlew -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
