package com.hyphenate.easeuisimpledemo.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.hyphenate.EMCallBack;
import com.hyphenate.chat.EMClient;
import com.hyphenate.easeui.ui.EaseBaseActivity;
import com.hyphenate.easeuisimpledemo.R;

public class LoginActivity extends EaseBaseActivity{
    private EditText usernameView;
    private EditText pwdView;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        if(EMClient.getInstance().isLoggedInBefore()){
            //登录过直接进入主页面
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
        
        setContentView(R.layout.activity_login);
        usernameView = (EditText) findViewById(R.id.et_username);
        pwdView = (EditText) findViewById(R.id.et_password);
        Button loginBtn = (Button) findViewById(R.id.btn_login);
        
        loginBtn.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                //登录
                EMClient.getInstance().login(usernameView.getText().toString(), pwdView.getText().toString(), new EMCallBack() {
                    
                    @Override
                    public void onSuccess() {
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    }
                    
                    @Override
                    public void onProgress(int progress, String status) {
                        
                    }
                    
                    @Override
                    public void onError(int code, String error) {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(getApplicationContext(), "登录失败", 0).show();
                            }
                        });
                    }
                });
            }
        });
        
    }
}
