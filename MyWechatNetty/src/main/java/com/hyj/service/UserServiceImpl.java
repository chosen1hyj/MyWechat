package com.hyj.service;

import com.hyj.enums.MsgActionEnum;
import com.hyj.enums.MsgSignFlagEnum;
import com.hyj.enums.SearchFriendsStatusEnum;
import com.hyj.mapper.*;
import com.hyj.netty.websocket.ChatMsg;
import com.hyj.netty.websocket.DataContent;
import com.hyj.netty.websocket.UserChannelRel;
import com.hyj.pojo.FriendsRequest;
import com.hyj.pojo.MyFriends;
import com.hyj.pojo.Users;
import com.hyj.pojo.vo.FriendRequestVO;
import com.hyj.pojo.vo.MyFriendsVO;
import com.hyj.utils.FastDFSClient;
import com.hyj.utils.FileUtils;
import com.hyj.utils.JsonUtils;
import com.hyj.utils.QRCodeUtils;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.n3r.idworker.Sid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tk.mybatis.mapper.entity.Example;

import javax.xml.crypto.Data;
import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * @description:
 * @author: Chosen1
 * @date: 2020/04/2 20:57
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UsersMapper usersMapper;

    @Autowired
    private MyFriendsMapper myFriendsMapper;

    @Autowired
    private FriendsRequestMapper friendsRequestMapper;

    @Autowired
    private UsersMapperCustom usersMapperCustom;

    @Autowired
    private Sid sid;

    @Autowired
    private QRCodeUtils qrCodeUtils;

    @Autowired
    private FastDFSClient fastDFSClient;

    @Autowired
    private ChatMsgMapper chatMsgMapper;

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public boolean queryUsernameIsExist(String username) {

        Users user = new Users();
        user.setUsername(username);
        Users result = usersMapper.selectOne(user);
        return result != null;
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public Users queryUserForLogin(String username, String pwd) {

        Example userExample = new Example(Users.class);
        Example.Criteria criteria = userExample.createCriteria();
        criteria.andEqualTo("username", username);
        criteria.andEqualTo("password", pwd);
        Users result = usersMapper.selectOneByExample(userExample);
        return result;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public Users saveUser(Users user) {

        String userId = sid.nextShort();
        //为每个用户生成唯一的二维码
        String qrCodePath = "d://" + userId + "qrcode.png";

        //wechat_qrcode:[username]
        qrCodeUtils.createQRCode(qrCodePath, "wechat_qrcode:" + user.getUsername());
        MultipartFile qrcodeFile = FileUtils.fileToMultipart(qrCodePath);
        String qrcodeUrl = "";
        try {
            qrcodeUrl = fastDFSClient.uploadQRCode(qrcodeFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        user.setQrcode(qrcodeUrl);
        user.setId(userId);
        usersMapper.insert(user);
        return user;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public Users updateUserInfo(Users user) {
        usersMapper.updateByPrimaryKeySelective(user);
        return queryUserById(user.getId());
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public Integer preconditionSearchFriend(String myUserId, String friendUsername) {

        //前置条件， 1.搜索的用户如果不存在，返回[无此用户]
        Users user = queryUserInfoByUsername(friendUsername);
        if(user == null)
            return SearchFriendsStatusEnum.USER_NOT_EXIST.status;

        //前置条件， 2.搜索的用户是自己，返回[不能添加自己]

        if(user.getId().equals(myUserId)){
            return SearchFriendsStatusEnum.NOT_YOURSELF.status;
        }

        //前置条件， 1.搜索的用户如果已经是好友，返回[该用户已经是你的好友]
        Example mfe = new Example(MyFriends.class);
        Example.Criteria mfc = mfe.createCriteria();
        mfc.andEqualTo("myUserId", myUserId);
        mfc.andEqualTo("myFriendUserId", user.getId());
        MyFriends myFriendsRel = myFriendsMapper.selectOneByExample(mfe);
        if(myFriendsRel != null){
            return SearchFriendsStatusEnum.ALREADY_FRIENDS.status;
        }


        return SearchFriendsStatusEnum.SUCCESS.status;
    }

    private Users queryUserById(String userId){

        return usersMapper.selectByPrimaryKey(userId);

    }

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public Users queryUserInfoByUsername(String username){

        Example ue = new Example(Users.class);
        Example.Criteria uc = ue.createCriteria();
        uc.andEqualTo("username", username);
        return usersMapper.selectOneByExample(ue);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void sendFriendRequest(String myUserId, String friendUsername) {

        //根据用户名查询朋友信息
        Users friend = queryUserInfoByUsername(friendUsername);
        //1.查询发送好友请求记录表
        Example fre = new Example(FriendsRequest.class);
        Example.Criteria frc = fre.createCriteria();
        frc.andEqualTo("sendUserId", myUserId);
        frc.andEqualTo("acceptUserId", friend.getId());
        FriendsRequest friendRequest = friendsRequestMapper.selectOneByExample(fre);
        if(friendRequest == null){
            //如果不是好友，并且好友记录没有添加，则新增好友请求记录
            String requestId = sid.nextShort();
            FriendsRequest request = new FriendsRequest();
            request.setId(requestId);
            request.setSendUserId(myUserId);
            request.setAcceptUserId(friend.getId());
            request.setRequestDateTime(new Date());
            friendsRequestMapper.insert(request);
        }
    }

    /**
     * 查询好友请求
     * @param acceptUserId
     * @return
     */
    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public List<FriendRequestVO> queryFriendRequestList(String acceptUserId) {

        return usersMapperCustom.queryFriendRequestList(acceptUserId);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void deleteFriendRequest(String sendUserId, String acceptUserId) {

        Example fre = new Example(FriendsRequest.class);
        Example.Criteria frc = fre.createCriteria();
        frc.andEqualTo("sendUserId", sendUserId);
        frc.andEqualTo("acceptUserId", acceptUserId);
        friendsRequestMapper.deleteByExample(fre);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void passFriendRequest(String sendUserId, String acceptUserId) {

        saveFriends(sendUserId, acceptUserId);
        saveFriends(acceptUserId, sendUserId);
        deleteFriendRequest(sendUserId, acceptUserId);
        
        //使用websocket主动推送消息到请求发起者，更新他的通讯录喂最新
        DataContent dataContent = new DataContent();
        dataContent.setAction(MsgActionEnum.PULL_FRIEND.type);
        Channel sendChannel = UserChannelRel.get(sendUserId);
        if(sendChannel != null){
            sendChannel.writeAndFlush(new TextWebSocketFrame(JsonUtils.objectToJson(dataContent)));
        }

    }


    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public List<MyFriendsVO> queryMyFriends(String userId) {

        return usersMapperCustom.queryMyFriends(userId);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public String saveMsg(ChatMsg chatMsg) {

        com.hyj.pojo.ChatMsg msgDB = new com.hyj.pojo.ChatMsg();
        String msgId = sid.nextShort();
        msgDB.setId(msgId);
        msgDB.setAcceptUserId(chatMsg.getReceiverId());
        msgDB.setSendUserId(chatMsg.getSenderId());
        msgDB.setCreateTime(new Date());
        msgDB.setSignFlag(MsgSignFlagEnum.unsign.type);
        msgDB.setMsg(chatMsg.getMsg());
        chatMsgMapper.insert(msgDB);
        return msgId;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void updateMsgSigned(List<String> msgIdList) {
        usersMapperCustom.batchUpdateMsgSigned(msgIdList);
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public List<com.hyj.pojo.ChatMsg> getUnReadMsgList(String acceptUserId) {

        Example chatExample = new Example(com.hyj.pojo.ChatMsg.class);
        Example.Criteria chatCriteria = chatExample.createCriteria();
        chatCriteria.andEqualTo("signFlag", 0);
        chatCriteria.andEqualTo("acceptUserId", acceptUserId);
        List<com.hyj.pojo.ChatMsg> result = chatMsgMapper.selectByExample(chatExample);
        return result;
    }

    private void saveFriends(String sendUserId, String acceptUserId){

        MyFriends myFriends = new MyFriends();
        String recordId = sid.nextShort();
        myFriends.setId(recordId);
        myFriends.setMyFriendUserId(acceptUserId);
        myFriends.setMyUserId(sendUserId);
        myFriendsMapper.insert(myFriends);
    }


}
