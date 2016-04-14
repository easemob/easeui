package com.easemob.easeui.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import com.easemob.easeui.R;
import com.easemob.easeui.widget.EaseTitleBar;
import com.easemob.util.EMLog;

public abstract class EaseBaseFragment extends Fragment{
    private static final String TAG = EaseBaseFragment.class.getSimpleName();
	protected EaseTitleBar titleBar;
    protected InputMethodManager inputMethodManager;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        titleBar = (EaseTitleBar) getView().findViewById(R.id.title_bar);
        
        initView();
        setUpView();
    }
    
    /**
     * 显示标题栏
     */
    public void showTitleBar(){
        if(titleBar != null){
            titleBar.setVisibility(View.VISIBLE);
        }else{
        	EMLog.e(TAG, "cant find titlebar");
        }
    }
    
    /**
     * 隐藏标题栏
     */
    public void hideTitleBar(){
        if(titleBar != null){
            titleBar.setVisibility(View.GONE);
        }else{
        	EMLog.e(TAG, "cant find titlebar");
        }
    }
    
    protected void hideSoftKeyboard() {
        if (getActivity().getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
            if (getActivity().getCurrentFocus() != null)
                inputMethodManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
    
    /**
     * 初始化控件
     */
    protected abstract void initView();
    
    /**
     * 设置属性，监听等
     */
    protected abstract void setUpView();
    
    
}
