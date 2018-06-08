package com.vchat.activity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.jivesoftware.smack.XMPPException;
import org.json.JSONException;
import org.json.JSONObject;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;
import com.vchat.WeChatApplication;
import com.vchat.R;
import com.vchat.adapter.ChatAdapter;
import com.vchat.adapter.FaceVPAdapter;
import com.vchat.bean.Msg;
import com.vchat.bean.Session;
import com.vchat.speech.TtsSettings;
import com.vchat.db.ChatMsgDao;
import com.vchat.db.SessionDao;
import com.vchat.speech.util.JsonParser;
import com.vchat.util.Const;
import com.vchat.util.ExpressionUtil;
import com.vchat.util.PreferencesUtils;
import com.vchat.util.ToastUtil;
import com.vchat.util.XmppUtil;
import com.vchat.view.CircleImageView;
import com.vchat.view.DropdownListView;
import com.vchat.view.DropdownListView.OnRefreshListenerHeader;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.ClipboardManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.iflytek.cloud.SpeechRecognizer;


/**
 * 聊天界面
 */
@SuppressLint("SimpleDateFormat")
public class ChatActivity extends Activity implements OnClickListener, OnRefreshListenerHeader {
    private ViewPager mViewPager;
    private LinearLayout mDotsLayout;
    private EditText input;
    private TextView send;
    private DropdownListView mListView;
    private ChatAdapter mLvAdapter;
    private ChatMsgDao msgDao;
    private SessionDao sessionDao;

    private LinearLayout chat_face_container, chat_add_container;
    private ImageView image_face;//表情图标
    private ImageView image_add;//更多图标


    private CircleImageView from_head;


    private Button btn_chat_voice;

    // 用HashMap存储语音结果
    private HashMap<String, String> mIatResults = new LinkedHashMap<String, String>();
    // 语音听写对象
    private SpeechRecognizer mIat;
    private SharedPreferences mSharedPreferences;
    // 语音听写UI
    private RecognizerDialog mIatDialog;

    int ret = 0;
    // 引擎类型
    private String mEngineType = SpeechConstant.TYPE_CLOUD;

    // 缓冲进度
    private int mPercentForBuffering = 0;
    // 播放进度
    private int mPercentForPlaying = 0;

    //合成
    // 语音合成对象
    private SpeechSynthesizer mTts;
    // 默认发音人
    private String voicer="xiaoyan";


    private TextView tv_title, tv_pic,//图片
            tv_camera,//拍照
            tv_loc;//位置

    //表情图标每页6列4行
    private int columns = 6;
    private int rows = 4;
    //每页显示的表情view
    private List<View> views = new ArrayList<View>();
    //表情列表
    private List<String> staticFacesList;
    //消息
    private List<Msg> listMsg;
    private SimpleDateFormat sd;
    private NewMsgReciver newMsgReciver;
    private MsgOperReciver msgOperReciver;
    private LayoutInflater inflater;
    private int offset;
    private String I, YOU;//为了好区分，I就是自己，YOU就是对方

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    mLvAdapter.notifyDataSetChanged();
                    break;
            }
        }
    };

    @SuppressLint("ShowToast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_chat);
        I = PreferencesUtils.getSharePreStr(this, "username");
        YOU = getIntent().getStringExtra("from");
        inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        tv_title = (TextView) findViewById(R.id.tv_title);
        tv_title.setText(YOU);
        sd = new SimpleDateFormat("MM-dd HH:mm");
        msgDao = new ChatMsgDao(this);
        sessionDao = new SessionDao(this);
        msgOperReciver = new MsgOperReciver();
        newMsgReciver = new NewMsgReciver();
        IntentFilter intentFilter = new IntentFilter(Const.ACTION_MSG_OPER);
        registerReceiver(msgOperReciver, intentFilter);
        intentFilter = new IntentFilter(Const.ACTION_NEW_MSG);
        registerReceiver(newMsgReciver, intentFilter);
        staticFacesList = ExpressionUtil.initStaticFaces(this);
        //初始化控件
        initViews();
        //初始化表情
        initViewPager();
        //初始化更多选项（即表情图标右侧"+"号内容）
        initAdd();
        //初始化数据
        initData();
        //更新与该用户的聊天记录全部为已读
        updateMsgToReaded();
    }


    private void updateMsgToReaded() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                msgDao.updateAllMsgToRead(YOU, I);
            }
        }).start();
    }


    /**
     * 初始化控件
     */
    private void initViews() {
        mListView = (DropdownListView) findViewById(R.id.message_chat_listview);
        //表情图标
        image_face = (ImageView) findViewById(R.id.image_face);
        //更多图标
        image_add = (ImageView) findViewById(R.id.image_add);
        //表情布局
        chat_face_container = (LinearLayout) findViewById(R.id.chat_face_container);
        //更多
        chat_add_container = (LinearLayout) findViewById(R.id.chat_add_container);


        //语音按键
        btn_chat_voice = (Button) findViewById(R.id.btn_chat_voice);
        // 初始化识别无UI识别对象
        // 使用SpeechRecognizer对象，可根据回调消息自定义界面；
        mIat = SpeechRecognizer.createRecognizer(this, mInitListener);
        mSharedPreferences = getSharedPreferences(TtsSettings.PREFER_NAME, MODE_PRIVATE);
        // 初始化听写Dialog，如果只使用有UI听写功能，无需创建SpeechRecognizer
        // 使用UI听写功能，请根据sdk文件目录下的notice.txt,放置布局文件和图片资源
        mIatDialog = new RecognizerDialog(ChatActivity.this, mInitListener);
        // 初始化合成对象
        mTts = SpeechSynthesizer.createSynthesizer(this, mTtsInitListener);


        from_head = (CircleImageView) findViewById(R.id.chatfrom_icon);

        mViewPager = (ViewPager) findViewById(R.id.face_viewpager);
        mViewPager.setOnPageChangeListener(new PageChange());
        //表情下小圆点
        mDotsLayout = (LinearLayout) findViewById(R.id.face_dots_container);
        input = (EditText) findViewById(R.id.input_sms);
        send = (TextView) findViewById(R.id.send_sms);
        input.setOnClickListener(this);

        //表情按钮
        image_face.setOnClickListener(this);
        //更多按钮
        image_add.setOnClickListener(this);
        // 发送
        send.setOnClickListener(this);

        //speak
        btn_chat_voice.setOnClickListener(this);

        //	from_head.setOnClickListener(this);

        //输入内容，出现发送键
        input.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                System.out.println("-----------before");
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                System.out.println("------------on");
                String text = input.getText().toString();
                System.out.println(text);
                if (text != null && !text.equals("")) {
                    send.setVisibility(View.VISIBLE);
                    image_add.setVisibility(View.GONE);
                } else {
                    send.setVisibility(View.GONE);
                    image_add.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                System.out.println("-------------after");
            }
        });

        mListView.setOnRefreshListenerHead(this);
        mListView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                if (arg1.getAction() == MotionEvent.ACTION_DOWN) {
                    if (chat_face_container.getVisibility() == View.VISIBLE) {
                        chat_face_container.setVisibility(View.GONE);
                    }
                    if (chat_add_container.getVisibility() == View.VISIBLE) {
                        chat_add_container.setVisibility(View.GONE);
                    }
                    hideSoftInputView();
                }
                return false;
            }
        });
    }

    public void initAdd() {
        tv_pic = (TextView) findViewById(R.id.tv_pic);
        tv_camera = (TextView) findViewById(R.id.tv_camera);
        tv_loc = (TextView) findViewById(R.id.tv_loc);

        tv_pic.setOnClickListener(this);
        tv_camera.setOnClickListener(this);
        tv_loc.setOnClickListener(this);

    }

    public void initData() {
        offset = 0;
        listMsg = msgDao.queryMsg(YOU, I, offset);
        offset = listMsg.size();
        mLvAdapter = new ChatAdapter(this, listMsg);
        mListView.setAdapter(mLvAdapter);
        mListView.setSelection(listMsg.size());
    }

    /**
     * 初始化表情
     */
    private void initViewPager() {
        int pagesize = ExpressionUtil.getPagerCount(staticFacesList.size(), columns, rows);
        // 获取页数
        for (int i = 0; i < pagesize; i++) {
            views.add(ExpressionUtil.viewPagerItem(this, i, staticFacesList, columns, rows, input));
            LayoutParams params = new LayoutParams(16, 16);
            mDotsLayout.addView(dotsItem(i), params);
        }
        FaceVPAdapter mVpAdapter = new FaceVPAdapter(views);
        mViewPager.setAdapter(mVpAdapter);
        mDotsLayout.getChildAt(0).setSelected(true);
    }

    /**
     * 表情页切换时，底部小圆点
     *
     * @param position
     * @return
     */
    private ImageView dotsItem(int position) {
        View layout = inflater.inflate(R.layout.dot_image, null);
        ImageView iv = (ImageView) layout.findViewById(R.id.face_dot);
        iv.setId(position);
        return iv;
    }

    /**
     */
    @Override
    protected void onStart() {
        super.onStart();
    }


    @Override
    public void onClick(View arg0) {
        switch (arg0.getId()) {
            case R.id.send_sms:
                String content = input.getText().toString();
                if (TextUtils.isEmpty(content)) {
                    return;
                }
                sendMsgText(content);
                break;
            case R.id.input_sms:
                if (chat_face_container.getVisibility() == View.VISIBLE) {
                    chat_face_container.setVisibility(View.GONE);
                }
                if (chat_add_container.getVisibility() == View.VISIBLE) {
                    chat_add_container.setVisibility(View.GONE);
                }
                break;
            case R.id.image_face:
                hideSoftInputView();//隐藏软键盘
                if (chat_add_container.getVisibility() == View.VISIBLE) {
                    chat_add_container.setVisibility(View.GONE);
                }
                if (chat_face_container.getVisibility() == View.GONE) {
                    chat_face_container.setVisibility(View.VISIBLE);
                } else {
                    chat_face_container.setVisibility(View.GONE);
                }
                break;
            case R.id.image_add:
                hideSoftInputView();//隐藏软键盘
                if (chat_face_container.getVisibility() == View.VISIBLE) {
                    chat_face_container.setVisibility(View.GONE);
                }
                if (chat_add_container.getVisibility() == View.GONE) {
                    chat_add_container.setVisibility(View.VISIBLE);
                } else {
                    chat_add_container.setVisibility(View.GONE);
                }
                break;
            case R.id.tv_pic://模拟一张图片路径
                sendMsgImg("http://my.csdn.net/uploads/avatar/3/B/9/1_baiyuliang2013.jpg");
                break;
            case R.id.tv_camera://拍照，换个美女图片吧
                sendMsgImg("http://b.hiphotos.baidu.com/image/pic/item/55e736d12f2eb93872b0d889d6628535e4dd6fe8.jpg");
                break;
            case R.id.tv_loc://位置，正常情况下是需要定位的，可以用百度或者高德地图，现设置为北京坐标
                sendMsgLocation("116.404,39.915");
                break;

            case R.id.btn_chat_voice:
                input.setText(null);// 清空显示内容
                mIatResults.clear();
                // 设置参数
                setParam();
                boolean isShowDialog = mSharedPreferences.getBoolean(
                        getString(R.string.pref_key_iat_show), true);
                if (isShowDialog) {
                    // 显示听写对话框
                    mIatDialog.setListener(recognizerDialogListener);
                    mIatDialog.show();
                    showTip("请开始说话...");
                } else {
                    // 不显示听写对话框
                    ret = mIat.startListening(recognizerListener);
                    if (ret != ErrorCode.SUCCESS) {
                        showTip("听写失败,错误码：" + ret);
                    } else {
                        showTip("请开始说话...");
                    }
                }
                break;
        }

    }

    //setParam
    public void setParam() {
        // 清空参数
        mIat.setParameter(SpeechConstant.PARAMS, null);

        // 设置听写引擎
        mIat.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);
        // 设置返回结果格式
        mIat.setParameter(SpeechConstant.RESULT_TYPE, "json");

        String lag = mSharedPreferences.getString("iat_language_preference",
                "mandarin"); //没有的话默认返回mandarin
        if (lag.equals("en_us")) {
            // 设置语言
            mIat.setParameter(SpeechConstant.LANGUAGE, "en_us");
        } else {
            // 设置语言
            mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
            // 设置语言区域
            mIat.setParameter(SpeechConstant.ACCENT, lag);
        }

        // 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
        mIat.setParameter(SpeechConstant.VAD_BOS, mSharedPreferences.getString("iat_vadbos_preference", "4000"));

        // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
        mIat.setParameter(SpeechConstant.VAD_EOS, mSharedPreferences.getString("iat_vadeos_preference", "1000"));

        // 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
        mIat.setParameter(SpeechConstant.ASR_PTT, mSharedPreferences.getString("iat_punc_preference", "1"));

        // 设置音频保存路径，保存音频格式仅为pcm，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        mIat.setParameter(SpeechConstant.ASR_AUDIO_PATH, Environment.getExternalStorageDirectory() + "/vchat/wavaudio.pcm");

        // 设置听写结果是否结果动态修正，为“1”则在听写过程中动态递增地返回结果，否则只在听写结束之后返回最终结果
        // 注：该参数暂时只对在线听写有效
        mIat.setParameter(SpeechConstant.ASR_DWA, mSharedPreferences.getString("iat_dwa_preference", "0"));
    }

    /**
     * 初始化监听器。
     */
    private InitListener mInitListener = new InitListener() {

        @Override
        public void onInit(int code) {
            Log.d("test", "SpeechRecognizer init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
                showTip("初始化失败，错误码：" + code);
            }
        }
    };

    private void showTip(final String str) {
        Toast.makeText(this, str, Toast.LENGTH_LONG).show();
    }

    /**
     * 听写UI监听器
     */
    private RecognizerDialogListener recognizerDialogListener = new RecognizerDialogListener() {
        public void onResult(RecognizerResult results, boolean isLast) {
            printResult(results);
        }

        /**
         * 识别回调错误.
         */
        public void onError(SpeechError error) {
            showTip(error.getPlainDescription(true) + "   gzy");
        }

    };

    private void printResult(RecognizerResult results) {
        String text = JsonParser.parseIatResult(results.getResultString());

        String sn = null;
        // 读取json结果中的sn字段
        try {
            JSONObject resultJson = new JSONObject(results.getResultString());
            sn = resultJson.optString("sn");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mIatResults.put(sn, text);

        StringBuffer resultBuffer = new StringBuffer();
        for (String key : mIatResults.keySet()) {
            resultBuffer.append(mIatResults.get(key));
        }

        input.setText(resultBuffer.toString());
        input.setSelection(input.length());
    }

    /**
     * 听写监听器。
     */
    private MyRecognizerListener recognizerListener = new MyRecognizerListener() {

        @Override
        public void onBeginOfSpeech() {
            showTip("开始说话");
        }

        @Override
        public void onError(SpeechError error) {
            // Tips：
            // 错误码：10118(您没有说话)，可能是录音机权限被禁，需要提示用户打开应用的录音权限。
            // 如果使用本地功能（语音+）需要提示用户开启语音+的录音权限。
            showTip(error.getPlainDescription(true));
        }

        @Override
        public void onEndOfSpeech() {
            showTip("结束说话");
        }

        @Override
        public void onResult(RecognizerResult results, boolean isLast) {
            Log.d("test  ", results.getResultString());
            printResult(results);

            if (isLast) {
                // TODO 最后的结果
            }
        }

        @Override
        public void onVolumeChanged(int i, byte[] bytes) {
            //	showTip("当前正在说话，音量大小：" + volume);
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
        }
    };


    /**
     * 执行发送消息 图片类型
     *
     * @param
     */
    void sendMsgImg(String imgpath) {
        Msg msg = getChatInfoTo(imgpath, Const.MSG_TYPE_IMG);
        msg.setMsgId(msgDao.insert(msg));
        listMsg.add(msg);
        offset = listMsg.size();
        mLvAdapter.notifyDataSetChanged();
        final String message = YOU + Const.SPLIT + I + Const.SPLIT + Const.MSG_TYPE_IMG + Const.SPLIT + imgpath + Const.SPLIT + sd.format(new Date());
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    XmppUtil.sendMessage(WeChatApplication.xmppConnection, message, YOU);
                } catch (XMPPException e) {
                    e.printStackTrace();
                    Looper.prepare();
                    ToastUtil.showShortToast(ChatActivity.this, "发送失败");
                    Looper.loop();
                }
            }
        }).start();
        updateSession(Const.MSG_TYPE_TEXT, "[图片]");
    }

    /**
     * 执行发送消息 文本类型
     *
     * @param content
     */
    void sendMsgText(String content) {
        Msg msg = getChatInfoTo(content, Const.MSG_TYPE_TEXT);
        msg.setMsgId(msgDao.insert(msg));
        listMsg.add(msg);
        offset = listMsg.size();
        mLvAdapter.notifyDataSetChanged();
        input.setText("");
        final String message = YOU + Const.SPLIT + I + Const.SPLIT + Const.MSG_TYPE_TEXT + Const.SPLIT + content + Const.SPLIT + sd.format(new Date());
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    XmppUtil.sendMessage(WeChatApplication.xmppConnection, message, YOU);
                } catch (XMPPException e) {
                    e.printStackTrace();
                    Looper.prepare();
                    ToastUtil.showShortToast(ChatActivity.this, "发送失败");
                    Looper.loop();
                }
            }
        }).start();
        updateSession(Const.MSG_TYPE_TEXT, content);
    }

    /**
     * 执行发送消息 文本类型
     *
     * @param content
     */
    void sendMsgLocation(String content) {
        Msg msg = getChatInfoTo(content, Const.MSG_TYPE_LOCATION);
        msg.setMsgId(msgDao.insert(msg));
        listMsg.add(msg);
        offset = listMsg.size();
        mLvAdapter.notifyDataSetChanged();
        final String message = YOU + Const.SPLIT + I + Const.SPLIT + Const.MSG_TYPE_LOCATION + Const.SPLIT + content + Const.SPLIT + sd.format(new Date());
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    XmppUtil.sendMessage(WeChatApplication.xmppConnection, message, YOU);
                } catch (XMPPException e) {
                    e.printStackTrace();
                    Looper.prepare();
                    ToastUtil.showShortToast(ChatActivity.this, "发送失败");
                    Looper.loop();
                }
            }
        }).start();
        updateSession(Const.MSG_TYPE_TEXT, "[位置]");
    }

    /**
     * 发送的信息
     * from为收到的消息，to为自己发送的消息
     *
     * @param message => 接收者卍发送者卍消息类型卍消息内容卍发送时间
     * @return
     */
    private Msg getChatInfoTo(String message, String msgtype) {
        String time = sd.format(new Date());
        Msg msg = new Msg();
        msg.setFromUser(YOU);  //发送者
        msg.setToUser(I);        //接受者
        msg.setType(msgtype);
        msg.setIsComing(1);
        msg.setContent(message);
        msg.setDate(time);
        return msg;
    }

    void updateSession(String type, String content) {
        Session session = new Session();
        session.setFrom(YOU);
        session.setTo(I);
        session.setNotReadCount("");//未读消息数量
        session.setContent(content);
        session.setTime(sd.format(new Date()));
        session.setType(type);
        if (sessionDao.isContent(YOU, I)) {
            sessionDao.updateSession(session);
        } else {
            sessionDao.insertSession(session);
        }
        Intent intent = new Intent(Const.ACTION_ADDFRIEND);//发送广播，通知消息界面更新
        sendBroadcast(intent);
    }


    /**
     * 表情页改变时，dots效果也要跟着改变
     */
    class PageChange implements OnPageChangeListener {
        @Override
        public void onPageScrollStateChanged(int arg0) {
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        @Override
        public void onPageSelected(int arg0) {
            for (int i = 0; i < mDotsLayout.getChildCount(); i++) {
                mDotsLayout.getChildAt(i).setSelected(false);
            }
            mDotsLayout.getChildAt(arg0).setSelected(true);
        }
    }

    /**
     * 下拉加载更多
     */
    @Override
    public void onRefresh() {
        List<Msg> list = msgDao.queryMsg(YOU, I, offset);
        if (list.size() <= 0) {
            mListView.setSelection(0);
            mListView.onRefreshCompleteHeader();
            return;
        }
        listMsg.addAll(0, list);
        offset = listMsg.size();
        mListView.onRefreshCompleteHeader();
        mLvAdapter.notifyDataSetChanged();
        mListView.setSelection(list.size());
    }

    /**
     * 弹出输入法窗口
     */
    private void showSoftInputView(final View v) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                ((InputMethodManager) v.getContext().getSystemService(Service.INPUT_METHOD_SERVICE)).toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }, 0);
    }

    /**
     * 隐藏软键盘
     */
    public void hideSoftInputView() {
        InputMethodManager manager = ((InputMethodManager) this.getSystemService(Activity.INPUT_METHOD_SERVICE));
        if (getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
            if (getCurrentFocus() != null)
                manager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    /**
     * 接收消息记录操作广播：删除复制
     */
    private class MsgOperReciver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int type = intent.getIntExtra("type", 0);
            final int position = intent.getIntExtra("position", 0);
            if (listMsg.size() <= 0) {
                return;
            }
            final Msg msg = listMsg.get(position);
            switch (type) {
                case 1://聊天记录操作
                    Builder bd = new Builder(ChatActivity.this);
                    String[] items = null;
                    if (msg.getType().equals(Const.MSG_TYPE_TEXT)) {
                        items = new String[]{"删除记录", "删除全部记录", "复制文字", "语音"};
                    } else {
                        items = new String[]{"删除记录", "删除全部记录"};
                    }
                    bd.setItems(items, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            switch (arg1) {
                                case 0://删除
                                    listMsg.remove(position);
                                    offset = listMsg.size();
                                    mLvAdapter.notifyDataSetChanged();
                                    msgDao.deleteMsgById(msg.getMsgId());
                                    break;
                                case 1://删除全部
                                    listMsg.removeAll(listMsg);
                                    offset = listMsg.size();
                                    mLvAdapter.notifyDataSetChanged();
                                    msgDao.deleteAllMsg(YOU, I);
                                    break;
                                case 2://复制
                                    ClipboardManager cmb = (ClipboardManager) ChatActivity.this.getSystemService(ChatActivity.CLIPBOARD_SERVICE);
                                    cmb.setText(msg.getContent());
                                    Toast.makeText(getApplicationContext(), "已复制到剪切板", Toast.LENGTH_SHORT).show();
                                    break;

                                case 3:
                                    showTip(msg.getContent());
                                    String text = msg.getContent();
                                    // 设置参数
                                    setTtsParam();
                                    int code = mTts.startSpeaking(text, mTtsListener);
                                    if (code != ErrorCode.SUCCESS) {
                                        if (code == ErrorCode.ERROR_COMPONENT_NOT_INSTALLED) {
                                            //未安装则跳转到提示安装页面
                                            //mInstaller.install();
                                            showTip("安装");

                                        } else {
                                            showTip("语音合成失败,错误码: " + code);
                                        }
                                    }
                                    break;

                            }


                        }
                    });
                    bd.show();
                    break;
            }

        }
    }

    private void setTtsParam(){
        // 清空参数
        mTts.setParameter(SpeechConstant.PARAMS, null);
        // 根据合成引擎设置相应参数
        if(mEngineType.equals(SpeechConstant.TYPE_CLOUD)) {
            mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
            // 设置在线合成发音人
            mTts.setParameter(SpeechConstant.VOICE_NAME,voicer);
        }else {
            mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_LOCAL);
            // 设置本地合成发音人 voicer为空，默认通过语音+界面指定发音人。
            mTts.setParameter(SpeechConstant.VOICE_NAME,"");
        }
        //设置合成语速
        mTts.setParameter(SpeechConstant.SPEED,mSharedPreferences.getString("speed_preference", "50"));
        //设置合成音调
        mTts.setParameter(SpeechConstant.PITCH,mSharedPreferences.getString("pitch_preference", "50"));
        //设置合成音量
        mTts.setParameter(SpeechConstant.VOLUME,mSharedPreferences.getString("volume_preference", "50"));
        //设置播放器音频流类型
        mTts.setParameter(SpeechConstant.STREAM_TYPE,mSharedPreferences.getString("stream_preference", "3"));

        // 设置播放合成音频打断音乐播放，默认为true
        mTts.setParameter(SpeechConstant.KEY_REQUEST_FOCUS, "true");

        // 设置合成音频保存路径，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        mTts.setParameter(SpeechConstant.PARAMS,"tts_audio_path="+Environment.getExternalStorageDirectory()+"/test.pcm");
    }

    /**
     * 初始化监听。
     */
    private InitListener mTtsInitListener = new InitListener() {
        @Override
        public void onInit(int code) {
            Log.d("jj", "InitListener init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
                showTip("初始化失败,错误码："+code);
            } else {
                // 初始化成功，之后可以调用startSpeaking方法
                // 注：有的开发者在onCreate方法中创建完合成对象之后马上就调用startSpeaking进行合成，
                // 正确的做法是将onCreate中的startSpeaking调用移至这里
            }
        }
    };

    private SynthesizerListener mTtsListener = new SynthesizerListener() {
        @Override
        public void onSpeakBegin() {
            showTip("开始播放");
        }

        @Override
        public void onSpeakPaused() {
            showTip("暂停播放");
        }

        @Override
        public void onSpeakResumed() {
            showTip("继续播放");
        }

        @Override
        public void onBufferProgress(int percent, int beginPos, int endPos,
                                     String info) {
            // 合成进度
            mPercentForBuffering = percent;
        }

        @Override
        public void onSpeakProgress(int percent, int beginPos, int endPos) {
            // 播放进度
            mPercentForPlaying = percent;
        }

        @Override
        public void onCompleted(SpeechError error) {
            if (error == null) {
                showTip("播放完成");
            } else if (error != null) {
                showTip(error.getPlainDescription(true));
            }
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {

        }
    };

    /**
     * 接收消息记录操作广播：删除复制
     */
    private class NewMsgReciver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle b = intent.getBundleExtra("msg");
            Msg msg = (Msg) b.getSerializable("msg");
            listMsg.add(msg);
            offset = listMsg.size();
            mLvAdapter.notifyDataSetChanged();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(msgOperReciver);
        unregisterReceiver(newMsgReciver);
    }

    @Override
    protected void onResume() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //让输入框获取焦点
                input.requestFocus();
            }
        }, 100);
        super.onResume();
    }

    ;

    /**
     * 监听返回键
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            hideSoftInputView();
            if (chat_face_container.getVisibility() == View.VISIBLE) {
                chat_face_container.setVisibility(View.GONE);
            } else if (chat_add_container.getVisibility() == View.VISIBLE) {
                chat_add_container.setVisibility(View.GONE);
            } else {
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}


class MyRecognizerListener implements RecognizerListener {

    @Override
    public void onVolumeChanged(int i, byte[] bytes) {

    }

    @Override
    public void onBeginOfSpeech() {

    }

    @Override
    public void onEndOfSpeech() {

    }

    @Override
    public void onResult(RecognizerResult recognizerResult, boolean b) {

    }

    @Override
    public void onError(SpeechError speechError) {

    }

    @Override
    public void onEvent(int i, int i1, int i2, Bundle bundle) {

    }
}