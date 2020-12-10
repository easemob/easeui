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

import com.bumptech.glide.Glide;
import com.hyphenate.easeui.R;
import com.hyphenate.easeui.adapter.EaseBaseRecyclerViewAdapter;
import com.hyphenate.easeui.modules.contact.model.EaseContactCustomBean;
import com.hyphenate.easeui.modules.contact.model.EaseContactSetStyle;
import com.hyphenate.easeui.widget.EaseImageView;

public class EaseContactCustomAdapter extends EaseBaseRecyclerViewAdapter<EaseContactCustomBean> {
    private EaseContactSetStyle contactSetModel;

    @Override
    public ViewHolder getViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.ease_widget_contact_custom_item, parent, false);
        return new CustomViewHolder(view);
    }

    public void setSettingModel(EaseContactSetStyle settingModel) {
        this.contactSetModel = settingModel;
    }

    public void addItem(int id, int image, String name) {
        EaseContactCustomBean bean = new EaseContactCustomBean();
        bean.setId(id);
        bean.setResourceId(image);
        bean.setName(name);
        this.addData(bean);
    }

    public void addItem(int id, String image, String name) {
        EaseContactCustomBean bean = new EaseContactCustomBean();
        bean.setId(id);
        bean.setImage(image);
        bean.setName(name);
        this.addData(bean);
    }

    private class CustomViewHolder extends ViewHolder<EaseContactCustomBean> {
        private TextView mHeader;
        private EaseImageView mAvatar;
        private TextView mName;
        private ConstraintLayout clUser;

        public CustomViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        @Override
        public void initView(View itemView) {
            mHeader = findViewById(R.id.header);
            mAvatar = findViewById(R.id.avatar);
            mName = findViewById(R.id.name);
            clUser = findViewById(R.id.cl_user);
            if(contactSetModel != null) {
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
        public void setData(EaseContactCustomBean item, int position) {
            mHeader.setVisibility(View.GONE);
            mName.setText(item.getName());
            if(item.getResourceId() != 0) {
                mAvatar.setImageResource(item.getResourceId());
            }else if(TextUtils.isEmpty(item.getImage())) {
                Glide.with(itemView.getContext()).load(item.getImage()).into(mAvatar);
            }
        }
    }
}

