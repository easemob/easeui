package com.hyphenate.easeui.player;

public interface EasyVideoCallback {

  void onStarted(EasyVideoPlayer player);

  void onPaused(EasyVideoPlayer player);

  void onPreparing(EasyVideoPlayer player);

  void onPrepared(EasyVideoPlayer player);

  void onBuffering(int percent);

  void onError(EasyVideoPlayer player, Exception e);

  void onCompletion(EasyVideoPlayer player);

  void onClickVideoFrame(EasyVideoPlayer player);
}
