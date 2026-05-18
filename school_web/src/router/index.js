// src/router/index.js
import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'home',
    component: () => import('../components/HomePage.vue') // 需要创建这个文件
  },
  {
    path: '/assistant',
    name: 'assistant',
    component: () => import('../components/second-hand-assistant.vue')
  },

    {
    path: '/session',
    name: 'session',
    component: () => import('../components/Session.vue')
  },

  {
    path: '/product',
    name: 'product',
    component: () => import('../components/ProductManagement.vue')
  },

  {
    path: '/category-management',
    name: 'Category',
    component: () => import('../components/Category.vue') 
  },

  {
    path: '/orders-selling',
    name: 'orders-selling', 
    component: () => import('../components/MyOrders.vue'),
    props: { role: 'admin' } 
  },

  {
  path: '/evaluation-manage',
  name: 'evaluation-manage',
  component: () => import('../components/EvaluationManage.vue')
  },

  {
  path: '/users',
  name: 'user-manage',
  component: () => import('../components/UserManage.vue')
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router