package com.hyj.mapper;

import com.hyj.pojo.ChatMsg;
import com.hyj.utils.MyMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChatMsgMapper extends MyMapper<ChatMsg> {
}