#!/bin/bash

unamestr=`uname`

if [[ "$unamestr" = MINGW* || "$unamestr" = "Linux" ]]; then
    	SED="sed"
elif [ "$unamestr" = "Darwin" ]; then
    	SED="gsed"
else
    	echo "only Linux, MingW or Mac OS can be supported" &
    	exit 1
fi

function log_print() {
	echo -e "\n------------------------------------------------------------------"
	echo "----------------- $1 -----------------"
	echo -e "------------------------------------------------------------------ \n\n"
}

function bintray_upload() {
	log_print "Start to build ease-im-kit aar and upload ..."
	# sdk aar 打包
	./gradlew :ease-im-kit:clean :ease-im-kit:build -PisAarRelease=true :ease-im-kit:install
	# push to jcenter
	./gradlew -PisAarRelease=true :ease-im-kit:bintrayUpload
	log_print "ease-im-kit aar build and upload finish."
}

bintray_upload


