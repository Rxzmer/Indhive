import React, { useState, useEffect } from 'react';
import './Register.css';

const EditProfileModal = ({ userInfo, editData, setEditData, onClose, onSave, anchorPosition }) => {
  const [errors, setErrors] = useState({});
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (userInfo?.username) {
      const input = document.getElementById('edit-username');
      if (input) input.focus();
    }
  }, [userInfo]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    const newErrors = {};
    if (!editData.username.trim()) newErrors.username = 'El nombre es obligatorio';
    if (!/\S+@\S+\.\S+/.test(editData.email)) newErrors.email = 'Email inválido';

    if (Object.keys(newErrors).length > 0) {
      setErrors(newErrors);
      return;
    }

    setLoading(true);
    await onSave(); // Esta función solo envía username y email
    setLoading(false);
  };

  const modalStyle = anchorPosition
    ? {
        position: 'fixed',
        top: anchorPosition.top + 8,
        left: anchorPosition.left + 8,
        zIndex: 20
      }
    : {
        position: 'fixed',
        top: '50%',
        left: '50%',
        transform: 'translate(-50%, -50%)',
        zIndex: 20
      };

  return (
    <div style={modalStyle}>
      <div className="register-card" style={{ maxWidth: '400px' }}>
        <button className="register-close" onClick={onClose}>✕</button>
        <h2 className="register-title">Editar Perfil</h2>
        <form onSubmit={handleSubmit} className="register-form">
          <input
            id="edit-username"
            type="text"
            value={editData.username}
            onChange={(e) => setEditData({ ...editData, username: e.target.value })}
            placeholder="Usuario"
            className="register-input"
            required
          />
          {errors.username && <small className="register-error">{errors.username}</small>}

          <input
            type="email"
            value={editData.email}
            onChange={(e) => setEditData({ ...editData, email: e.target.value })}
            placeholder="Correo electrónico"
            className="register-input"
            required
          />
          {errors.email && <small className="register-error">{errors.email}</small>}

          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '1rem' }}>
            <button
              type="button"
              onClick={onClose}
              style={{ background: '#666', color: 'white', border: 'none', padding: '0.5rem 1rem', borderRadius: '6px' }}
            >
              Cancelar
            </button>
            {loading ? (
              <div className="dashboard-loading">Guardando...</div>
            ) : (
              <button type="submit" className="register-button">Guardar</button>
            )}
          </div>
        </form>
      </div>
    </div>
  );
};

export default EditProfileModal;
