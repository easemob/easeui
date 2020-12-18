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

function get_sdk_version() {
  cd ..
	cd emclient-android;
	# 从EMClient.java中获取版本号
	TEMPFILE=`find . -name EMClient.java`
	ANCHOR='public final static String VERSION'
	SDK_VERSION=`$SED -n -e '/'"$ANCHOR"'/p' $TEMPFILE | $SED -e 's/.*'"$ANCHOR"' = "\(.*\)";/\1/g'`

	if [[ $SDK_VERSION = '' ]];then
		echo "can not find sdk version in file: $TEMPFILE with anchor: $ANCHOR"
		exit 1
	elif [[ ${#SDK_VERSION} -ge 10 ]];then
		echo "$SDK_VERSION may too long. Make sure the sdk version split is correct."
		exit 1
	fi
	cd -;
	cd easeui;
}

function fetch_code_from_git() {
	log_print "Start fetch EaseIMKit code from git ..."

	# easeui
	cd easeui;git fetch upstream; git clean -fd; git checkout -f; git checkout easemob/EaseIMKit; cd -;

	log_print "Fet EaseIMKit code from git finished"

	echo -e "\n** SDK版本为：$SDK_VERSION **\n"
}

function prepare() {
    cp emclient-android/hyphenatechatsdk/res/layout/em_simple_notification.xml easeui/EaseIMKit/src/main/res/layout
}

function add_tag() {
	TAG=$1
        echo "add tag $TAG"
        (echo "easeui"; cd easeui; git tag $TAG; git push origin $TAG;)
}

function bintray_upload() {
	log_print "Start to build ease-im-kit aar and upload ..."
	# sdk aar 打包
	./gradlew :ease-im-kit:clean :ease-im-kit:build -PsdkVersion=$SDK_VERSION :ease-im-kit:install
	# push to jcenter
	./gradlew -PsdkVersion=$SDK_VERSION :ease-im-kit:bintrayUpload
	log_print "ease-im-kit aar build and upload finish."
}

get_sdk_version
if [[ $# == 0 ]];then
  fetch_code_from_git
  prepare
  bintray_upload
elif [[ $# -ge 1 ]];then
  if [[ $1 = "add_tag" ]];then
      if [[ $# -eq 1 ]];then
			  TAG=$SDK_VERSION
		  else
			  TAG=$2
		  fi
      echo "confirm to add tag \"$TAG\" to remote server[yes/no]?"
		  read confirm
		  if [[ $confirm = 'yes' ]];then
			  add_tag $TAG
			  exit 0
		  fi
  fi
fi



