package com.hyj.mapper;

import java.util.List;

import com.hyj.pojo.Users;
import com.hyj.pojo.vo.FriendRequestVO;
import com.hyj.pojo.vo.MyFriendsVO;
import com.hyj.utils.MyMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UsersMapperCustom extends MyMapper<Users> {
	
	List<FriendRequestVO> queryFriendRequestList(String acceptUserId);
	
	List<MyFriendsVO> queryMyFriends(String userId);
	
	void batchUpdateMsgSigned(List<String> msgIdList);
	
}