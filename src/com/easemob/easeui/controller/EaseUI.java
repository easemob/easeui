package com.easemob.easeui.controller;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.util.Log;

import com.easemob.EMEventListener;
import com.easemob.chat.EMChat;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMChatOptions;
import com.easemob.chat.EMMessage;
import com.easemob.easeui.EaseConstant;
import com.easemob.easeui.domain.EaseEmojicon;
import com.easemob.easeui.domain.EaseUser;
import com.easemob.easeui.model.EaseNotifier;

public final class EaseUI {
	private static final String TAG = EaseUI.class.getSimpleName();

	/**
	 * the global EaseUI instance
	 */
	private static EaseUI instance = null;

	/**
	 * EMEventListener
	 */
	private EMEventListener eventListener = null;

	/**
	 * 用户属性提供者
	 */
	private EaseUserProfileProvider userProvider;

	private EaseSettingsProvider settingsProvider;

	/**
	 * application context
	 */
	private Context appContext = null;

	/**
	 * init flag: test if the sdk has been inited before, we don't need to init
	 * again
	 */
	private boolean sdkInited = false;

	/**
	 * the notifier
	 */
	private EaseNotifier notifier = null;

	/**
	 * 用来记录注册了eventlistener的foreground Activity
	 */
	private List<Activity> activityList = new ArrayList<Activity>();

	public void pushActivity(Activity activity) {
		if (!activityList.contains(activity)) {
			activityList.add(0, activity);
		}
	}

	public void popActivity(Activity activity) {
		activityList.remove(activity);
	}

	private EaseUI() {
	}

	/**
	 * 获取EaseUI单实例对象
	 * 
	 * @return
	 */
	public synchronized static EaseUI getInstance() {
		if (instance == null) {
			instance = new EaseUI();
		}
		return instance;
	}

	/**
	 * this function will initialize the HuanXin SDK
	 * 
	 * @return boolean true if caller can continue to call HuanXin related APIs
	 *         after calling onInit, otherwise false.
	 * 
	 *         初始化环信sdk及easeui库
	 *         返回true如果正确初始化，否则false，如果返回为false，请在后续的调用中不要调用任何和环信相关的代码
	 * @param context
	 * @return
	 */
	public synchronized boolean init(Context context) {
		if (sdkInited) {
			return true;
		}
		appContext = context;

		int pid = android.os.Process.myPid();
		String processAppName = getAppName(pid);

		Log.d(TAG, "process app name : " + processAppName);

		// 如果app启用了远程的service，此application:onCreate会被调用2次
		// 为了防止环信SDK被初始化2次，加此判断会保证SDK被初始化1次
		// 默认的app会在以包名为默认的process name下运行，如果查到的process name不是app的process
		// name就立即返回
		if (processAppName == null || !processAppName.equalsIgnoreCase(appContext.getPackageName())) {
			Log.e(TAG, "enter the service process!");

			// 则此application::onCreate 是被service 调用的，直接返回
			return false;
		}
		// 初始化环信SDK,一定要先调用init()
		EMChat.getInstance().init(context);

		initChatOptions();
		if (settingsProvider == null) {
			settingsProvider = new DefaultSettingsProvider();
		}

		sdkInited = true;
		return true;
	}

	protected void initChatOptions() {
		Log.d(TAG, "init HuanXin Options");

		// 获取到EMChatOptions对象
		EMChatOptions options = EMChatManager.getInstance().getChatOptions();
		// 默认添加好友时，是不需要验证的，改成需要验证
		options.setAcceptInvitationAlways(false);
		// 默认环信是不维护好友关系列表的，如果app依赖环信的好友关系，把这个属性设置为true
		options.setUseRoster(false);
		// 设置是否需要已读回执
		options.setRequireAck(true);
		// 设置是否需要已送达回执
		options.setRequireDeliveryAck(false);
		// 设置从db初始化加载时, 每个conversation需要加载msg的个数
		options.setNumberOfMessagesLoaded(1);

		notifier = createNotifier();
		notifier.init(appContext);

		// notifier.setNotificationInfoProvider(getNotificationListener());
	}

	protected EaseNotifier createNotifier() {
		return new EaseNotifier();
	}

	public EaseNotifier getNotifier() {
		return notifier;
	}

	public boolean hasForegroundActivies() {
		return activityList.size() != 0;
	}

	/**
	 * 设置用户属性提供者
	 * 
	 * @param provider
	 */
	public void setUserProfileProvider(EaseUserProfileProvider userProvider) {
		this.userProvider = userProvider;
	}

	/**
	 * 获取用户属性提供者
	 * 
	 * @return
	 */
	public EaseUserProfileProvider getUserProfileProvider() {
		return userProvider;
	}

	public void setSettingsProvider(EaseSettingsProvider settingsProvider) {
		this.settingsProvider = settingsProvider;
	}

	public EaseSettingsProvider getSettingsProvider() {
		return settingsProvider;
	}

	/**
	 * check the application process name if process name is not qualified, then
	 * we think it is a service process and we will not init SDK
	 * 
	 * @param pID
	 * @return
	 */
	private String getAppName(int pID) {
		String processName = null;
		ActivityManager am = (ActivityManager) appContext.getSystemService(Context.ACTIVITY_SERVICE);
		List l = am.getRunningAppProcesses();
		Iterator i = l.iterator();
		PackageManager pm = appContext.getPackageManager();
		while (i.hasNext()) {
			ActivityManager.RunningAppProcessInfo info = (ActivityManager.RunningAppProcessInfo) (i.next());
			try {
				if (info.pid == pID) {
					CharSequence c = pm
							.getApplicationLabel(pm.getApplicationInfo(info.processName, PackageManager.GET_META_DATA));
					// Log.d("Process", "Id: "+ info.pid +" ProcessName: "+
					// info.processName +" Label: "+c.toString());
					// processName = c.toString();
					processName = info.processName;
					return processName;
				}
			} catch (Exception e) {
				// Log.d("Process", "Error>> :"+ e.toString());
			}
		}
		return processName;
	}

	/**
	 * 用户属性提供者
	 * 
	 * @author wei
	 *
	 */
	public interface EaseUserProfileProvider {
		/**
		 * 返回此username对应的user
		 * 
		 * @param username
		 *            环信id
		 * @return
		 */
		EaseUser getUser(String username);
	}

	/**
	 * 表情信息提供者
	 *
	 */
	public interface EaseEmojiconInfoProvider {
		/**
		 * 根据唯一识别号返回此表情内容
		 * 
		 * @param emojiconIdentityCode
		 * @return
		 */
		EaseEmojicon getEmojiconInfo(String emojiconIdentityCode);

		/**
		 * 获取文字表情的映射Map,map的key为表情的emoji文本内容，value为对应的图片资源id或者本地路径(不能为网络地址)
		 * 
		 * @return
		 */
		Map<String, Object> getTextEmojiconMapping();
	}

	private EaseEmojiconInfoProvider emojiconInfoProvider;

	/**
	 * 获取表情提供者
	 * 
	 * @return
	 */
	public EaseEmojiconInfoProvider getEmojiconInfoProvider() {
		return emojiconInfoProvider;
	}

	/**
	 * 设置表情信息提供者
	 * 
	 * @param emojiconInfoProvider
	 */
	public void setEmojiconInfoProvider(EaseEmojiconInfoProvider emojiconInfoProvider) {
		this.emojiconInfoProvider = emojiconInfoProvider;
	}

	/**
	 * 新消息提示设置的提供者
	 *
	 */
	public interface EaseSettingsProvider {
		boolean isMsgNotifyAllowed(EMMessage message);

		boolean isMsgSoundAllowed(EMMessage message);

		boolean isMsgVibrateAllowed(EMMessage message);

		boolean isSpeakerOpened();
	}

	/**
	 * default settings provider
	 *
	 */
	protected class DefaultSettingsProvider implements EaseSettingsProvider {

		@Override
		public boolean isMsgNotifyAllowed(EMMessage message) {
			// TODO Auto-generated method stub
			return true;
		}

		@Override
		public boolean isMsgSoundAllowed(EMMessage message) {
			return true;
		}

		@Override
		public boolean isMsgVibrateAllowed(EMMessage message) {
			return true;
		}

		@Override
		public boolean isSpeakerOpened() {
			return true;
		}

	}
}
