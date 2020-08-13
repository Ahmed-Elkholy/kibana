#!/usr/bin/env bash

tc_set_env() {
  export "$1"="$2"
  echo "##teamcity[setParameter name='env.$1' value='$2']"
}

verify_no_git_changes() {
  RED='\033[0;31m'
  C_RESET='\033[0m' # Reset color

  "$@"

  GIT_CHANGES="$(git ls-files --modified)"
  if [ "$GIT_CHANGES" ]; then
    echo -e "\n${RED}ERROR: '$*' caused changes to the following files:${C_RESET}\n"
    echo -e "$GIT_CHANGES\n"
    exit 1
  fi
}

tc_start_block() {
  echo "##teamcity[blockOpened name='$1']"
}

tc_end_block() {
  echo "##teamcity[blockClosed name='$1']"
}
