import api from './axiosClient';

const productApi = {
  getProducts: async (params = {}) => {
    const response = await api.get('/api/products', { params });
    return response.data.data;
  },
  getProductById: async (id) => {
    const response = await api.get(`/api/products/${id}`);
    return response.data.data;
  },
  createProduct: async (payload) => {
    const response = await api.post('/api/products', payload);
    return response.data.data;
  },
  updateProduct: async (id, payload) => {
    const response = await api.put(`/api/products/${id}`, payload);
    return response.data.data;
  },
  deleteProduct: async (id) => {
    const response = await api.delete(`/api/products/${id}`);
    return response.data.data;
  },
};

export default productApi;
