import api from './axiosClient';

const aiApi = {
  chat: async (payload) => {
    const response = await api.post('/api/ai/chat', payload);
    return response.data.data;
  },
  getRecommendations: async () => {
    const response = await api.get('/api/ai/recommendations');
    return response.data.data;
  },
};

export default aiApi;
