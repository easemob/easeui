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
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.hyphenate.util.EMLog;
import com.hyphenate.util.PathUtil;

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
	 * 展示图片的逻辑如下：
	 * 1、图片的宽度不超过屏幕宽度的1/3，高度不超过屏幕宽度1/2，这样的话，图片的长宽比位3：2
	 * 2、如果图片的长宽比大于3：2，则选择高度方向与规定一致，宽度方向按比例缩放
	 * 3、如果图片的长宽比小于3：2，则选择宽度方向与规定一致，高度方向按比例缩放
	 * 4、如果图片的长和宽都小的话，就按照图片的大小展示就好
	 * @param bitmap
	 */
	public static ViewGroup.LayoutParams showImage(ImageView imageView, Bitmap bitmap, int maxWidth, int maxHeight) {
		float mRadio = maxWidth * 1.0f / maxHeight;
		imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
		//获取图片的长和宽
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		float radio  = width * 1.0f / height;
		//按原图展示的情况
		if((maxHeight == 0 && maxWidth == 0) /*|| (width <= maxWidth && height <= maxHeight)*/) {
			imageView.setImageBitmap(bitmap);
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
		imageView.setImageBitmap(bitmap);
		return params;
	}


}
