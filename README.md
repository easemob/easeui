# EaseIMKit

## 简介
EaseIMKit是一个基于环信sdk的UI库，封装了IM功能常用的控件、fragment等等。</br>
github上的代码不包含环信sdk，需要依赖环信IM 3.x版本的SDK使用，建议与环信的IM demo一起使用。

## 关于分支
当前分支EaseIMKit分支是EaseIMKit UI库的开源分支。开发者使用时，建议使用EaseIMKit的远程依赖。

## 关于Gradle接入
使用MavenCentral仓库，需要在项目根目录build.gradle中配置：
>```Java
>buildscript {
>    repositories {
>        ...
>        mavenCentral()
>    }
>}
>
>
>allprojects {
>    repositories {
>        ...
>        mavenCentral()
>    }
>}
>```
>
在module的build.gradle里加入以下依赖：
>```Java
>implementation 'io.hyphenate:ease-im-kit:xxx版本'
>implementation 'io.hyphenate:hyphenate-chat:xxx版本'
>```
其中，xxx版本请替换为最新的aar版本号。</br>
最新版本号请跳转到这里进行查看：http://docs-im.easemob.com/im/android/sdk/releasenote

## 相关文档
快速集成请参考文档：http://docs-im.easemob.com/im/android/other/easeimkit</br>
IM SDK集成请参考集成说明：http://docs-im.easemob.com/im/android/sdk/import
