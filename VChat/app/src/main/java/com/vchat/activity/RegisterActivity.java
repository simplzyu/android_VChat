package com.vchat.activity;

import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;

import com.vchat.R;
import com.vchat.util.Common;
import com.vchat.util.PreferencesUtils;
import com.vchat.util.ToastUtil;
import com.vchat.util.XmppConnectionManager;
import com.vchat.util.XmppUtil;
import com.vchat.view.LoadingDialog;
import com.vchat.view.TitleBarView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.vchat.util.SendMail;


/**
 * 注册界面
 */
public class RegisterActivity extends Activity {
	private Context mContext;
	private Button btn_complete;
	private Button btn_sendMail;
	private TitleBarView mTitleBarView;
	private EditText et_name,et_password;
	private EditText et_mail,et_code;
	
	private String account,password;
	String id_code;
	private LoadingDialog loadDialog;
	
	private XmppConnectionManager xmppConnectionManager;
	
	@SuppressLint("HandlerLeak")
	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if(loadDialog.isShowing()){
				loadDialog.dismiss();
			}
			switch (msg.what) {
			case 0:
				ToastUtil.showLongToast(mContext, "注册失败");
				break;
			case 1:
				ToastUtil.showLongToast(mContext, "注册成功，请牢记您的账号和密码");
				PreferencesUtils.putSharePre(mContext, "username", account);
				PreferencesUtils.putSharePre(mContext, "pwd", password);
				finish();
				break;
			case 2:
				ToastUtil.showLongToast(mContext, "该昵称已被注册");
				break;
			case 3:
				ToastUtil.showLongToast(mContext, "注册失败");
				break;
			case 4:
				ToastUtil.showLongToast(mContext, "注册失败,请检查您的网络");
				break;
			default:
				break;
			}
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register_userinfo);
		mContext=this;
		xmppConnectionManager=XmppConnectionManager.getInstance();
		loadDialog=new LoadingDialog(this);
		findView();
		initTitleView();
		init();
	}
	
	private void findView(){
		mTitleBarView=(TitleBarView) findViewById(R.id.title_bar);
		btn_complete=(Button) findViewById(R.id.register_complete);
		btn_sendMail=(Button) findViewById(R.id.btn_id_code);
		
		et_name=(EditText) findViewById(R.id.name);//账号
		et_password=(EditText) findViewById(R.id.password);//密码
		et_mail = (EditText) findViewById(R.id.text_mail);
		et_code = (EditText) findViewById(R.id.text_id_code);
		
	}
	
	private void init(){
		btn_complete.setOnClickListener(completeOnClickListener);
		btn_sendMail.setOnClickListener(completeOnClickListener);
	}
	
	private void initTitleView(){
		mTitleBarView.setCommonTitle(View.VISIBLE, View.VISIBLE,View.GONE, View.GONE);
		mTitleBarView.setTitleText(R.string.title_register_info);
		mTitleBarView.setBtnLeft(R.drawable.fft, R.string.back);
		mTitleBarView.setBtnLeftOnclickListener(new OnClickListener() {	
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}
	
	/**
	 * 点击注册
	 */
	private OnClickListener completeOnClickListener=new OnClickListener() {
		@Override
		public void onClick(View v) {

			if(v.getId() == R.id.btn_id_code){
				id_code = Common.getIdCode();
				String toAddr = et_mail.getText().toString();
				if(TextUtils.isEmpty(toAddr)){
					ToastUtil.showLongToast(mContext, "请填写邮箱");
					return;
				}else{
					if(Common.checkEmail(toAddr)){  //检查邮箱格式
						if(Common.getNetworkState(RegisterActivity.this)) {
							SendMail sendMail = new SendMail(et_name.getText().toString(), toAddr, "验证码：" + id_code);
							Thread t = new Thread(sendMail);
							t.start();
							ToastUtil.showLongToast(mContext,"已发送");
						}else{
							ToastUtil.showLongToast(mContext,"网络错误");
						}
					}
				}
			}

	//		Mail.sendMail("郭振宇","191163281@qq.com","www.vchat.com","验证码：8089");
			if(v.getId() == R.id.register_complete){
				Log.d("code: ", id_code);
				String text_id_code = et_code.getText().toString();
				if(text_id_code.equals(id_code)){
					doReg();
				}else{
					ToastUtil.showLongToast(mContext, "验证码错误！");
				}

			}


		}
	};
	
	void doReg(){
		account=et_name.getText().toString();
		password=et_password.getText().toString();
		if(TextUtils.isEmpty(account)){
			ToastUtil.showLongToast(mContext, "请填写昵称");
			return;
		}
		if(TextUtils.isEmpty(password)){
			ToastUtil.showLongToast(mContext, "请填写密码");
			return;
		}
		loadDialog.setTitle("正在注册...");
		loadDialog.show();
		new Thread(new Runnable() {
			@Override
			public void run() {
				XMPPConnection mXMPPConnection=xmppConnectionManager.init();
				try {
//					Context context = getApplicationContext();
//					SmackAndroid.init(context);
					mXMPPConnection.connect();
					int result=XmppUtil.register(mXMPPConnection, account, password);
					mHandler.sendEmptyMessage(result);
				} catch (XMPPException e) {
					e.printStackTrace();
					mHandler.sendEmptyMessage(4);
				}
			}
		}).start();
	}

}
