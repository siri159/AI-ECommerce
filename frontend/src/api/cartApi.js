import api from './axiosClient';

const cartApi = {
  getCart: async () => {
    const response = await api.get('/api/cart');
    return response.data.data;
  },
  addItem: async (payload) => {
    const response = await api.post('/api/cart/items', payload);
    return response.data.data;
  },
  updateItem: async (itemId, payload) => {
    const response = await api.put(`/api/cart/items/${itemId}`, payload);
    return response.data.data;
  },
  removeItem: async (itemId) => {
    const response = await api.delete(`/api/cart/items/${itemId}`);
    return response.data.data;
  },
  clearCart: async () => {
    const response = await api.delete('/api/cart');
    return response.data.data;
  },
};

export default cartApi;
