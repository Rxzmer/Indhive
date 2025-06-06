import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import background from '../assets/background.jpg';
import { Link } from 'react-router-dom';
import './Register.css'; // Reutilizamos el CSS de registro

const Login = () => {
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    const savedEmail = localStorage.getItem('savedEmail');
    const savedPassword = localStorage.getItem('savedPassword');
    if (savedEmail) setEmail(savedEmail);
    if (savedPassword) setPassword(savedPassword);
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
      localStorage.setItem('savedEmail', email);
      localStorage.setItem('savedPassword', password);
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
          <input
            type="email"
            placeholder="Correo electrónico"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            className="register-input"
            required
          />

          <div className="password-wrapper">
            <input
              type={showPassword ? 'text' : 'password'}
              placeholder="Contraseña"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className="register-input password-input"
              required
            />
            <span
              className="password-toggle"
              onClick={() => setShowPassword(!showPassword)}
              aria-label="Mostrar u ocultar contraseña"
            >
              {showPassword ? (
                <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" fill="#ccc" viewBox="0 0 24 24">
                  <path d="M12 5c-7.633 0-12 7-12 7s4.367 7 12 7 12-7 12-7-4.367-7-12-7zm0 12c-2.761 0-5-2.239-5-5s2.239-5 5-5c2.761 0 5 2.239 5 5s-2.239 5-5 5z" />
                  <path d="M12 10c-1.104 0-2 .897-2 2s.896 2 2 2c1.103 0 2-.897 2-2s-.897-2-2-2z" />
                </svg>
              ) : (
                <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" fill="#ccc" viewBox="0 0 24 24">
                  <path d="M2.393 1.393l20.121 20.121-1.414 1.414-2.945-2.945c-1.842.73-3.867 1.017-6.155.646-4.135-.67-7.744-3.709-9.868-7.629l-.132-.25.132-.25c.802-1.521 1.821-2.885 3.008-4.047l-3.014-3.014 1.414-1.414zm3.263 8.091c-1.003.941-1.864 2.093-2.57 3.352 1.807 3.149 5.044 5.86 8.914 6.49 1.671.271 3.247.083 4.679-.446l-2.262-2.262c-.58.175-1.203.272-1.845.272-2.761 0-5-2.239-5-5 0-.642.097-1.265.272-1.845l-2.188-2.188zm6.344-.344c-1.104 0-2 .897-2 2 0 .285.06.556.165.802l2.637 2.637c.246.105.517.165.802.165 1.103 0 2-.897 2-2s-.897-2-2-2c-.285 0-.556.06-.802.165l-.802-.802zm7.715 4.594c.412-.598.783-1.226 1.109-1.872-1.807-3.149-5.044-5.86-8.914-6.49-.861-.139-1.695-.146-2.502-.043l-2.154-2.154c1.355-.36 2.792-.48 4.303-.245 4.135.67 7.744 3.709 9.868 7.629l.132.25-.132.25c-.184.35-.383.692-.598 1.025l-.112.173z" />
                </svg>
              )}
            </span>
          </div>

          <button type="submit" className="register-button">Entrar</button>
          {error && <p className="register-error">{error}</p>}

          <Link to="/recover-password" className="highlight-link" style={{ marginTop: '0.75rem', textAlign: 'center' }}>
            ¿Olvidaste tu contraseña?
          </Link>
        </form>
      </div>
    </div>
  );
};

export default Login;
