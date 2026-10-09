import api from './axiosClient';

const authApi = {
  register: async (payload) => {
    const response = await api.post('/api/auth/register', payload);
    return response.data.data;
  },
  login: async (payload) => {
    const response = await api.post('/api/auth/login', payload);
    return response.data.data;
  },
};

export default authApi;
