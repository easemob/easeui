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
import android.net.Uri;
import android.text.Spannable;
import android.text.Spannable.Factory;
import android.text.style.ImageSpan;

import com.hyphenate.easeui.EaseIM;
import com.hyphenate.easeui.domain.EaseEmojicon;
import com.hyphenate.easeui.model.EaseDefaultEmojiconDatas;
import com.hyphenate.easeui.provider.EaseEmojiconInfoProvider;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EaseSmileUtils {
    public static final String DELETE_KEY = "em_delete_delete_expression";
    
	public static final String ee_1 = "[):]";
	public static final String ee_2 = "[:D]";
	public static final String ee_3 = "[;)]";
	public static final String ee_4 = "[:-o]";
	public static final String ee_5 = "[:p]";
	public static final String ee_6 = "[(H)]";
	public static final String ee_7 = "[:@]";
	public static final String ee_8 = "[:s]";
	public static final String ee_9 = "[:$]";
	public static final String ee_10 = "[:(]";
	public static final String ee_11 = "[:'(]";
	public static final String ee_12 = "[:|]"; 
	public static final String ee_13 = "[(a)]";
	public static final String ee_14 = "[8o|]";
	public static final String ee_15 = "[8-|]";
	public static final String ee_16 = "[+o(]";
	public static final String ee_17 = "[<o)]";
	public static final String ee_18 = "[|-)]";
	public static final String ee_19 = "[*-)]";
	public static final String ee_20 = "[:-#]";
	public static final String ee_21 = "[:-*]";
	public static final String ee_22 = "[^o)]";
	public static final String ee_23 = "[8-)]";
	public static final String ee_24 = "[(|)]";
	public static final String ee_25 = "[(u)]";
	public static final String ee_26 = "[(S)]";
	public static final String ee_27 = "[(*)]";
	public static final String ee_28 = "[(#)]";
	public static final String ee_29 = "[(R)]";
	public static final String ee_30 = "[({)]";
	public static final String ee_31 = "[(})]";
	public static final String ee_32 = "[(k)]";
	public static final String ee_33 = "[(F)]";
	public static final String ee_34 = "[(W)]";
	public static final String ee_35 = "[(D)]";

	public static final String e_1 = "\uD83D\uDE0A";
	public static final String e_2 = "\uD83D\uDE03";
	public static final String e_3 = "\uD83D\uDE09";
	public static final String e_4 = "\uD83D\uDE2E";
	public static final String e_5 = "\uD83D\uDE0B";
	public static final String e_6 = "\uD83D\uDE0E";
	public static final String e_7 = "\uD83D\uDE21";
	public static final String e_8 = "\uD83D\uDE16";
	public static final String e_9 = "\uD83D\uDE33";
	public static final String e_10 = "\uD83D\uDE1E";
	public static final String e_11 = "\uD83D\uDE2D";
	public static final String e_12 = "\uD83D\uDE10";
	public static final String e_13 = "\uD83D\uDE07";
	public static final String e_14 = "\uD83D\uDE2C";
	public static final String e_15 = "\uD83D\uDE06";
	public static final String e_16 = "\uD83D\uDE31";
	public static final String e_17 = "\uD83C\uDF85";
	public static final String e_18 = "\uD83D\uDE34";
	public static final String e_19 = "\uD83D\uDE15";
	public static final String e_20 = "\uD83D\uDE37";
	public static final String e_21 = "\uD83D\uDE2F";
	public static final String e_22 = "\uD83D\uDE0F";
	public static final String e_23 = "\uD83D\uDE11";
	public static final String e_24 = "\uD83D\uDC96";
	public static final String e_25 = "\uD83D\uDC94";
	public static final String e_26 = "\uD83C\uDF19";
	public static final String e_27 = "\uD83C\uDF1F";
	public static final String e_28 = "\uD83C\uDF1E";
	public static final String e_29 = "\uD83C\uDF08";
	public static final String e_30 = "\uD83D\uDE0D";
	public static final String e_31 = "\uD83D\uDE1A";
	public static final String e_32 = "\uD83D\uDC8B";
	public static final String e_33 = "\uD83C\uDF39";
	public static final String e_34 = "\uD83C\uDF42";
	public static final String e_35 = "\uD83D\uDC4D";

	
	private static final Factory spannableFactory = Factory
	        .getInstance();
	
	private static final Map<Pattern, Object> emoticons = new HashMap<Pattern, Object>();
	

	static {
	    EaseEmojicon[] emojicons = EaseDefaultEmojiconDatas.getDefaultData();
		for (EaseEmojicon emojicon : emojicons) {
			addPattern(emojicon.getEmojiText(), emojicon.getIcon());
		}
	    EaseEmojiconInfoProvider emojiconInfoProvider = EaseIM.getInstance().getEmojiconInfoProvider();
	    if(emojiconInfoProvider != null && emojiconInfoProvider.getTextEmojiconMapping() != null){
	        for(Entry<String, Object> entry : emojiconInfoProvider.getTextEmojiconMapping().entrySet()){
	            addPattern(entry.getKey(), entry.getValue());
	        }
	    }
	    
	}

	/**
	 * add text and icon to the map
	 * @param emojiText-- text of emoji
	 * @param icon -- resource id or local path
	 */
	public static void addPattern(String emojiText, Object icon){
	    emoticons.put(Pattern.compile(Pattern.quote(emojiText)), icon);
	}
	

	/**
	 * replace existing spannable with smiles
	 * @param context
	 * @param spannable
	 * @return
	 */
	public static boolean addSmiles(Context context, Spannable spannable) {
	    boolean hasChanges = false;
	    for (Entry<Pattern, Object> entry : emoticons.entrySet()) {
	        Matcher matcher = entry.getKey().matcher(spannable);
	        while (matcher.find()) {
	            boolean set = true;
	            for (ImageSpan span : spannable.getSpans(matcher.start(),
	                    matcher.end(), ImageSpan.class))
	                if (spannable.getSpanStart(span) >= matcher.start()
	                        && spannable.getSpanEnd(span) <= matcher.end())
	                    spannable.removeSpan(span);
	                else {
	                    set = false;
	                    break;
	                }
	            if (set) {
	                hasChanges = true;
	                Object value = entry.getValue();
	                if(value instanceof String && !((String) value).startsWith("http")){
	                    File file = new File((String) value);
	                    if(!file.exists() || file.isDirectory()){
	                        return false;
	                    }
	                    spannable.setSpan(new ImageSpan(context, Uri.fromFile(file)),
	                            matcher.start(), matcher.end(),
	                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
	                }else{
	                    spannable.setSpan(new ImageSpan(context, (Integer)value),
	                            matcher.start(), matcher.end(),
	                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
	                }
	            }
	        }
	    }
	    
	    return hasChanges;
	}

	public static Spannable getSmiledText(Context context, CharSequence text) {
	    Spannable spannable = spannableFactory.newSpannable(text);
	    addSmiles(context, spannable);
	    return spannable;
	}
	
	public static boolean containsKey(String key){
		boolean b = false;
		for (Entry<Pattern, Object> entry : emoticons.entrySet()) {
	        Matcher matcher = entry.getKey().matcher(key);
	        if (matcher.find()) {
	        	b = true;
	        	break;
	        }
		}
		
		return b;
	}
	
	public static int getSmilesSize(){
        return emoticons.size();
    }
    
	
}
