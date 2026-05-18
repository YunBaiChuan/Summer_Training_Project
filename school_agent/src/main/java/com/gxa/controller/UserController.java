package com.gxa.controller;

import com.gxa.pojo.entity.User;
import com.gxa.result.Result;
import com.gxa.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
@Tag(name = "用户接口")
public class UserController {

    @Resource
    private UserService userService;

    @Operation(summary = "新增用户")
    @PostMapping("/register")
    public User register(@RequestBody User user) {
        return userService.register(user);
    }

    @Operation(summary = "查找用户")
    @GetMapping("/search")
    public List<User> searchUsers(@RequestParam(required = false) String nickname,
                                  @RequestParam(required = false) String phone) {
        return userService.getUsersByCondition(nickname, phone);
    }

    @Operation(summary = "更新用户信息")
    @PutMapping("/update")
    public boolean updateUser(@RequestBody User user) {
        return userService.updateUser(user);
    }

    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")
    public boolean deleteUser(@PathVariable Long id) {
        return userService.deleteById(id);
    }

    @Operation(summary = "增加信用分")
    @PutMapping("/increase-credit/{userId}")
    public void increaseCredit(@PathVariable Long userId) {
        userService.increaseCredit(userId);
    }

    @Operation(summary = "减少信用分")
    @PutMapping("/decrease-credit/{userId}")
    public void decreaseCredit(@PathVariable Long userId) {
        userService.decreaseCredit(userId);
    }

    @Operation(summary = "根据id查询用户")
    @GetMapping("/{id}")
    public Result<User> getById(@PathVariable Long id){
        return Result.buildSuccess(userService.getById(id));
    }

    @Operation(summary = "用户列表")
    @GetMapping("/list")
    public Result<List<User>> list(){
        return Result.buildSuccess(userService.list());
    }
}