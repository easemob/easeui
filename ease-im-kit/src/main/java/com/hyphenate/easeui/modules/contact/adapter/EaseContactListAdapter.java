package com.hyphenate.easeui.modules.contact.adapter;

import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.hyphenate.easeui.EaseIM;
import com.hyphenate.easeui.R;
import com.hyphenate.easeui.adapter.EaseBaseRecyclerViewAdapter;
import com.hyphenate.easeui.domain.EaseUser;
import com.hyphenate.easeui.modules.contact.model.EaseContactSetStyle;
import com.hyphenate.easeui.provider.EaseUserProfileProvider;
import com.hyphenate.easeui.utils.EaseUserUtils;
import com.hyphenate.easeui.widget.EaseImageView;

public class EaseContactListAdapter extends EaseBaseRecyclerViewAdapter<EaseUser> {
    private int emptyLayoutResource;
    private EaseContactSetStyle contactSetModel;

    @Override
    public ViewHolder getViewHolder(ViewGroup parent, int viewType) {
        return new ContactViewHolder(LayoutInflater.from(mContext).inflate(R.layout.ease_widget_contact_item, parent, false));
    }

    @Override
    public int getEmptyLayoutId() {
        if(emptyLayoutResource != 0) {
            return emptyLayoutResource;
        }
        return R.layout.ease_layout_no_data_show_nothing;
    }

    public void setSettingModel(EaseContactSetStyle settingModel) {
        this.contactSetModel = settingModel;
    }

    /**
     * 设置无数据时的布局
     * @param emptyLayoutResource
     */
    public void setEmptyLayoutResource(int emptyLayoutResource) {
        this.emptyLayoutResource = emptyLayoutResource;
    }

    private class ContactViewHolder extends ViewHolder<EaseUser> {
        private TextView mHeader;
        private EaseImageView mAvatar;
        private TextView mName;
        private TextView mSignature;
        private TextView mUnreadMsgNumber;
        private ConstraintLayout clUser;

        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        @Override
        public void initView(View itemView) {
            mHeader = findViewById(R.id.header);
            mAvatar = findViewById(R.id.avatar);
            mName = findViewById(R.id.name);
            mSignature = findViewById(R.id.signature);
            mUnreadMsgNumber = findViewById(R.id.unread_msg_number);
            clUser = findViewById(R.id.cl_user);
            EaseUserUtils.setUserAvatarStyle(mAvatar);
            if(contactSetModel != null) {
                float headerTextSize = contactSetModel.getHeaderTextSize();
                if(headerTextSize != 0) {
                    mHeader.setTextSize(TypedValue.COMPLEX_UNIT_PX, headerTextSize);
                }
                int headerTextColor = contactSetModel.getHeaderTextColor();
                if(headerTextColor != 0) {
                    mHeader.setTextColor(headerTextColor);
                }
                Drawable headerBgDrawable = contactSetModel.getHeaderBgDrawable();
                if(headerBgDrawable != null) {
                    mHeader.setBackground(headerBgDrawable);
                }
                float titleTextSize = contactSetModel.getTitleTextSize();
                if(titleTextSize != 0) {
                    mName.setTextSize(TypedValue.COMPLEX_UNIT_PX, titleTextSize);
                }
                int titleTextColor = contactSetModel.getTitleTextColor();
                if(titleTextColor != 0) {
                    mName.setTextColor(titleTextColor);
                }
                Drawable avatarDefaultSrc = contactSetModel.getAvatarDefaultSrc();
                if(avatarDefaultSrc != null) {
                    mAvatar.setImageDrawable(avatarDefaultSrc);
                }
                float avatarRadius = contactSetModel.getAvatarRadius();
                if(avatarRadius != 0) {
                    mAvatar.setRadius((int) avatarRadius);
                }
                float borderWidth = contactSetModel.getBorderWidth();
                if(borderWidth != 0) {
                    mAvatar.setBorderWidth((int) borderWidth);
                }
                int borderColor = contactSetModel.getBorderColor();
                if(borderColor != 0) {
                    mAvatar.setBorderColor(borderColor);
                }
                mAvatar.setShapeType(contactSetModel.getShapeType());
                float avatarSize = contactSetModel.getAvatarSize();
                if(avatarSize != 0) {
                    ViewGroup.LayoutParams mAvatarLayoutParams = mAvatar.getLayoutParams();
                    mAvatarLayoutParams.height = (int) avatarSize;
                    mAvatarLayoutParams.width = (int) avatarSize;
                }
                float itemHeight = contactSetModel.getItemHeight();
                if(itemHeight != 0) {
                    ViewGroup.LayoutParams userLayoutParams = clUser.getLayoutParams();
                    userLayoutParams.height = (int) itemHeight;
                }
                Drawable bgDrawable = contactSetModel.getBgDrawable();
                clUser.setBackground(bgDrawable);
            }
        }

        @Override
        public void setData(EaseUser item, int position) {
            EaseUserProfileProvider provider = EaseIM.getInstance().getUserProvider();
            if(provider != null) {
                EaseUser user = provider.getUser(item.getUsername());
                if(user != null) {
                    item = user;
                }
            }
            String header = item.getInitialLetter();
            mHeader.setVisibility(View.GONE);
            if(position == 0 || (header != null && !header.equals(getItem(position -1).getInitialLetter()))) {
                if(!TextUtils.isEmpty(header)) {
                    mHeader.setVisibility(View.VISIBLE);
                    if(contactSetModel != null) {
                        mHeader.setVisibility(contactSetModel.isShowItemHeader() ? View.VISIBLE : View.GONE);
                    }
                    mHeader.setText(header);
                }
            }
            mName.setText(item.getNickname());
            Glide.with(mContext)
                    .load(item.getAvatar())
                    .error(contactSetModel.getAvatarDefaultSrc() != null ? contactSetModel.getAvatarDefaultSrc()
                            : ContextCompat.getDrawable(mContext, R.drawable.ease_default_avatar))
                    .into(mAvatar);
        }
    }
}
