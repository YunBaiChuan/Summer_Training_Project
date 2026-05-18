// 商品管理API
export const productAPI = {
  // 新增商品
  async addProduct(product) {
    try {
      const res = await fetch('/api/product/add', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(product)
      });

      if (!res.ok) {
        throw new Error(`HTTP ${res.status}: ${res.statusText}`);
      }

      return await res.json();
    } catch (error) {
      console.error('新增商品失败:', error);
      return {
        code: 500,
        message: '新增商品失败，请稍后再试'
      };
    }
  },

  // 更新商品
  async updateProduct(product) {
    try {
      const res = await fetch('/api/product/update/product', {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(product)
      });

      if (!res.ok) {
        throw new Error(`HTTP ${res.status}: ${res.statusText}`);
      }

      return await res.json();
    } catch (error) {
      console.error('更新商品失败:', error);
      return {
        code: 500,
        message: '更新商品失败，请稍后再试'
      };
    }
  },

  // 删除商品
  async deleteProduct(id) {
    try {
      const res = await fetch(`/api/product/delete?id=${id}`, {
        method: 'DELETE',
        headers: {
          'Content-Type': 'application/json'
        }
      });

      if (!res.ok) {
        throw new Error(`HTTP ${res.status}: ${res.statusText}`);
      }

      return await res.json();
    } catch (error) {
      console.error('删除商品失败:', error);
      return {
        code: 500,
        message: '删除商品失败，请稍后再试'
      };
    }
  },

  // 分页查询商品
  async listProducts(params) {
    try {
      const queryParams = new URLSearchParams();
      if (params.page) queryParams.append('page', params.page);
      if (params.limit) queryParams.append('limit', params.limit);
      if (params.sellerId) queryParams.append('sellerId', params.sellerId);
      if (params.categoryId) queryParams.append('categoryId', params.categoryId);
      if (params.title) queryParams.append('title', params.title);

      console.log('请求商品列表的参数:', params);
      console.log('请求URL:', `/api/product/list?${queryParams}`);

      const res = await fetch(`/api/product/list?${queryParams}`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json'
        }
      });

      console.log('响应状态:', res.status);
      console.log('响应状态文本:', res.statusText);

      if (!res.ok) {
        throw new Error(`HTTP ${res.status}: ${res.statusText}`);
      }

      const data = await res.json();
      console.log('从后端获取的原始数据:', data);
      return data;
    } catch (error) {
      console.error('查询商品失败:', error);
      return {
        code: 500,
        message: '查询商品失败，请稍后再试',
        data: [],
        total: 0
      };
    }
  },

  // 查询用户发布的商品
  async getSellerProducts(sellerId) {
    try {
      const res = await fetch(`/api/product/seek/product/${sellerId}`, {
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
      console.error('查询用户商品失败:', error);
      return {
        code: 500,
        message: '查询用户商品失败，请稍后再试',
        data: []
      };
    }
  },

  // 搜索商品
  async searchProducts(title) {
    try {
      const res = await fetch(`/api/product/search?title=${encodeURIComponent(title)}`, {
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
      console.error('搜索商品失败:', error);
      return {
        code: 500,
        message: '搜索商品失败，请稍后再试',
        data: []
      };
    }
  }
};