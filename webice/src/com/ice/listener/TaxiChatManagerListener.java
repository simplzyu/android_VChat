package com.ice.listener;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.jivesoftware.smack.Chat;  
import org.jivesoftware.smack.ChatManagerListener;  
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;  
import org.jivesoftware.smack.util.StringUtils;  
import org.json.JSONException;  
import org.json.JSONObject;

import com.ice.bean.Session;
import com.ice.util.APIReceiver;
import com.ice.util.Const;
import com.ice.util.MapUtil;
import com.ice.util.XmppConnectionManager;
import com.ice.util.XmppUtil;  
  
  
/** 
 * 单人聊天信息监听类 
 *  
 * @author Administrator 
 *  
 */  
public class TaxiChatManagerListener implements ChatManagerListener {  
  
    public void chatCreated(Chat chat, boolean arg1) {  
        chat.addMessageListener(new MessageListener() {  
            public void processMessage(Chat arg0, Message msg) {  
            	try {
        			String msgBody = msg.getBody();
        			if (msgBody==null || msgBody.equals(""))
        				return;
        			//接收者卍发送者卍消息类型卍消息内容卍发送时间
        			String[] msgs=msgBody.split(Const.SPLIT);
        			String to=msgs[0];//接收者,当然是自己
        			String from=msgs[1];//发送者，谁给你发的消息
        			String msgtype=msgs[2];//消息类型
        			String msgcontent=msgs[3];//消息内容
        			String msgtime=msgs[4];//消息时间
        			
        			final Session session=new Session();
        			session.setFrom(from);
        			session.setTo(to);
        			session.setNotReadCount("");//未读消息数量
        			session.setTime(msgtime);
        			
        			System.out.println(msgBody);
        			if(msgtype.equals(Const.MSG_TYPE_ADD_FRIEND)){//添加好友的请求
        				Roster roster= XmppConnectionManager.connection.getRoster();
						XmppUtil.addGroup(roster, "我的好友");//先默认创建一个分组
						if(XmppUtil.addUsers(roster, session.getFrom()+"@"+ XmppConnectionManager.connection.getServiceName(), session.getFrom(),"我的好友")){
							//告知对方，同意添加其为好友
							new Thread(new Runnable() {

								public void run() {
									try {
										//注意消息的协议格式 =》接收者卍发送者卍消息类型卍消息内容卍发送时间
										String message= session.getFrom()+Const.SPLIT+MapUtil.get("name")+Const.SPLIT+Const.MSG_TYPE_ADD_FRIEND_SUCCESS+Const.SPLIT+""+Const.SPLIT+new SimpleDateFormat("MM-dd HH:mm").format(new Date());
										XmppUtil.sendMessage(XmppConnectionManager.connection, message, session.getFrom());
									} catch (XMPPException e) {
										e.printStackTrace();		
									}
								}
									
							}).start();
//							sessionDao.updateSessionToDisPose(session.getId());//将本条数据在数据库中改为已处理
////							ToastUtil.showShortToast(mContext, "你们已经是好友了，快去聊天吧！");
//							sessionList.remove(session);
//							session.setIsdispose("1");
//							sessionList.add(0,session);
//							adapter.notifyDataSetChanged();
//							//发送广播更新好友列表
//							 Intent intent=new Intent(Const.ACTION_FRIENDS_ONLINE_STATUS_CHANGE);
//				        	 mContext.sendBroadcast(intent);
						}else{
							System.out.println("添加好友失败");
						}
        			}else	if(msgtype.equals(Const.MSG_TYPE_ADD_FRIEND_SUCCESS)){//对方同意添加好友的请求
        				
        			}else if(msgtype.equals(Const.MSG_TYPE_TEXT)){//文本类型
        				System.out.println(msgcontent);
        				String content = APIReceiver.getResult(msgcontent);
        				String message= session.getFrom()+Const.SPLIT+MapUtil.get("name")+Const.SPLIT+Const.MSG_TYPE_TEXT+Const.SPLIT+ content +Const.SPLIT+new SimpleDateFormat("MM-dd HH:mm").format(new Date());
						XmppUtil.sendMessage(XmppConnectionManager.connection, message, session.getFrom());
        			}else if(msgtype.equals(Const.MSG_TYPE_IMG)){
        				
        			}else if(msgtype.equals(Const.MSG_TYPE_LOCATION)){//位置
        				
        			}
        
        			
        		} catch (Exception e) {
        			e.printStackTrace();
        		}

            }  
        });  
    }  
}  
