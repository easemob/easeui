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
	log_print "Start to build easeim-kit aar and upload ..."
	# sdk aar 打包
	./gradlew :easeui:clean :easeui:build -PisAarRelease=true :easeui:install
	# push to jcenter
	./gradlew -PisAarRelease=true :easeui:bintrayUpload
	log_print "easeim-kit aar build and upload finish."
}

bintray_upload


