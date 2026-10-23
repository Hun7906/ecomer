#!/bin/sh
cd "$(dirname "$0")"
exec "${JAVA_HOME:+$JAVA_HOME/bin/}java" -classpath gradle/wrapper/gradle-wrapper.jar org.gradle.wrapper.GradleWrapperMain "$@"
