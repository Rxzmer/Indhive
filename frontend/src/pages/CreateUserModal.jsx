import React, { useState } from 'react';
import './Modal.css';

const CreateUserModal = ({ onClose, onUserCreated }) => {
  const [form, setForm] = useState({
    username: '',
    email: '',
    password: '',
    roles: 'ROLE_USER',
  });

  const [error, setError] = useState('');
  const apiUrl = process.env.REACT_APP_API_URL;
  const token = localStorage.getItem('token');

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    try {
      const response = await fetch(`${apiUrl}/api/users`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify(form),
      });

      if (!response.ok) {
        const msg = await response.text();
        throw new Error(msg || 'Error al crear usuario');
      }

      onUserCreated();
      onClose();
    } catch (err) {
      setError(err.message);
    }
  };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-card" onClick={(e) => e.stopPropagation()}>
        <h3>Crear nuevo usuario</h3>
        <form onSubmit={handleSubmit} className="modal-form">
          <input
            type="text"
            name="username"
            placeholder="Usuario"
            value={form.username}
            onChange={handleChange}
            required
          />
          <input
            type="email"
            name="email"
            placeholder="Email"
            value={form.email}
            onChange={handleChange}
            required
          />
          <input
            type="password"
            name="password"
            placeholder="Contraseña"
            value={form.password}
            onChange={handleChange}
            required
          />
          <select name="roles" value={form.roles} onChange={handleChange}>
            <option value="ROLE_USER">Usuario</option>
            <option value="ROLE_ADMIN">Administrador</option>
          </select>

          {error && <p className="modal-error">{error}</p>}

          <div className="modal-buttons">
            <button type="submit" className="register-button">Crear</button>
            <button type="button" className="register-button" onClick={onClose}>Cancelar</button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default CreateUserModal;
