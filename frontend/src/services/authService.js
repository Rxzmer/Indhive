const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

export const getToken = () => localStorage.getItem('token');

export const logout = async () => {
  const token = getToken();
  if (!token) return;

  try {
    await fetch(`${API_URL}/api/auth/logout`, {
      method: 'POST',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
  } catch (e) {
    console.error('Error al cerrar sesión:', e);
  }

  localStorage.removeItem('token');
};

export const isAuthenticated = () => !!getToken();
