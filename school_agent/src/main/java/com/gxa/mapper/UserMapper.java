package com.gxa.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gxa.pojo.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    int insert(User user);

    User selectById(@Param("id") Long id);

    // 新增的方法
    List<User> selectAll();

    List<User> selectByCondition(@Param("nickname") String nickname,
                                 @Param("phone") String phone);

    User selectByPhone(@Param("phone") String phone);

    int update(User user);

    int updateCredit(@Param("id") Long id,
                     @Param("credit") Integer credit);

    // 新增的方法
    int deleteById(@Param("id") Long id);

    int deleteBatch(@Param("ids") List<Long> ids);

    @Update("UPDATE user SET credit = credit + 1 WHERE id = #{userId}")
    int incrCredit(@Param("userId") Long userId);
}