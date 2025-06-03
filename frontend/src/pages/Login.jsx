import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import background from '../assets/background.jpg';
import './Register.css'; // Reutilizamos el CSS de registro

const Login = () => {
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [rememberMe, setRememberMe] = useState(false);
  const [error, setError] = useState('');

  // Cargar email guardado si existe
  useEffect(() => {
    const savedEmail = localStorage.getItem('savedEmail');
    if (savedEmail) setEmail(savedEmail);
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    try {
      const apiUrl = process.env.REACT_APP_API_URL;
      const response = await fetch(`${apiUrl}/api/auth/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, password }),
      });

      if (!response.ok) {
        const msg = await response.text();
        throw new Error(msg || 'Credenciales inválidas');
      }

      const data = await response.json();
      localStorage.setItem('token', data.token);

      // Guardar email si el checkbox está marcado
      if (rememberMe) {
        localStorage.setItem('savedEmail', email);
      } else {
        localStorage.removeItem('savedEmail');
      }

      navigate('/dashboard');
    } catch (err) {
      setError(err.message);
    }
  };

  return (
    <div className="register-container">
      <div
        className="register-background"
        style={{ backgroundImage: `url(${background})` }}
      />

      <div className="register-card">
        <button className="register-close" onClick={() => navigate('/')}>✕</button>
        <h2 className="register-title">Inicia Sesión</h2>
        <form className="register-form" onSubmit={handleSubmit}>
          <label style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '10px' }}>
            <input
              type="checkbox"
              checked={rememberMe}
              onChange={(e) => setRememberMe(e.target.checked)}
            />
            Recordarme
          </label>
          <input
            type="email"
            placeholder="Correo electrónico"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            className="register-input"
            required
          />
          <input
            type="password"
            placeholder="Contraseña"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            className="register-input"
            required
          />
          <button type="submit" className="register-button">Entrar</button>
          {error && <p className="register-error">{error}</p>}
        </form>
      </div>
    </div>
  );
};

export default Login;
