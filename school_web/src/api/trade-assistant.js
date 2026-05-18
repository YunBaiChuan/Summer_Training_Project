// 在 tradeAPI 中
export const tradeAPI = {
  async sendStreamMessage({ content, chatId }) {
    const p = new URLSearchParams({ 
      msg: content, 
      sessionId: chatId, 
      userId: 'demo' 
    })
    
    const res = await fetch(`/api/ai/chat/v2?${p}`, { 
      method: 'GET', 
      headers: { 
        'Accept': 'text/event-stream',
        'Cache-Control': 'no-cache'
      } 
    })
    
    if (!res.ok) {
      throw new Error(`HTTP ${res.status}: ${res.statusText}`)
    }
    
    // 直接返回响应体，让前端组件处理流
    return res.body
  },
  
  async getSessions() {
    try {
      const userId = localStorage.getItem('userId') || 'demo';
      // 使用与 Session.vue 一致的API路径
      const res = await fetch(`/api/ai/history/sessions?userId=${userId}`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json'
        }
      });
      
      if (!res.ok) {
        throw new Error(`HTTP ${res.status}: ${res.statusText}`);
      }
      
      return await res.json();
    } catch (error) {
      console.error('获取会话信息失败:', error);
      return { 
        code: 200,
        data: [] 
      };
    }
  },
  
  // 新增：获取聊天历史列表
  async getChatHistory() {
    try {
      const userId = localStorage.getItem('userId') || 'demo';

      //修正接口路径为 /api/history/chat/history
      const res = await fetch(`/api/history/chat/history?userId=${userId}`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json'
        }
      });
      
      if (!res.ok) {
        throw new Error(`HTTP ${res.status}: ${res.statusText}`);
      }
      
      return await res.json();
    } catch (error) {
      console.error('获取聊天历史失败:', error);
      return { 
        code: 200,  // 返回200，前端好处理
        data: [] 
      };
    }
  },
  
  // 新增：获取指定聊天的消息
  async getChatMessages(chatId) {
    try {
      const res = await fetch(`/api/chat/messages?chatId=${chatId}`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json'
        }
      });
      
      if (!res.ok) {
        throw new Error(`HTTP ${res.status}: ${res.statusText}`);
      }
      
      return await res.json();
    } catch (error) {
      console.error('获取聊天消息失败:', error);
      return { 
        code: 200,
        data: [] 
      };
    }
  },

  // 1. 获取商品类别列表（分页）
  async getCategoryList(params = { current: 1, size: 10 }) {
    try {
      const p = new URLSearchParams({
        current: params.current,
        size: params.size
      });
      
      const res = await fetch(`/api/category/page?${p}`, {  // 改为 /category/page
        method: 'GET',
        headers: {
          'Content-Type': 'application/json'
        }
      });

      console.log('请求URL:', `/category/page?${p}`);
      console.log('响应状态:', res.status);

      if (!res.ok) {
        throw new Error(`HTTP ${res.status}: ${res.statusText}`);
      }

      const result = await res.json();
      console.log('完整响应:', result);  // 查看后端返回的实际格式
      
      return result;
    } catch (error) {
      console.error('获取商品类别列表失败:', error);
      return {
        code: 500,
        data: { records: [], total: 0 },
        msg: '请求失败'
      };
    }
  },

  // 2. 新增商品类别
  async addCategory(categoryName) {
    try {
      console.log('新增类别请求:', { name: categoryName });
      
      const res = await fetch('/api/category', {  
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ name: categoryName })
      });

      console.log('新增API响应状态:', res.status);
      
      const result = await res.json();
      console.log('新增API响应数据:', result);
      
      if (!res.ok) {
        throw new Error(`HTTP ${res.status}: ${res.statusText}`);
      }
      
      return result;
      
    } catch (error) {
      console.error('新增商品类别失败:', error);
      return {
        code: 500,
        msg: '新增类别失败: ' + error.message
      };
    }
  },

  // 3. 编辑商品类别
  async editCategory(categoryId, categoryName) {
    try {
      console.log('编辑类别请求:', { id: categoryId, name: categoryName });
      
      const res = await fetch(`/api/category/${categoryId}`, { 
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ name: categoryName })
      });

      console.log('编辑API响应状态:', res.status);
      
      const result = await res.json();
      console.log('编辑API响应数据:', result);
      
      if (!res.ok) {
        throw new Error(`HTTP ${res.status}: ${res.statusText}`);
      }
      
      return result;
      
    } catch (error) {
      console.error('编辑商品类别失败:', error);
      return {
        code: 500,
        msg: '编辑类别失败: ' + error.message
      };
    }
  },

  // 4. 删除商品类别
  async deleteCategory(categoryId) {
    try {
      console.log('删除类别请求ID:', categoryId);
      
      const res = await fetch(`/api/category/${categoryId}`, {  
        method: 'DELETE',
        headers: {
          'Content-Type': 'application/json'
        }
      });

      console.log('删除API响应状态:', res.status);
      
      const result = await res.json();
      console.log('删除API响应数据:', result);
      
      if (!res.ok) {
        throw new Error(`HTTP ${res.status}: ${res.statusText}`);
      }
      
      return result;
      
    } catch (error) {
      console.error('删除商品类别失败:', error);
      return {
        code: 500,
        msg: '删除类别失败: ' + error.message
      };
    }
  },

  async getCategoryById(id) {
    try {
      console.log('查询类别详情请求ID:', id);
      
      const res = await fetch(`/api/category/${id}`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json'
        }
      });

      console.log('查询详情API响应状态:', res.status);
      
      const result = await res.json();
      console.log('查询详情API响应数据:', result);
      
      if (!res.ok) {
        throw new Error(`HTTP ${res.status}: ${res.statusText}`);
      }
      
      return result;
      
    } catch (error) {
      console.error('查询类别详情失败:', error);
      return {
        code: 500,
        data: null,
        msg: '查询类别详情失败: ' + error.message
      };
    }
  },

}