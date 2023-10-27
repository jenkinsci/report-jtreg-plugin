#!/bin/sh

#
# Why is this here?
# After refactoring the codebase of this plugin into multiple modules, the report-jtreg-lib remained as a dependency
# to the report-jtreg-plugin module (the Jenkins plugin itself). However, because of the Jenkins security hardening
# (https://www.jenkins.io/blog/2018/03/15/jep-200-lts/), the plugin can't load classes from external modules and it
# throws an class filter exception. After trying many different solutions to the problem, this seemed like the only
# doable workaround.
#
# What does this do?
# This script is run in the compile phase of the report-jtreg-plugin module (the report-jtreg-lib module is already
# compiled) and it copies the target folder of the lib module to the target folder of the plugin module. Since the
# report-jtreg-lib dependency has the "provided" scope, the classes do not clash and the plugin works as intended with
# the lib classes in the plugin jar/hpi.
#
#

set -x
set -e
set -o pipefail

## resolve folder of this script, following all symlinks,
## http://stackoverflow.com/questions/59895/can-a-bash-script-tell-what-directory-its-stored-in
SCRIPT_SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SCRIPT_SOURCE" ]; do # resolve $SOURCE until the file is no longer a symlink
  SCRIPT_DIR="$( cd -P "$( dirname "$SCRIPT_SOURCE" )" && pwd )"
  SCRIPT_SOURCE="$(readlink "$SCRIPT_SOURCE")"
  # if $SOURCE was a relative symlink, we need to resolve it relative to the path where the symlink file was located
  [[ $SCRIPT_SOURCE != /* ]] && SCRIPT_SOURCE="$SCRIPT_DIR/$SCRIPT_SOURCE"
done
readonly SCRIPT_DIR="$( cd -P "$( dirname "$SCRIPT_SOURCE" )" && pwd )"

# the absolute path is necessary, as PWD is different for mvn clean install, and mvn release:prepare/perform
if [ ! -e ${SCRIPT_DIR}/../report-jtreg-lib/target/ ] ; then
  # thsi can happen during mvn release:perform even if  you built ahead of time
  pushd ${SCRIPT_DIR}/../report-jtreg-lib
    mvn package -DskipTests
  popd
fi


cp -arv ${SCRIPT_DIR}/../report-jtreg-lib/target/* ${SCRIPT_DIR}/target
