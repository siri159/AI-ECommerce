import api from './axiosClient';

const reviewApi = {
  getForProduct: async (productId) => {
    const response = await api.get(`/api/products/${productId}/reviews`);
    return response.data.data;
  },
  create: async (productId, payload) => {
    const response = await api.post(`/api/products/${productId}/reviews`, payload);
    return response.data.data;
  },
  update: async (reviewId, payload) => {
    const response = await api.put(`/api/reviews/${reviewId}`, payload);
    return response.data.data;
  },
  remove: async (reviewId) => {
    const response = await api.delete(`/api/reviews/${reviewId}`);
    return response.data.data;
  },
};

export default reviewApi;
