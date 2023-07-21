package com.hyphenate.easeui.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.text.LineBreaker;
import android.net.Uri;
import android.os.Build;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMCustomMessageBody;
import com.hyphenate.chat.EMFileMessageBody;
import com.hyphenate.chat.EMImageMessageBody;
import com.hyphenate.chat.EMLocationMessageBody;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMVideoMessageBody;
import com.hyphenate.chat.EMVoiceMessageBody;
import com.hyphenate.easeui.EaseIM;
import com.hyphenate.easeui.R;
import com.hyphenate.easeui.constants.EaseConstant;
import com.hyphenate.easeui.domain.EaseEmojicon;
import com.hyphenate.easeui.domain.EaseUser;
import com.hyphenate.easeui.interfaces.IUIKitInterface;
import com.hyphenate.easeui.manager.EaseChatInterfaceManager;
import com.hyphenate.easeui.utils.EaseCommonUtils;
import com.hyphenate.easeui.utils.EaseEditTextUtils;
import com.hyphenate.easeui.utils.EaseFileUtils;
import com.hyphenate.easeui.utils.EaseSmileUtils;
import com.hyphenate.easeui.utils.EaseUserUtils;
import com.hyphenate.exceptions.HyphenateException;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class EaseChatQuoteView extends LinearLayout {
    private static final float IMAGE_DEFAULT_WIDTH = 36;
    private final Context mContext;
    private TextView quoteContent;
    private ImageView quoteVoiceIcon;
    private TextView quoteVoiceName;
    private TextView quoteVoiceLength;
    private TextView quoteVideoName;
    private EaseImageView quoteVideoIcon;
    private TextView quoteFileTitle;
    private TextView quoteFileName;
    private ImageView quoteFileIcon;
    private TextView quoteImageName;
    private EaseImageView quoteImageView;
    private TextView quoteLocationAddress;
    private TextView quoteBigExpressionTitle;
    private TextView quoteDefaultView;
    private EaseImageView quoteBigExpressionImg;
    private ViewGroup quoteTextLayout;
    private ViewGroup quoteVoiceLayout;
    private ViewGroup quoteVideoLayout;
    private ViewGroup quoteFileLayout;
    private ViewGroup quoteLocationLayout;
    private ViewGroup quoteImageLayout;
    private ViewGroup quoteBigExpressionLayout;
    private ViewGroup quoteDefaultLayout;
    private EMMessage message;
    private EMMessage quoteMessage;
    private final Map<String,String> receiveMsgTypes = new HashMap<String,String>();

    public EaseChatQuoteView(Context context) {
        this(context, null);
    }

    public EaseChatQuoteView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EaseChatQuoteView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        initMsgType();
        @SuppressLint("CustomViewStyleable")
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ease_quote_style);
        boolean isSender = typedArray.getBoolean(R.styleable.ease_quote_style_ease_chat_item_sender, false);
        typedArray.recycle();

        if (isSender) {
            LayoutInflater.from(context).inflate(R.layout.ease_row_sent_quote_layout, this);
        } else {
            LayoutInflater.from(context).inflate(R.layout.ease_row_received_quote_layout, this);
        }

        quoteContent = findViewById(R.id.tv_subContent);

        quoteVoiceName = findViewById(R.id.iv_voice_name);
        quoteVoiceLength = findViewById(R.id.tv_voice_length);
        quoteVoiceIcon = findViewById(R.id.iv_voice);

        quoteVideoName = findViewById(R.id.tv_video_subContent);
        quoteVideoIcon = findViewById(R.id.chatting_content_iv);

        quoteFileTitle = findViewById(R.id.iv_file_title);
        quoteFileName = findViewById(R.id.tv_file_name);
        quoteFileIcon = findViewById(R.id.iv_file);

        quoteImageName = findViewById(R.id.tv_image_subContent);
        quoteImageView = findViewById(R.id.iv_quote_picture);

        quoteLocationAddress = findViewById(R.id.tv_location_address);

        quoteBigExpressionTitle =  findViewById(R.id.tv_bigExpression_subContent);
        quoteBigExpressionImg = findViewById(R.id.iv_bigExpression_picture);

        quoteDefaultView = findViewById(R.id.tv_default);

        /////////// layout //////////
        quoteTextLayout = findViewById(R.id.subBubble_text_layout);
        quoteVoiceLayout = findViewById(R.id.subBubble_audio_layout);
        quoteVideoLayout = findViewById(R.id.subBubble_video_layout);
        quoteFileLayout = findViewById(R.id.subBubble_file_layout);
        quoteImageLayout = findViewById(R.id.subBubble_image_layout);
        quoteLocationLayout = findViewById(R.id.subBubble_location_layout);
        quoteBigExpressionLayout = findViewById(R.id.subBubble_bigExpression_layout);
        quoteDefaultLayout = findViewById(R.id.subBubble_default_layout);

        setTextBreakStrategy(quoteContent);
        setTextBreakStrategy(quoteVoiceName);
        setTextBreakStrategy(quoteVideoName);
        setTextBreakStrategy(quoteFileTitle);
        setTextBreakStrategy(quoteImageName);
        setTextBreakStrategy(quoteLocationAddress);
        setTextBreakStrategy(quoteBigExpressionTitle);
        setTextBreakStrategy(quoteDefaultView);
    }

    private void setTextBreakStrategy(TextView textView) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            textView.setBreakStrategy(LineBreaker.BREAK_STRATEGY_BALANCED);
        }
    }

    public void updateMessageInfo(EMMessage quoteMsg){
        this.message = quoteMsg;
        JSONObject jsonObject = null;
        if (message != null){
            try {
                String msgQuote = message.getStringAttribute(EaseConstant.QUOTE_MSG_QUOTE,"");
                if (!TextUtils.isEmpty(msgQuote)){
                    try {
                        jsonObject = new JSONObject(msgQuote);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }else {
                    jsonObject = message.getJSONObjectAttribute(EaseConstant.QUOTE_MSG_QUOTE);
                }

                if (jsonObject != null){
                    setContent(jsonObject);
                    this.setVisibility(VISIBLE);
                }else {
                    this.setVisibility(GONE);
                }
            } catch (HyphenateException e) {
                e.printStackTrace();
            }
        }
    }

    private void setContent(JSONObject jsonObject){
        try {
            String quoteMsgID = jsonObject.getString(EaseConstant.QUOTE_MSG_ID);
            String quoteSender = jsonObject.getString(EaseConstant.QUOTE_MSG_SENDER);
            String quoteType = jsonObject.getString(EaseConstant.QUOTE_MSG_TYPE);
            String quoteContent = jsonObject.getString(EaseConstant.QUOTE_MSG_PREVIEW);

            String quoteSenderNick = "";
            if (!TextUtils.isEmpty(quoteSender)){
                EaseUser user = EaseUserUtils.getUserInfo(quoteSender);
                if (user == null){
                    quoteSenderNick = quoteSender;
                }else {
                    if (TextUtils.isEmpty(user.getNickname())){
                        quoteSenderNick = user.getUsername();
                    }else {
                        quoteSenderNick = user.getNickname();
                    }
                }
            }
            quoteMessage = EMClient.getInstance().chatManager().getMessage(quoteMsgID);
            isShowType(quoteSenderNick,getQuoteMessageType(quoteType),quoteContent);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void initMsgType(){
        receiveMsgTypes.put("text",EMMessage.Type.TXT.name());
        receiveMsgTypes.put("img",EMMessage.Type.IMAGE.name());
        receiveMsgTypes.put("video",EMMessage.Type.VIDEO.name());
        receiveMsgTypes.put("location",EMMessage.Type.LOCATION.name());
        receiveMsgTypes.put("audio",EMMessage.Type.VOICE.name());
        receiveMsgTypes.put("file",EMMessage.Type.FILE.name());
        receiveMsgTypes.put("cmd",EMMessage.Type.CMD.name());
        receiveMsgTypes.put("custom",EMMessage.Type.CUSTOM.name());
    }

    private EMMessage.Type getQuoteMessageType(String quoteType){
        if (receiveMsgTypes.get(quoteType) == null) return EMMessage.Type.TXT;
        return EMMessage.Type.valueOf(receiveMsgTypes.get(quoteType));
    }

    private void reSetLayout(){
        quoteTextLayout.setVisibility(GONE);
        quoteVoiceLayout.setVisibility(GONE);
        quoteVideoLayout.setVisibility(GONE);
        quoteFileLayout.setVisibility(GONE);
        quoteImageLayout.setVisibility(GONE);
        quoteLocationLayout.setVisibility(GONE);
        quoteBigExpressionLayout.setVisibility(GONE);
        quoteDefaultLayout.setVisibility(GONE);
    }

    private void isShowType(String quoteSender,EMMessage.Type quoteMsgType,String content){
        reSetLayout();
        switch (quoteMsgType){
            case TXT:
                txtTypeDisplay(quoteMessage,quoteSender,content);
                break;
            case IMAGE:
                imageTypeDisplay(quoteMessage,quoteSender,content);
                break;
            case VIDEO:
                videoTypeDisplay(quoteMessage,quoteSender,content);
                break;
            case LOCATION:
                locationTypeDisplay(quoteMessage,quoteSender,content);
                break;
            case VOICE:
                voiceTypeDisplay(quoteMessage,quoteSender,content);
                break;
            case FILE:
                fileTypeDisplay(quoteMessage,quoteSender,content);
                break;
            default:
                IUIKitInterface listener = EaseChatInterfaceManager.getInstance().getInterface(EaseConstant.INTERFACE_QUOTE_MESSAGE_TAG);
                if(listener instanceof IChatQuoteMessageShow) {
                    SpannableString result = ((IChatQuoteMessageShow)listener).itemQuoteMessageShow(quoteMessage, quoteMsgType, quoteSender, content);
                    if(result != null) {
                        reSetLayout();
                        quoteDefaultView.setText(result);
                        quoteDefaultLayout.setVisibility(VISIBLE);
                    }
                }
                break;
        }
    }

    protected void txtTypeDisplay(EMMessage quoteMessage,String quoteSender,String content){
        StringBuilder builder = new StringBuilder();
        if (quoteMessage != null && quoteMessage.getBooleanAttribute(EaseConstant.MESSAGE_ATTR_IS_BIG_EXPRESSION, false)){
            showBigExpression(quoteMessage);
            builder.append(quoteSender).append(": ");
            SpannableString bigSpan = new SpannableString(builder.toString());
            quoteBigExpressionTitle.setText(bigSpan);
            quoteBigExpressionLayout.setVisibility(View.VISIBLE);
        }else {
            String text = getContext().getString(R.string.colon_and_space, quoteSender, content);
            Spannable textSpan = EaseSmileUtils.getSmiledText(mContext, text);
            SpannableString spannableString = new SpannableString(textSpan);
            quoteContent.setText(spannableString);
            quoteContent.setEllipsize(TextUtils.TruncateAt.END);
            quoteContent.setMaxLines(2);
            quoteTextLayout.setVisibility(View.VISIBLE);
        }
    }

    protected void imageTypeDisplay(EMMessage quoteMessage,String quoteSender,String content){
        StringBuilder builder = new StringBuilder();
        if (quoteMessage == null){
            builder.append(quoteSender).append(": ");
            quoteImageView.setImageResource(R.drawable.ease_default_image);
            ViewGroup.LayoutParams layoutParams = quoteImageView.getLayoutParams();
            layoutParams.height = layoutParams.width;
        }else {
            builder.append(quoteSender).append(": ");
            showImageView(quoteMessage);
        }
        SpannableString imageSpan = new SpannableString(builder.toString());
        quoteImageName.setText(imageSpan);
        quoteImageLayout.setVisibility(View.VISIBLE);
    }

    protected void videoTypeDisplay(EMMessage quoteMessage,String quoteSender,String content){
        StringBuilder builder = new StringBuilder();
        if (quoteMessage == null){
            builder.append(quoteSender).append(": ");
            quoteVideoName.setText(builder);
            quoteVideoIcon.setImageResource(R.drawable.ease_default_image);
            ViewGroup.LayoutParams layoutParams = quoteVideoIcon.getLayoutParams();
            layoutParams.height = layoutParams.width;
        }else {

            builder.append(quoteSender).append(": ");
            SpannableString videoSpan = new SpannableString(builder.toString());
            quoteVideoName.setText(videoSpan);
            showVideoThumbView(quoteMessage);
        }
        quoteVideoLayout.setVisibility(View.VISIBLE);
    }

    protected void locationTypeDisplay(EMMessage quoteMessage,String quoteSender,String content){
        String title = "";
        int startIndex = quoteSender.length() + 1;
        if (quoteMessage == null){
            title = quoteSender + ": " + content;
        }else {
            if (quoteMessage.getBody() instanceof EMLocationMessageBody){
                EMLocationMessageBody locationMessageBody = (EMLocationMessageBody)quoteMessage.getBody();
                title = quoteSender + ": " + locationMessageBody.getAddress();
            }
        }
        SpannableStringBuilder locationSb = new SpannableStringBuilder(title);
        CenterImageSpan span = new CenterImageSpan(mContext, R.drawable.ease_chat_item_menu_location);
        if (locationSb.length() >= startIndex + 2){
            locationSb.setSpan(span, startIndex, startIndex + 2, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        }
        quoteLocationAddress.setText(locationSb);
        quoteLocationLayout.setVisibility(View.VISIBLE);
    }

    protected void voiceTypeDisplay(EMMessage quoteMessage,String quoteSender,String content){
        StringBuilder builder = new StringBuilder();
        if (quoteMessage == null){
            builder.append(quoteSender).append(": ");
            quoteVoiceLength.setText(content);
        }else {
            String voiceLength = "";
            if (quoteMessage.getBody() instanceof EMVoiceMessageBody){
                EMVoiceMessageBody voiceMessageBody = (EMVoiceMessageBody) quoteMessage.getBody();
                if (voiceMessageBody != null && voiceMessageBody.getLength() > 0){
                    voiceLength = voiceMessageBody.getLength() + "\"";
                }
                builder.append(quoteSender).append(": ");
            }
            quoteVoiceLength.setText(voiceLength);
        }
        SpannableString voiceSpan = new SpannableString(builder.toString());
        quoteVoiceName.setText(voiceSpan);
        quoteVoiceLayout.setVisibility(View.VISIBLE);
    }

    protected void fileTypeDisplay(EMMessage quoteMessage,String quoteSender,String content){
        StringBuilder builder = new StringBuilder();
        if (quoteMessage == null){
            builder.append(quoteSender).append(": ");
            quoteFileIcon.setVisibility(View.VISIBLE);
            quoteFileName.setText(content);
        }else {
            String fileName = "";
            if (quoteMessage.getBody() instanceof EMFileMessageBody){
                EMFileMessageBody fileMessageBody = (EMFileMessageBody) quoteMessage.getBody();
                if (fileMessageBody != null && !TextUtils.isEmpty(fileMessageBody.getFileName())){
                    fileName = EaseEditTextUtils.ellipsizeMiddleString(quoteFileName,
                            fileMessageBody.getFileName(),
                            1,
                            quoteFileName.getWidth() - quoteFileName.getPaddingLeft() - quoteFileName.getPaddingRight());
                }
                builder.append(quoteSender).append(": ");
            }
            quoteFileName.setText(fileName);
        }
        SpannableString fileSpan = new SpannableString(builder.toString());
        quoteFileTitle.setText(fileSpan);
        quoteFileLayout.setVisibility(View.VISIBLE);
    }

    /**
     * show video thumbnails
     * @param quoteMessage
     */
    private void showVideoThumbView(EMMessage quoteMessage) {
        String imageUri = null;
        String imageUrl = "";
        int width = 0;
        int height = 0;
        if (quoteMessage.getBody() instanceof EMVideoMessageBody){
            EMVideoMessageBody videoMessageBody = (EMVideoMessageBody) quoteMessage.getBody();
            if (videoMessageBody != null){
                imageUri = videoMessageBody.getLocalThumb();
                imageUrl = videoMessageBody.getThumbnailUrl();
                int thumbnailHeight = videoMessageBody.getThumbnailHeight();
                int thumbnailWidth = videoMessageBody.getThumbnailWidth();
                width = (int) EaseCommonUtils.dip2px(mContext, IMAGE_DEFAULT_WIDTH);
                height = (int) (width * 1.0f * thumbnailHeight / thumbnailWidth);
            }
        }
        Glide.with(mContext)
                .load(EaseFileUtils.isFileExistByUri(getContext(), Uri.parse(imageUri)) ? imageUri : imageUrl)
                .override(width, height)
                .apply(new RequestOptions()
                        .error(R.drawable.ease_default_image))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(quoteVideoIcon);
    }

    /**
     * load image into image view
     *
     */
    private void showImageView(final EMMessage message) {
        if (message.getType() == EMMessage.Type.IMAGE){
            Uri imageUri = null;
            String imageUrl = "";
            int width = 0;
            int height = 0;
            EMImageMessageBody imageMessageBody = (EMImageMessageBody) message.getBody();
            if (imageMessageBody != null){
                imageUri = imageMessageBody.getLocalUri();
                imageUrl = imageMessageBody.getRemoteUrl();
                int thumbnailHeight = imageMessageBody.getHeight();
                int thumbnailWidth = imageMessageBody.getWidth();
                width = (int) EaseCommonUtils.dip2px(mContext, IMAGE_DEFAULT_WIDTH);
                height = (int) (width * 1.0f * thumbnailHeight / thumbnailWidth);
            }
            Glide.with(mContext)
                    .load(!EaseFileUtils.isFileExistByUri(getContext(), imageUri) ? imageUrl : imageUri)
                    .override(width, height)
                    .apply(new RequestOptions()
                            .error(R.drawable.ease_default_image))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(quoteImageView);
        }
    }

    private void showBigExpression(EMMessage message){
        String emojiconId = message.getStringAttribute(EaseConstant.MESSAGE_ATTR_EXPRESSION_ID, null);
        EaseEmojicon emojicon = null;
        if(EaseIM.getInstance().getEmojiconInfoProvider() != null){
            emojicon =  EaseIM.getInstance().getEmojiconInfoProvider().getEmojiconInfo(emojiconId);
        }
        if(emojicon != null){
            if(emojicon.getBigIcon() != 0){

                Glide.with(mContext).load(emojicon.getIcon())
                        .apply(RequestOptions.placeholderOf(R.drawable.ease_default_expression))
                        .into(quoteBigExpressionImg);
            }else if(emojicon.getBigIconPath() != null){
                Glide.with(mContext).load(emojicon.getBigIconPath())
                        .apply(RequestOptions.placeholderOf(R.drawable.ease_default_expression))
                        .into(quoteBigExpressionImg);
            }else{
                quoteBigExpressionImg.setImageResource(R.drawable.ease_default_expression);
            }
        }
    }

    public void clear(){
        message = null;
        quoteMessage = null;
    }

    public interface IChatQuoteMessageShow extends IUIKitInterface {
        SpannableString itemQuoteMessageShow(EMMessage quoteMessage, EMMessage.Type quoteMsgType, String quoteSender, String quoteContent);
    }

}
