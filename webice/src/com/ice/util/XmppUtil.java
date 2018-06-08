package com.ice.util;

import java.util.Collection;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.util.StringUtils;

import com.ice.listener.TaxiChatManagerListener;

public class XmppUtil {

	/**
	 * 注册
	 * 
	 * @param account
	 *            注册帐号
	 * @param password
	 *            注册密码
	 * @return 1、注册成功 0、服务器没有返回结果2、这个账号已经存在3、注册失败
	 */
	public static boolean register(XMPPConnection mXMPPConnection, String username, String password) {
		try {
			mXMPPConnection.connect();
			mXMPPConnection.getAccountManager().createAccount(username, password);
			return true;
		} catch (XMPPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}

	public static boolean login(XMPPConnection connection, String username, String password) {
		try {
			connection.connect();
			connection.login(username, password);
			if (connection.isAuthenticated()) {
				XmppUtil.setPresence(connection, Const.ONLINE);
				XmppConnectionManager.connection = connection;
				TaxiChatManagerListener chatManagerListener = new TaxiChatManagerListener();
				connection.getChatManager().addChatListener(chatManagerListener);
				return true;
			} else {
				return false;
			}
		} catch (XMPPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}

	public static void setPresence(XMPPConnection con, int code) {
		if (con == null)
			return;
		Presence presence = null;
		switch (code) {
		case 0:
			presence = new Presence(Presence.Type.available); // 在线
			break;
		case 1:
			presence = new Presence(Presence.Type.available); // 设置Q我吧
			presence.setMode(Presence.Mode.chat);
			break;
		case 2: // 隐身
			Roster roster = con.getRoster();
			Collection<RosterEntry> entries = roster.getEntries();
			for (RosterEntry entry : entries) {
				presence = new Presence(Presence.Type.unavailable);
				presence.setPacketID(Packet.ID_NOT_AVAILABLE);
				presence.setFrom(con.getUser());
				presence.setTo(entry.getUser());
			}
			// 向同一用户的其他客户端发送隐身状态
			presence = new Presence(Presence.Type.unavailable);
			presence.setPacketID(Packet.ID_NOT_AVAILABLE);
			presence.setFrom(con.getUser());
			presence.setTo(StringUtils.parseBareAddress(con.getUser()));
			break;
		case 3:
			presence = new Presence(Presence.Type.available); // 设置忙碌
			presence.setMode(Presence.Mode.dnd);
			break;
		case 4:
			presence = new Presence(Presence.Type.available); // 设置离开
			presence.setMode(Presence.Mode.away);
			break;
		case 5:
			presence = new Presence(Presence.Type.unavailable); // 离线
			break;
		default:
			break;
		}
		if (presence != null) {
			con.sendPacket(presence);
		}
	}

	/**
	 * 发送消息
	 * 
	 * @param content
	 * @param touser
	 * @throws XMPPException
	 */
	public static void sendMessage(XMPPConnection mXMPPConnection, String content, String touser) throws XMPPException {

		String xmpp_domain = mXMPPConnection.getServiceName();
		if (mXMPPConnection == null || !mXMPPConnection.isConnected()) {
			throw new XMPPException();
		}
		ChatManager chatmanager = mXMPPConnection.getChatManager();
		Chat chat = chatmanager.createChat(touser + "@" + xmpp_domain, null);
		if (chat != null) {
			chat.sendMessage(content);
		}
	}
	
	/** 
     * 创建一个组 
     */ 
	public static boolean addGroup(Roster roster,String groupName)  
    {  
        try {  
            roster.createGroup(groupName);  
            return true;  
        } catch (Exception e) {  
            e.printStackTrace();  
            System.out.println("创建分组异常："+e.getMessage());
            return false;  
        }  
    }  
	
	/**
	 * 添加一个好友  无分组
	 */
	public static boolean addUser(Roster roster,String userName,String name)
	{
		try {
			roster.createEntry(userName, name, null);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
	}
	
	/**
	 * 添加一个好友到分组
	 * @param roster
	 * @param userName
	 * @param name
	 * @return
	 */
	public static boolean addUsers(Roster roster,String userName,String name,String groupName)
	{
		try {
			roster.createEntry(userName, name,new String[]{ groupName});
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("添加好友异常："+e.getMessage());
			return false;
		}
	}

}
