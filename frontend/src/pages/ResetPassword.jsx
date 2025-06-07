import React, { useState } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import './Register.css';
import background from '../assets/background.jpg';

const ResetPassword = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const token = searchParams.get('token');

  const [password, setPassword] = useState('');
  const [confirm, setConfirm] = useState('');
  const [message, setMessage] = useState('');

  const apiUrl = process.env.REACT_APP_API_URL;

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (password !== confirm) {
      return setMessage('Las contraseñas no coinciden');
    }

    try {
      const res = await fetch(`${apiUrl}/api/auth/reset-password`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ token, password }),
      });

      const text = await res.text();
      setMessage(text);
      if (res.ok) {
        setTimeout(() => navigate('/login'), 2000);
      }
    } catch (err) {
      setMessage('Error al intentar cambiar la contraseña');
    }
  };

  return (
    <div className="register-container">
      <div
        className="register-background"
        style={{ backgroundImage: `url(${background})` }}
      />
      <div className="register-card">
        <button
          className="register-close"
          onClick={() => navigate('/login')}
        >
          ✕
        </button>
        <h2 className="register-title">Nueva Contraseña</h2>
        <form onSubmit={handleSubmit} className="register-form">
          <input
            type="password"
            placeholder="Nueva contraseña"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            className="register-input"
            required
          />
          <input
            type="password"
            placeholder="Confirmar contraseña"
            value={confirm}
            onChange={(e) => setConfirm(e.target.value)}
            className="register-input"
            required
          />
          <button type="submit" className="register-button">Cambiar</button>
        </form>
        {message && <p style={{ marginTop: '1rem', color: 'white' }}>{message}</p>}
      </div>
    </div>
  );
};

export default ResetPassword;
