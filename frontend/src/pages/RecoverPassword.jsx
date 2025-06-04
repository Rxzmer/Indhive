import React, { useState } from 'react';
import './Register.css';
import background from '../assets/background.jpg';

const RecoverPassword = () => {
  const [email, setEmail] = useState('');
  const [message, setMessage] = useState('');
  const apiUrl = process.env.REACT_APP_API_URL;

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const res = await fetch(`${apiUrl}/api/auth/recover`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email })
      });
      const text = await res.text();
      setMessage(text || 'Si el correo existe, recibirás instrucciones.');
    } catch (err) {
      setMessage('Error al procesar la solicitud.');
    }
  };

  return (
    <div className="register-container">
      <div
        className="register-background"
        style={{ backgroundImage: `url(${background})` }}
      />
      <div className="register-card">
        <h2 className="register-title">Recuperar Contraseña</h2>
        <form onSubmit={handleSubmit} className="register-form">
          <input
            type="email"
            name="email"
            placeholder="Correo electrónico"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
            className="register-input"
          />
          <button type="submit" className="register-button">Enviar</button>
        </form>
        {message && <p style={{ marginTop: '1rem', color: 'white' }}>{message}</p>}
      </div>
    </div>
  );
};

export default RecoverPassword;
