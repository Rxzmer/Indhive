import React, { useState, useEffect } from 'react';
import ReactQuill from 'react-quill';
import 'react-quill/dist/quill.snow.css';
import './Modal.css';

const ProjectDetailModal = ({ project, onClose, onUpdated }) => {
  const [isEditing, setIsEditing] = useState(false);
  const [title, setTitle] = useState(project.title);
  const [description, setDescription] = useState(project.description);
  const [canEdit, setCanEdit] = useState(project.canEdit);
  const [query, setQuery] = useState('');
  const [userSuggestions, setUserSuggestions] = useState([]);
  const [selectedUsers, setSelectedUsers] = useState(project.collaborators?.map(c => ({ id: c.id, username: c.username })) || []);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [toastMessage, setToastMessage] = useState('');
  const [toastType, setToastType] = useState('info');
  const token = localStorage.getItem('token');
  const apiUrl = process.env.REACT_APP_API_URL;


  useEffect(() => {
    const checkPermissions = async () => {
      try {
        const res = await fetch(`${apiUrl}/api/auth/me`, {
          headers: { Authorization: `Bearer ${token}` },
        });
        const user = await res.json();
        console.log("Usuario actual:", user); // 🔍 Verifica roles y ID

        const isAdmin = user.roles?.includes('ROLE_ADMIN');
        const isCreator = user.roles?.includes('ROLE_CREATOR');
        const isOwner = user.id === project.ownerId;

        console.log(`isAdmin: ${isAdmin}, isCreator: ${isCreator}, isOwner: ${isOwner}`); // 🔍
        setCanEdit(isAdmin || (isCreator && isOwner));
      } catch (err) {
        console.error("Error al verificar permisos:", err);
      }
    };
    checkPermissions();
  }, [apiUrl, token, project.ownerId]);


  const modules = {
    toolbar: [
      ['bold', 'italic', 'underline'],
      [{ list: 'bullet' }],
      ['clean']
    ]
  };

  const handleSave = async () => {
    try {
      const collaboratorIds = selectedUsers.map(user => user.id);
      const res = await fetch(`${apiUrl}/api/projects/${project.id}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({ title, description, collaboratorIds })
      });

      if (res.ok) {
        onUpdated();
        setIsEditing(false);
        setToastMessage('Proyecto actualizado correctamente');
        setToastType('success');
      } else {
        setToastMessage('Error al guardar');
        setToastType('error');
      }
    } catch (err) {
      console.error(err);
      setToastMessage('Error al guardar el proyecto');
      setToastType('error');
    }
  };

  const handleUserSearch = async (text) => {
  setQuery(text);
  if (text.trim().length < 2) return setUserSuggestions([]);

  try {
    const res = await fetch(`${apiUrl}/api/users`, {
      headers: { Authorization: `Bearer ${token}` },
    });
    const data = await res.json();

    // Asegúrate de que la respuesta sea un arreglo
    if (Array.isArray(data)) {
      const filtered = data.filter((u) =>
        u.username.toLowerCase().includes(text.toLowerCase()) &&
        !selectedUsers.some(sel => sel.id === u.id)
      );
      setUserSuggestions(filtered);
    } else {
      console.error('La respuesta no es un arreglo:', data);
    }
  } catch (err) {
    console.error('Error buscando usuarios:', err);
  }
};


  const addUser = (user) => {
    setSelectedUsers([...selectedUsers, user]);
    setUserSuggestions([]);
    setQuery('');
  };

  const removeUser = async (userId) => {
    if (!userId || loading) return;

    setLoading(true);

    try {
      const res = await fetch(`${apiUrl}/api/projects/${project.id}/collaborators/${userId}`, {
        method: 'DELETE',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      });

      if (!res.ok) {
        const errorData = await res.json();
        throw new Error(errorData.message || 'Error al eliminar colaborador');
      }

      setSelectedUsers(prevUsers => prevUsers.filter(u => u.id !== userId));
      setToastMessage('Colaborador eliminado correctamente');
      setToastType('success');
    } catch (err) {
      console.error('Error eliminando colaborador:', err);
      setError(err.message || 'Error al eliminar colaborador');
      setToastMessage('Error al eliminar colaborador');
      setToastType('error');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-card" onClick={(e) => e.stopPropagation()}>
        {project.bannerUrl && (
          <div className="banner-container">
            <img src={project.bannerUrl} alt="Project banner" className="project-banner" />
          </div>
        )}

        {canEdit && (
          <button
            className="register-close"
            onClick={() => setIsEditing(!isEditing)}
            title={isEditing ? "Cancelar edición" : "Editar proyecto"}
          >
            {isEditing ? '✕' : '✎'}
          </button>
        )}

        {isEditing ? (
          <>
            <input
              className="register-input"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              placeholder="Título del proyecto"
              required
            />
            <label style={{ marginBottom: '0.5rem', display: 'block' }}>Descripción detallada:</label>
            <ReactQuill
              theme="snow"
              value={description}
              onChange={setDescription}
              modules={modules}
              style={{
                backgroundColor: 'black',
                borderRadius: '8px',
                color: 'white',
                marginBottom: '1rem'
              }}
            />

            <div className="user-autocomplete">
              <input
                type="text"
                placeholder="Buscar usuarios colaboradores"
                value={query}
                onChange={(e) => handleUserSearch(e.target.value)}
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
                      type="button"
                      onClick={(e) => {
                        e.stopPropagation();
                        removeUser(u.id);
                      }}
                      disabled={loading}
                      className="user-tag-remove"
                      aria-label={`Eliminar a ${u.username}`}
                    >
                      {loading ? '...' : '×'}
                    </button>
                  </span>
                ))}
              </div>
            </div>

            <div className="modal-buttons">
              <button className="register-button" onClick={handleSave}>Guardar</button>
              <button className="register-button" onClick={() => setIsEditing(false)}>Cancelar</button>
            </div>
          </>
        ) : (
          <>
            <h3 className="modal-title">{project.title}</h3>
            <div
              className="project-description"
              dangerouslySetInnerHTML={{ __html: project.description }}
            />
          </>
        )}

        {Array.isArray(project.collaborators) && project.collaborators.length > 0 && (
          <div style={{ marginTop: '1rem' }}>
            <h4>Colaboradores:</h4>
            <div className="collaborators-tags">
              {project.collaborators.map((colab) => (
                <span key={colab.id} className="user-tag">
                  {colab.username}
                  {isEditing && canEdit && (
                    <button
                      type="button"
                      onClick={(e) => {
                        e.stopPropagation();
                        removeUser(colab.id);
                      }}
                      disabled={loading}
                      className="user-tag-remove"
                    >
                      {loading ? '...' : '×'}
                    </button>
                  )}
                </span>
              ))}
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default ProjectDetailModal;
