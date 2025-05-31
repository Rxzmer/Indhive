import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import background from '../assets/background.jpg';
import './Register.css'; // Reutilizamos los estilos de login/register

const Dashboard = () => {
  const navigate = useNavigate();
  const [user, setUser] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    const fetchUser = async () => {
      try {
        const token = localStorage.getItem('token');
        const apiUrl = process.env.REACT_APP_API_URL || 'http://localhost:8080';

        const response = await fetch(`${apiUrl}/api/auth/me`, {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        });

        if (!response.ok) {
          const msg = await response.text();
          throw new Error(`Error ${response.status}: ${msg}`);
        }

        const data = await response.json();
        setUser(data);
      } catch (err) {
        setError(err.message);
      }
    };

    fetchUser();
  }, []);

  const handleLogout = () => {
    localStorage.removeItem('token');
    navigate('/');
  };

  return (
    <div className="register-container">
      <div
        className="register-background"
        style={{ backgroundImage: `url(${background})` }}
      />

      <div className="register-card">
        <button className="register-close" onClick={handleLogout}>✕</button>
        <h2 className="register-title">Bienvenido a Indhive</h2>

        {user && user.username ? (
          <>
            <p className="dashboard-info">Usuario: <strong>{user.username}</strong></p>
            <p className="dashboard-info">Correo: {user.email}</p>
            <p className="dashboard-info">Rol: {user.roles}</p>
          </>
        ) : error ? (
          <p className="register-error">{error}</p>
        ) : (
          <p className="dashboard-info">Cargando perfil...</p>
        )}
      </div>
    </div>
  );
};

export default Dashboard;
