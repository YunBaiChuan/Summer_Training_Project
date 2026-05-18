package com.gxa.service.impl;

import com.gxa.pojo.entity.User;
import com.gxa.mapper.UserMapper;
import com.gxa.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Resource
    private UserMapper userMapper;

    @Override
    public User register(User user) {
        // 检查手机号是否已存在
        User existingUser = userMapper.selectByPhone(user.getPhone());
        if (existingUser != null) {
            throw new RuntimeException("手机号已注册");
        }

        // 设置默认值
        if (user.getCredit() == null) {
            user.setCredit(100);
        }

        userMapper.insert(user);
        return user;
    }

    @Override
    public User login(String phone, String password) {
        User user = userMapper.selectByPhone(phone);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (!user.getPassword().equals(password)) {
            throw new RuntimeException("密码错误");
        }
        return user;
    }

    @Override
    public User getById(Long id) {
        return userMapper.selectById(id);
    }

    @Override
    public List<User> getAllUsers() {
        return userMapper.selectAll();
    }

    @Override
    public List<User> getUsersByCondition(String nickname, String phone) {
        return userMapper.selectByCondition(nickname, phone);
    }

    @Override
    public boolean updateUser(User user) {
        if (user == null || user.getId() == null) {
            throw new RuntimeException("用户ID不能为空");
        }

        // 如果要更新手机号，检查是否重复
        if (StringUtils.hasText(user.getPhone())) {
            User existingUser = userMapper.selectByPhone(user.getPhone());
            if (existingUser != null && !existingUser.getId().equals(user.getId())) {
                throw new RuntimeException("手机号已被其他用户使用");
            }
        }

        return userMapper.update(user) > 0;
    }

    @Override
    public boolean updatePassword(Long id, String oldPassword, String newPassword) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 验证旧密码
        if (!user.getPassword().equals(oldPassword)) {
            throw new RuntimeException("旧密码错误");
        }

        // 更新密码
        User updateUser = new User();
        updateUser.setId(id);
        updateUser.setPassword(newPassword);

        return userMapper.update(updateUser) > 0;
    }

    @Override
    public boolean deleteById(Long id) {
        return userMapper.deleteById(id) > 0;
    }

    @Override
    public boolean deleteBatch(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new RuntimeException("请选择要删除的用户");
        }

        int deletedCount = 0;
        for (Long id : ids) {
            if (deleteById(id)) {
                deletedCount++;
            }
        }

        return deletedCount > 0;
    }

    @Override
    public void increaseCredit(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        userMapper.updateCredit(userId, user.getCredit() + 1);
    }

    @Override
    public void decreaseCredit(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        userMapper.updateCredit(userId, user.getCredit() - 1);
    }

    @Override
    public List<User> list() {
        return userMapper.selectList(null);   // MP 默认方法
    }
}