import api from './axiosClient';

const paymentApi = {
  initiate: async (payload) => {
    const response = await api.post('/api/payments/initiate', payload);
    return response.data.data;
  },
  getStatus: async (paymentId) => {
    const response = await api.get(`/api/payments/${paymentId}`);
    return response.data.data;
  },
  complete: async (paymentId) => {
    const response = await api.post(`/api/payments/${paymentId}/complete`);
    return response.data.data;
  },
  fail: async (paymentId) => {
    const response = await api.post(`/api/payments/${paymentId}/fail`);
    return response.data.data;
  },
  cancel: async (paymentId) => {
    const response = await api.post(`/api/payments/${paymentId}/cancel`);
    return response.data.data;
  },
};

export default paymentApi;
