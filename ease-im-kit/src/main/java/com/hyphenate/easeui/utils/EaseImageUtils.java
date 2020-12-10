/**
 * Copyright (C) 2016 Hyphenate Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hyphenate.easeui.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.hyphenate.chat.EMImageMessageBody;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMMessageBody;
import com.hyphenate.chat.EMVideoMessageBody;
import com.hyphenate.easeui.R;
import com.hyphenate.util.EMLog;
import com.hyphenate.util.ImageUtils;
import com.hyphenate.util.PathUtil;
import com.hyphenate.util.UriUtils;

import java.io.IOException;

public class EaseImageUtils extends com.hyphenate.util.ImageUtils{
	
	public static String getImagePath(String remoteUrl)
	{
		String imageName= remoteUrl.substring(remoteUrl.lastIndexOf("/") + 1, remoteUrl.length());
		String path = PathUtil.getInstance().getImagePath()+"/"+ imageName;
        EMLog.d("msg", "image path:" + path);
        return path;
		
	}

	public static String getImagePathByFileName(String filename)
	{
		String path = PathUtil.getInstance().getImagePath()+"/"+ filename;
        EMLog.d("msg", "image path:" + path);
        return path;

	}

	public static String getThumbnailImagePath(String thumbRemoteUrl) {
		String thumbImageName= thumbRemoteUrl.substring(thumbRemoteUrl.lastIndexOf("/") + 1, thumbRemoteUrl.length());
		String path = PathUtil.getInstance().getImagePath()+"/"+ "th"+thumbImageName;
        EMLog.d("msg", "thum image path:" + path);
        return path;
    }

	public static String getThumbnailImagePathByName(String filename) {
		String path = PathUtil.getInstance().getImagePath()+"/"+ "th"+filename;
        EMLog.d("msg", "thum image dgdfg path:" + path);
        return path;
    }

	/**
	 * 获取图片最大的长和宽
	 * @param context
	 */
	public static int[] getImageMaxSize(Context context) {
		float[] screenInfo = EaseCommonUtils.getScreenInfo(context);
		int[] maxSize = new int[2];
		if(screenInfo != null) {
			maxSize[0] = (int) (screenInfo[0] / 3);
			maxSize[1] = (int) (screenInfo[0] / 2);
		}
		return maxSize;
	}

	/**
	 * 展示视频封面
	 * @param context
	 * @param imageView
	 * @param message
	 * @return
	 */
	public static ViewGroup.LayoutParams showVideoThumb(Context context, ImageView imageView, EMMessage message) {
		EMMessageBody body = message.getBody();
		if(!(body instanceof EMVideoMessageBody)) {
			return imageView.getLayoutParams();
		}
		//获取图片的尺寸
		int width = ((EMVideoMessageBody) body).getThumbnailWidth();
		int height = ((EMVideoMessageBody) body).getThumbnailHeight();
		//获取视频封面本地资源路径
		Uri localThumbUri = ((EMVideoMessageBody) body).getLocalThumbUri();
		//获取视频封面服务器地址
		String thumbnailUrl = ((EMVideoMessageBody) body).getThumbnailUrl();
		return showImage(context, imageView, localThumbUri, thumbnailUrl, width, height);
	}

	/**
	 * 展示图片
	 * @param context
	 * @param imageView
	 * @param message
	 * @return
	 */
	public static ViewGroup.LayoutParams showImage(Context context, ImageView imageView, EMMessage message) {
		EMMessageBody body = message.getBody();
		if(!(body instanceof EMImageMessageBody)) {
		    return imageView.getLayoutParams();
		}
		//获取图片的长和宽
		int width = ((EMImageMessageBody) body).getWidth();
		int height = ((EMImageMessageBody) body).getHeight();
		//获取图片本地资源地址
		Uri imageUri = ((EMImageMessageBody) body).getLocalUri();
		if(!UriUtils.isFileExistByUri(context, imageUri)) {
			imageUri = ((EMImageMessageBody) body).thumbnailLocalUri();
		}
		//图片附件上传之前从消息体中获取不到图片的长和宽
		if(width == 0 || height == 0) {
			BitmapFactory.Options options = null;
			try {
				options = ImageUtils.getBitmapOptions(context, imageUri);
			} catch (IOException e) {
				e.printStackTrace();
			}
			if(options != null) {
			    width = options.outWidth;
			    height = options.outHeight;
			}
		}
		//获取图片服务器地址
		String thumbnailUrl = ((EMImageMessageBody) body).getThumbnailUrl();
		return showImage(context, imageView, imageUri, thumbnailUrl, width, height);
	}

	/**
	 * 展示图片的逻辑如下：
	 * 1、图片的宽度不超过屏幕宽度的1/3，高度不超过屏幕宽度1/2，这样的话，图片的长宽比位3：2
	 * 2、如果图片的长宽比大于3：2，则选择高度方向与规定一致，宽度方向按比例缩放
	 * 3、如果图片的长宽比小于3：2，则选择宽度方向与规定一致，高度方向按比例缩放
	 * 4、如果图片的长和宽都小的话，就按照图片的大小展示就好
	 * 5、如果没有本地资源，则展示服务器地址
	 * @param context 上下文
	 * @param imageView
	 * @param imageUri 图片本地资源
	 * @param imageUrl 服务器图片地址
	 * @param imgWidth 图片的宽度
	 * @param imgHeight 图片的长度
	 * @return
	 */
	public static ViewGroup.LayoutParams showImage(Context context, ImageView imageView, Uri imageUri, String imageUrl, int imgWidth, int imgHeight) {
		int[] maxSize = getImageMaxSize(context);
		int maxWidth = maxSize[0];
		int maxHeight = maxSize[1];

		float mRadio = maxWidth * 1.0f / maxHeight;
		imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
		float radio  = imgWidth * 1.0f / (imgHeight == 0 ? 1 : imgHeight);
		if(radio == 0) {
		    radio = 1;
		}

		//按原图展示的情况
		if((maxHeight == 0 && maxWidth == 0) /*|| (width <= maxWidth && height <= maxHeight)*/) {
			Glide.with(context).load(imageUri == null ? imageUrl : imageUri).diskCacheStrategy(DiskCacheStrategy.ALL).into(imageView);
			return imageView.getLayoutParams();
		}
		ViewGroup.LayoutParams params = imageView.getLayoutParams();
		//如果宽度方向大于最大值，且宽高比过大,将图片设置为centerCrop类型
		//宽度方向设置为最大值，高度的话设置为宽度的1/2
		if(mRadio / radio < 0.1f) {
			imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
			params.width = maxWidth;
			params.height = maxWidth / 2;
		}else if(mRadio / radio > 4) {
			//如果高度方向大于最大值，且宽高比过大,将图片设置为centerCrop类型
			//高度方向设置为最大值，宽度的话设置为宽度的1/2
			imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
			params.width = maxHeight / 2;
			params.height = maxHeight;
		}else {
			//对比图片的宽高比，找到最接近最大值的，其余方向，按比例缩放
			if(radio < mRadio) {
				//说明高度方向上更大
				params.height = maxHeight;
				params.width = (int) (maxHeight * radio);
			}else {
				//宽度方向上更大
				params.width = maxWidth;
				params.height = (int) (maxWidth / radio);
			}
		}
		Glide.with(context)
				.load(imageUri == null ? imageUrl : imageUri)
				.apply(new RequestOptions()
						.error(R.drawable.ease_default_image))
				.diskCacheStrategy(DiskCacheStrategy.ALL)
				.into(imageView);
		return params;
	}

}
