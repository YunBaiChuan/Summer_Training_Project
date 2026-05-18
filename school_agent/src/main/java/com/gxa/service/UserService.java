package com.gxa.service;

import com.gxa.pojo.entity.User;

import java.util.List;

public interface UserService {

    User register(User user);

    User login(String phone, String password);

    User getById(Long id);

    // 新增的方法
    List<User> getAllUsers();

    List<User> getUsersByCondition(String nickname, String phone);

    boolean updateUser(User user);

    boolean updatePassword(Long id, String oldPassword, String newPassword);

    boolean deleteById(Long id);

    boolean deleteBatch(List<Long> ids);

    void increaseCredit(Long userId);

    void decreaseCredit(Long userId);

    List<User> list();
}