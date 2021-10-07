package com.hyj.controller;

import com.hyj.enums.OperatorFriendRequestTypeEnum;
import com.hyj.enums.SearchFriendsStatusEnum;
import com.hyj.pojo.ChatMsg;
import com.hyj.pojo.Users;
import com.hyj.pojo.bo.UsersBO;
import com.hyj.pojo.vo.MyFriendsVO;
import com.hyj.pojo.vo.UsersVO;
import com.hyj.service.UserService;
import com.hyj.utils.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @description:
 * @author: Chosen1
 * @date: 2020/04/2 18:13
 */

@RestController
@RequestMapping("u")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private FastDFSClient fastDFSClient;

    @PostMapping("/registerOrLogin")
    public WeChatJSONResult registerOrLogin(@RequestBody Users user) throws Exception {
        //0. 判断用户名密码不能为空
        if(StringUtils.isBlank(user.getUsername())
                || StringUtils.isBlank(user.getPassword())){

            return WeChatJSONResult.errorMsg("用户名或密码不能为空");
        }

        //判断用户名是否存在，如果存在就登录，不然注册
        boolean usernameIsExist = userService.queryUsernameIsExist(user.getUsername());
        Users userResult = null;
        if(usernameIsExist){
            //1.1登录
            userResult = userService.queryUserForLogin(user.getUsername(), MD5Utils.getMD5Str(user.getPassword()));
            if(userResult == null){
                return WeChatJSONResult.errorMsg("用户名或密码不正确");
            }
        }else{
            //1.2注册
            user.setNickname(user.getUsername());
            user.setFaceImage("");
            user.setFaceImageBig("");
            user.setPassword(MD5Utils.getMD5Str(user.getPassword()));
            userResult = userService.saveUser(user);
        }

        UsersVO usersVO = new UsersVO();
        BeanUtils.copyProperties(userResult, usersVO);
        return WeChatJSONResult.ok(usersVO);
    }

    @PostMapping("/uploadFaceBase64")
    public WeChatJSONResult uploadFaceBase64(@RequestBody UsersBO userBO) throws Exception {

        //获取前端传过来的base64字符串，任何转换为文件对象再上传
        String base64Data = userBO.getFaceData();
        String userFacePath = "d:\\" + userBO.getUserId() + "userface64.png";
        FileUtils.base64ToFile(userFacePath, base64Data);

        //上传文件到fastdfs
        MultipartFile faceFile = FileUtils.fileToMultipart(userFacePath);
        String url = fastDFSClient.uploadBase64(faceFile);
        System.out.println(url);

        //获取缩略图url
        String thump = "_80x80.";
        String[] arr = url.split("\\.");
        String thumpImgUrl = arr[0] + thump + arr[1];
        //更新用户头像
        Users user = new Users();
        user.setId(userBO.getUserId());
        user.setFaceImage(thumpImgUrl);
        user.setFaceImageBig(url);
        Users returnUser = userService.updateUserInfo(user);
        return WeChatJSONResult.ok(returnUser);
    }

    @PostMapping("/setNickname")
    public WeChatJSONResult setNickname(@RequestBody UsersBO userBO){

        Users user = new Users();
        user.setId(userBO.getUserId());
        user.setNickname(userBO.getNickname());

        Users result = userService.updateUserInfo(user);
        return WeChatJSONResult.ok(result);
    }

    /**
     * 搜索好友接口，根据账号做匹配查询而不是模糊查询
     * @param myUserId
     * @param friendUsername
     * @return
     */
    @PostMapping("/search")
    public WeChatJSONResult setUser(String myUserId, String friendUsername){

        //0.判断myUserId friendUsername不能为空
        if(StringUtils.isBlank(myUserId) || StringUtils.isBlank(friendUsername)){
            return WeChatJSONResult.errorMsg("");
        }

        //前置条件， 1.搜索的用户如果不存在，返回[无此用户]
        //前置条件， 2.搜索的用户是自己，返回[不能添加自己]
        //前置条件， 1.搜索的用户如果已经是好友，返回[该用户已经是你的好友]

        Integer status = userService.preconditionSearchFriend(myUserId, friendUsername);
        if(status == SearchFriendsStatusEnum.SUCCESS.status){

            Users user = userService.queryUserInfoByUsername(friendUsername);
            UsersVO userVO = new UsersVO();
            BeanUtils.copyProperties(user, userVO);
            return WeChatJSONResult.ok(userVO);
        }else {
            String errorMsg = SearchFriendsStatusEnum.getMsgByKey(status);
            return WeChatJSONResult.errorMsg(errorMsg);
        }
    }


    @PostMapping("/addFriendRequest")
    public WeChatJSONResult addUser(String myUserId, String friendUsername){

        //0.判断myUserId friendUsername不能为空
        if(StringUtils.isBlank(myUserId) || StringUtils.isBlank(friendUsername)){
            return WeChatJSONResult.errorMsg("");
        }

        //前置条件， 1.搜索的用户如果不存在，返回[无此用户]
        //前置条件， 2.搜索的用户是自己，返回[不能添加自己]
        //前置条件， 1.搜索的用户如果已经是好友，返回[该用户已经是你的好友]

        Integer status = userService.preconditionSearchFriend(myUserId, friendUsername);
        if(status == SearchFriendsStatusEnum.SUCCESS.status){

            userService.sendFriendRequest(myUserId, friendUsername);

        }else {
            String errorMsg = SearchFriendsStatusEnum.getMsgByKey(status);
            return WeChatJSONResult.errorMsg(errorMsg);
        }

        return WeChatJSONResult.ok("");
    }

    @PostMapping("/queryFriendsRequests")
    public WeChatJSONResult queryFriendsRequests(String userId){

        //0.判断不能为空
        if(StringUtils.isBlank(userId)){
            return WeChatJSONResult.errorMsg("");
        }

        //1. 查询用户接收到的朋友申请
        return WeChatJSONResult.ok(userService.queryFriendRequestList(userId));

    }

    @PostMapping("/operFriendRequest")
    public WeChatJSONResult operFriendRequest(String acceptUserId, String sendUserId, Integer operType){

        //0.判断不能为空
        if(StringUtils.isBlank(acceptUserId) || StringUtils.isBlank(sendUserId) || operType == null){
            return WeChatJSONResult.errorMsg("");
        }

        //1. 如果operType没有对应的枚举值，则直接抛出空错误信息
       if (StringUtils.isBlank(OperatorFriendRequestTypeEnum.getMsgByType(operType))){

           return WeChatJSONResult.errorMsg("");
       }

       if(operType == OperatorFriendRequestTypeEnum.IGNORE.type){
           //2. 判断如果忽略好友请求，则直接删除好友请求数据库表记录
           userService.deleteFriendRequest(sendUserId,acceptUserId);
       }else{
           //3. 判断如果是通过好友请求，则互相增加好友记录到数据库对应的表
           //然后删除好友请求的数据库表记录
           userService.passFriendRequest(sendUserId, acceptUserId);
       }

        // 数据库查询好友列表
        List<MyFriendsVO> myFriends = userService.queryMyFriends(acceptUserId);

       return WeChatJSONResult.ok(myFriends);
    }

    @PostMapping("/myFriends")
    public WeChatJSONResult myFriends(String userId){

        if(StringUtils.isBlank(userId)){
            return WeChatJSONResult.errorMsg("");
        }

        // 数据库查询好友列表
        List<MyFriendsVO> myFriends = userService.queryMyFriends(userId);
//        System.out.println(myFriends);
        return WeChatJSONResult.ok(myFriends);
    }

    /**
     * 用户手机端获取未签收的消息列表
     * @param acceptUserId
     * @return
     */
    @PostMapping("/getUnReadMsgList")
    public WeChatJSONResult getUnReadMsgList(String acceptUserId){

        if(StringUtils.isBlank(acceptUserId)){
            return WeChatJSONResult.errorMsg("");
        }

        // 数据库查询未签收消息列表
        List<ChatMsg> unreadMsgList = userService.getUnReadMsgList(acceptUserId);
        return WeChatJSONResult.ok(unreadMsgList);
    }



}
