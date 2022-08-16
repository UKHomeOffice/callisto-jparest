#!/bin/bash

version=$1

if [ $# -eq 0 ];
then
  echo "$0: Please enter a version number"
  exit 1
fi
[[ "version" =~ '^(0|[1-9]\d*).(0|[1-9]\d*).(0|[1-9]\d*)' ]];
  echo "Updating version in parentPom: $version"
  sed -i  "0,/<version>.*<\/version>/{s/<version>.*<\/version>/<version>$version<\/version>/}" pom.xml

  echo "Updating version in demoPom: $version"
  sed -i  "0,/<version>.*<\/version>/{s/<version>.*<\/version>/<version>$version<\/version>/}" demo/pom.xml

  echo "Updating version in jparestPom: $version"
  sed -i  "0,/<version>.*<\/version>/{s/<version>.*<\/version>/<version>$version<\/version>/}" jparest/pom.xml
  exit 1
else
echo "version"
fi

