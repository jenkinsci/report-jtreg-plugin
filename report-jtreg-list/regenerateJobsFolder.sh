#!/bin/bash

###Warning! hardcoded values!
###Serves moreover as inspiration
###TODO, create fat jar if reused in future

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
readonly PROJECT_DIR=`readlink -f "$SCRIPT_DIR/.."`

medium="${1}"
#medium="/run/media/$USER/8313ec6e-7099-48fd-b765-315ef90ba0e6"
target_jobs_dir="$medium/mnt-mod/raid/jobs"

export CLASSPATH="\
$PROJECT_DIR/report-jtreg-lib/target/report-jtreg-lib.jar\
:$PROJECT_DIR/report-jtreg-list/target/report-jtreg-list.jar\
:$HOME/.m2/repository/org/apache/commons/commons-compress/1.15/commons-compress-1.15.jar\
:$HOME/.m2/repository/commons-io/commons-io/2.11.0/commons-io-2.11.0.jar\
:$HOME/.m2/repository/commons-io/commons-io/2.11.0/commons-io-2.11.0.jar\
:$HOME/.m2/repository/org/tukaani/xz/1.9/xz-1.9.jar\
:$HOME/.m2/repository/com/google/code/gson/gson/2.10.1/gson-2.10.1.jar/"

function runit() {
  type="${1}"
  regex="${2}"
  v="${3}"
  for x in $(ls "${target_jobs_dir}" | grep $v "${regex}" ) ; do
    let counter=$counter+1
    echo "$counter/$total started $x"
    pushd "$target_jobs_dir/$x";
      java -Djdk.xml.maxGeneralEntitySizeLimit=0 -Djdk.xml.totalEntitySizeLimit=0 \
           io.jenkins.plugins.report.jtreg.main.recreate.Recreate "${type}" \
              -add-files "build.xml" \
              -url "http://hydra.brq.redhat.com:8080/" \
              -nvr-db "$medium/db/nvr-db" \
              -job-db "$medium/db/job-db" ;
    popd;
    echo "$counter/$total finished $x"
  done
}

echo "in $medium, the"
echo "There may be hardcoded subdirs. Have you read teh script?"
echo "-url? -nvr-db? -job-db? "
total=$(ls "$target_jobs_dir" | wc -l)
echo "   $target_jobs_dir contains: $total direct items/jobs"
echo ok?
read

counter=0
runit jtreg "tck.*" -v
runit jck "tck.*"




