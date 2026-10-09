import api from './axiosClient';

const orderApi = {
  createOrder: async (payload) => {
    const response = await api.post('/api/orders', payload);
    return response.data.data;
  },
  getMyOrders: async () => {
    const response = await api.get('/api/orders/my-orders');
    return response.data.data;
  },
  getOrderById: async (id) => {
    const response = await api.get(`/api/orders/${id}`);
    return response.data.data;
  },
  cancelOrder: async (id) => {
    const response = await api.put(`/api/orders/${id}/cancel`);
    return response.data.data;
  },
  getAllOrders: async () => {
    const response = await api.get('/api/admin/orders');
    return response.data.data;
  },
};

export default orderApi;
