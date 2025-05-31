import React, { useEffect, useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import background from '../assets/background.jpg';
import './Register.css';

const Dashboard = () => {
  const navigate = useNavigate();
  const [user, setUser] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    const fetchUser = async () => {
      try {
        const token = localStorage.getItem('token');
        const apiUrl = process.env.REACT_APP_API_URL;

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

  const getAvatar = (username) => {
    const initials = username.slice(0, 2).toUpperCase();
    return (
      <div className="avatar-circle">
        <span>{initials}</span>
      </div>
    );
  };

  return (
    <div className="register-container">
      <div
        className="register-background"
        style={{ backgroundImage: `url(${background})` }}
      />

      <div className="register-card" style={{ textAlign: 'center' }}>
        <button className="register-close" onClick={handleLogout}>✕</button>
        <h2 className="register-title">Mi Perfil</h2>

        {user && user.username ? (
          <>
            {getAvatar(user.username)}
            <h3 className="dashboard-info"><strong>{user.username}</strong></h3>
            <p className="dashboard-info">{user.email}</p>
            <p className="dashboard-info">Rol: {user.roles}</p>

            <div className="dashboard-actions">
              <Link to="/projects" className="register-button">Ver Proyectos</Link>
              <Link to="/projects/new" className="register-button">Crear Proyecto</Link>
            </div>
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
