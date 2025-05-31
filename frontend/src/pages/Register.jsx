import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import './Register.css';
import background from '../assets/background.jpg';

const Register = () => {
  const navigate = useNavigate();
  const [form, setForm] = useState({
    username: '',
    email: '',
    password: '',
  });
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');

    try {
      const response = await fetch(
        `${import.meta.env.VITE_API_URL || 'http://localhost:8080'}/api/auth/register`,
        {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(form),
        }
      );

      if (!response.ok) {
        const msg = await response.text();
        throw new Error(msg || 'Error al registrar');
      }

      setSuccess('Registro exitoso. Ahora puedes iniciar sesión.');
      setTimeout(() => navigate('/login'), 2000);
    } catch (err) {
      setError(err.message);
    }
  };

  return (
    <div
      className="register-container"
      style={{ backgroundImage: `url(${background})` }}
    >
      <div className="register-card">
        <button className="register-close" onClick={() => navigate('/')}>✕</button>
        <h2 className="register-title">Registro de Usuario</h2>
        <form onSubmit={handleSubmit} className="register-form">
          <input
            type="text"
            name="username"
            placeholder="Usuario"
            value={form.username}
            onChange={handleChange}
            required
            className="register-input"
          />
          <input
            type="email"
            name="email"
            placeholder="Correo electrónico"
            value={form.email}
            onChange={handleChange}
            required
            className="register-input"
          />
          <input
            type="password"
            name="password"
            placeholder="Contraseña"
            value={form.password}
            onChange={handleChange}
            required
            className="register-input"
          />
          <button type="submit" className="register-button">Registrarse</button>
        </form>

        {success && <p className="register-success">{success}</p>}
        {error && <p className="register-error">{error}</p>}
      </div>
    </div>
  );
};

export default Register;
