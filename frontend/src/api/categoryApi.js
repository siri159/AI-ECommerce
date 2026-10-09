import api from './axiosClient';

const categoryApi = {
  getAll: async () => {
    const response = await api.get('/api/categories');
    return response.data.data;
  },
  getById: async (id) => {
    const response = await api.get(`/api/categories/${id}`);
    return response.data.data;
  },
  create: async (payload) => {
    const response = await api.post('/api/categories', payload);
    return response.data.data;
  },
  update: async (id, payload) => {
    const response = await api.put(`/api/categories/${id}`, payload);
    return response.data.data;
  },
  remove: async (id) => {
    const response = await api.delete(`/api/categories/${id}`);
    return response.data.data;
  },
};

export default categoryApi;
