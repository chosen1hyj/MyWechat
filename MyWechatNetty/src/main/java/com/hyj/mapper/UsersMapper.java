package com.hyj.mapper;

import com.hyj.pojo.Users;
import com.hyj.utils.MyMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UsersMapper extends MyMapper<Users> {
}