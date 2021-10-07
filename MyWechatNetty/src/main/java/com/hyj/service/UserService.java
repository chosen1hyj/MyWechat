package com.hyj.service;

import com.hyj.netty.websocket.ChatMsg;
import com.hyj.pojo.Users;
import com.hyj.pojo.vo.FriendRequestVO;
import com.hyj.pojo.vo.MyFriendsVO;
import com.hyj.pojo.vo.UsersVO;

import java.util.List;

/**
 * @description:
 * @author: Chosen1
 * @date: 2020/04/2 20:56
 */
public interface UserService {

    /**
     * 判断用户名是否存在
     * @param username
     * @return
     */
    boolean queryUsernameIsExist(String username);

    Users queryUserForLogin(String username, String pwd);

    //用户注册
    Users saveUser(Users user);

    Users updateUserInfo(Users user);

    /**
     * 搜索朋友的前置条件
     * @param myUserId
     * @param friendUsername
     * @return
     */
    Integer preconditionSearchFriend(String myUserId, String friendUsername);

    /**
     * 根据用户名查询用户对象
     * @param username
     * @return
     */
    Users queryUserInfoByUsername(String username);

    /**
     * 添加好友请求记录，保存到数据库
     * @param myUserId
     * @param friendUsername
     */
    void sendFriendRequest(String myUserId, String friendUsername);

    List<FriendRequestVO> queryFriendRequestList(String acceptUserId);


    /**
     * 删除好友请求记录
     * @param sendUserId
     * @param acceptUserId
     */
    void deleteFriendRequest(String sendUserId, String acceptUserId);

    /**
     * 通过好友请求
     * 1:保存好友
     * 2:逆向保存好友
     * @param sendUserId
     * @param acceptUserId
     */
    void passFriendRequest(String sendUserId, String acceptUserId);

    List<MyFriendsVO> queryMyFriends(String userId);

    /**
     * 保存聊天消息到数据库
     * @param chatMsg
     * @return
     */
    String saveMsg(ChatMsg chatMsg);

    /**
     * 批量签收消息
     * @param msgIdList
     */
    void updateMsgSigned(List<String> msgIdList);

    /**
     * 获取未签收消息列表
     * @param acceptUserId
     * @return
     */
    List<com.hyj.pojo.ChatMsg> getUnReadMsgList(String acceptUserId);
}
