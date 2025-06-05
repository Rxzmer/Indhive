import React, { useState } from 'react';
import './Modal.css';

const CreateProjectModal = ({ onClose, onProjectCreated }) => {
  const [form, setForm] = useState({
    title: '',
    description: ''
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const token = localStorage.getItem('token');
  const apiUrl = process.env.REACT_APP_API_URL;

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    if (!form.title.trim()) {
      setError('El título es obligatorio');
      setLoading(false);
      return;
    }

    try {
      const res = await fetch(`${apiUrl}/api/projects`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`
        },
        body: JSON.stringify(form)
      });

      if (!res.ok) {
        const msg = await res.text();
        throw new Error(msg || 'Error al crear proyecto');
      }

      onProjectCreated(); // Actualiza la lista de proyectos
      onClose(); // Cierra el modal
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-card" onClick={e => e.stopPropagation()}>
        <h3 className="modal-title">Crear nuevo proyecto</h3>
        <form onSubmit={handleSubmit} className="modal-form">
          <input
            type="text"
            name="title"
            placeholder="Título"
            value={form.title}
            onChange={handleChange}
            required
            autoFocus
          />
          <textarea
            name="description"
            placeholder="Descripción"
            value={form.description}
            onChange={handleChange}
            rows={4}
          />
          {error && <p className="modal-error">{error}</p>}

          <div className="modal-buttons">
            <button type="submit" className="register-button" disabled={loading}>
              {loading ? 'Creando...' : 'Crear'}
            </button>
            <button type="button" className="register-button" onClick={onClose}>Cancelar</button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default CreateProjectModal;
