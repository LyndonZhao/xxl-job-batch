package pn.lyndon.batch.user.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import pn.lyndon.batch.user.entity.User;

@Mapper
public interface UserDao {
	
	public List<User> selectUser(User user);
	public Integer insertUser(@Param("user") User user);

}
