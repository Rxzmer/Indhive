import React, { useState, useEffect } from 'react';
import ReactQuill from 'react-quill';
import 'react-quill/dist/quill.snow.css';

import './Modal.css';

const CreateProjectModal = ({ onClose, onProjectCreated }) => {
  const [form, setForm] = useState({
    title: '',
    description: ''
  });
  const [query, setQuery] = useState('');
  const [userSuggestions, setUserSuggestions] = useState([]);
  const [selectedUsers, setSelectedUsers] = useState([]);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const token = localStorage.getItem('token');
  const apiUrl = process.env.REACT_APP_API_URL;

  // Debounce para la búsqueda de usuarios
  useEffect(() => {
    const timeoutId = setTimeout(() => {
      if (query.trim().length >= 2) {
        handleUserSearch(query);
      } else {
        setUserSuggestions([]);
      }
    }, 500); // Retraso de 500ms para evitar múltiples solicitudes al escribir

    return () => clearTimeout(timeoutId); // Limpiar el timeout si cambia el query
  }, [query]);

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleUserSearch = async (text) => {
    setQuery(text);
    if (text.trim().length < 2) return setUserSuggestions([]);

    try {
      const res = await fetch(`${apiUrl}/api/users`, {
        headers: { Authorization: `Bearer ${token}` },
      });
      const users = await res.json();
      const filtered = users.filter(u =>
        u.username.toLowerCase().includes(text.toLowerCase()) &&
        !selectedUsers.some(sel => sel.id === u.id)
      );
      setUserSuggestions(filtered);
    } catch (err) {
      console.error('Error buscando usuarios', err);
    }
  };

  const addUser = (user) => {
    setSelectedUsers([...selectedUsers, user]);
    setUserSuggestions([]);
    setQuery('');
  };

  const removeUser = (id) => {
    setSelectedUsers(selectedUsers.filter(u => u.id !== id));
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

    const payload = {
      title: form.title,
      description: form.description,
      collaboratorIds: selectedUsers.map(u => u.id)
    };

    try {
      const res = await fetch(`${apiUrl}/api/projects`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`
        },
        body: JSON.stringify(payload)
      });

      if (!res.ok) {
        let message = 'Error desconocido';
        try {
          const data = await res.json();
          message = data.message || JSON.stringify(data);
        } catch {
          message = await res.text(); // fallback por si no es JSON
        }

        // Diferenciar errores
        if (res.status === 403 || res.status === 401) {
          throw new Error('No tienes permisos o tu sesión ha expirado');
        }

        throw new Error(message);
      }

      onProjectCreated();
      onClose();
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
          <label style={{ marginBottom: '0.5rem', display: 'block' }}>Descripción detallada:</label>
          <ReactQuill
            theme="snow"
            value={form.description}
            onChange={(value) => setForm({ ...form, description: value })}
          />

          <div className="user-autocomplete">
            <input
              type="text"
              placeholder="Buscar usuarios colaboradores"
              value={query}
              onChange={(e) => setQuery(e.target.value)}
            />
            {userSuggestions.length > 0 && (
              <ul className="suggestions-list">
                {userSuggestions.map(u => (
                  <li key={u.id} onClick={() => addUser(u)}>{u.username}</li>
                ))}
              </ul>
            )}
            <div className="selected-users">
              {selectedUsers.map(u => (
                <span key={u.id} className="user-tag">
                  {u.username}
                  <button
                    onClick={() => removeUser(u.id)}
                    disabled={loading} // Deshabilitar si la creación está en proceso
                  >
                    x
                  </button>
                </span>
              ))}
            </div>
          </div>

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
