// src/api/user.js
import request from '@/utils/request'

export const userAPI = {

  // 查询用户
  getUserList(params) {
    // params: { nickname, phone }
    return request.get('/user/search', { params })
  },

  // 新增用户
  addUser(data) {
    return request.post('/user/register', data)
  },

  // 更新用户
  updateUser(data) {
    return request.put('/user/update', data)
  },

  // 删除用户
  deleteUser(id) {
    return request.delete(`/user/${id}`)
  },

  // 增加信誉分
  addCredit(userId) {
    return request.put(`/user/increase-credit/${userId}`)
  },

  // 减少信誉分
  subCredit(userId) {
    return request.put(`/user/decrease-credit/${userId}`)
  }
}
