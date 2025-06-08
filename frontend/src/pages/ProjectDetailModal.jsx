import React, { useState, useEffect } from 'react';
import ReactQuill from 'react-quill';
import 'react-quill/dist/quill.snow.css';
import './Modal.css';

const ProjectDetailModal = ({ project, onClose, onUpdated }) => {
  const token = localStorage.getItem('token');
  const apiUrl = process.env.REACT_APP_API_URL;

  // Estado
  const [editMode, setEditMode] = useState(false);
  const [formData, setFormData] = useState({
    title: project.title,
    description: project.description,
  });
  const [collaborators, setCollaborators] = useState([]);
  const [userSearch, setUserSearch] = useState({ query: '', results: [] });
  const [loading, setLoading] = useState(false);
  const [permissions, setPermissions] = useState({
    canEdit: false,
    isAdmin: false,
    isOwner: false,
  });

  // Este hook garantiza que SIEMPRE tengas colaboradores en estado, usando lo que venga
  useEffect(() => {
    if (Array.isArray(project.collaborators) && project.collaborators.length > 0) {
      setCollaborators(project.collaborators);
    } else if (Array.isArray(project.collaboratorUsernames) && project.collaboratorUsernames.length > 0) {
      // Si no tienes los objetos, los creas con un id ficticio negativo (solo para vista)
      setCollaborators(
        project.collaboratorUsernames.map((username, i) => ({
          id: -100 - i, // valor negativo para que nunca choque con un id real
          username,
        }))
      );
    } else {
      setCollaborators([]);
    }
    setFormData({
      title: project.title,
      description: project.description,
    });
  }, [project]);

  useEffect(() => {
    const checkPermissions = async () => {
      try {
        const res = await fetch(`${apiUrl}/api/auth/me`, {
          headers: { Authorization: `Bearer ${token}` },
        });
        const user = await res.json();

        const isAdmin = user.roles?.includes('ROLE_ADMIN');
        const isOwner = user.id === project.ownerId;

        setPermissions({
          canEdit: isAdmin || (user.roles?.includes('ROLE_CREATOR') && isOwner),
          isAdmin,
          isOwner,
        });
      } catch (err) {
        console.error('Error checking permissions:', err);
      }
    };
    checkPermissions();
  }, [apiUrl, token, project.ownerId]);

  // --------
  // Métodos auxiliares
  // --------

  const handleChange = (field, value) => {
    setFormData(prev => ({ ...prev, [field]: value }));
  };

  const searchUsers = async (query) => {
    if (query.length < 2) {
      setUserSearch(prev => ({ ...prev, results: [] }));
      return;
    }

    try {
      const encodedQuery = encodeURIComponent(query);
      const res = await fetch(`${apiUrl}/api/users?search=${encodedQuery}`, {
        headers: { Authorization: `Bearer ${token}` },
      });

      if (!res.ok) throw new Error('Error al realizar la búsqueda de usuarios');
      const data = await res.json();

      if (Array.isArray(data)) {
        setUserSearch(prev => ({
          ...prev,
          results: data.filter(u =>
            !collaborators.some(c => c.username === u.username) &&
            u.id !== project.ownerId
          ),
        }));
      } else {
        setUserSearch(prev => ({ ...prev, results: [] }));
      }
    } catch (err) {
      setUserSearch(prev => ({ ...prev, results: [] }));
    }
  };

  const handleAddCollaborator = (user) => {
    if (!collaborators.some(c => c.id === user.id || c.username === user.username)) {
      setCollaborators(prev => [...prev, user]);
    }
    setUserSearch(prev => ({ ...prev, query: '', results: [] }));
  };

  const handleRemoveCollaborator = (userId) => {
    setCollaborators(prev => prev.filter(c => c.id !== userId));
  };

  const handleSave = async () => {
    setLoading(true);
    try {
      // Filtra solo los colaboradores que tienen id > 0
      const validCollaborators = collaborators.filter(c => c.id > 0);
      const collaboratorIds = validCollaborators.map(c => c.id);

      const res = await fetch(`${apiUrl}/api/projects/${project.id}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({
          title: formData.title,
          description: formData.description,
          collaboratorIds,
        }),
      });

      if (!res.ok) throw new Error('Error al guardar el proyecto');

      const res2 = await fetch(`${apiUrl}/api/projects/${project.id}`, {
        headers: { Authorization: `Bearer ${token}` },
      });
      const fullProject = await res2.json();
      onUpdated(fullProject);
      setEditMode(false);
    } catch (err) {
      console.error('Error saving project:', err);
    } finally {
      setLoading(false);
    }
  };

  // --------
  // Renderizado
  // --------
  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-card" onClick={e => e.stopPropagation()}>
        {project.bannerUrl && (
          <div className="banner-container">
            <img
              src={project.bannerUrl}
              alt="Banner del proyecto"
              className="project-banner"
            />
          </div>
        )}

        {permissions.canEdit && (
          <button
            className="register-close"
            onClick={() => setEditMode(!editMode)}
            title={editMode ? 'Cancelar edición' : 'Editar proyecto'}
          >
            {editMode ? '✕' : '✎'}
          </button>
        )}

        {editMode ? (
          <>
            <input
              className="register-input"
              value={formData.title}
              onChange={e => handleChange('title', e.target.value)}
              placeholder="Título del proyecto"
            />

            <label className="description-label">Descripción:</label>
            <ReactQuill
              theme="snow"
              value={formData.description}
              onChange={value => handleChange('description', value)}
              modules={{
                toolbar: [
                  ['bold', 'italic', 'underline'],
                  [{ list: 'bullet' }],
                  ['clean'],
                ],
              }}
              style={{
                backgroundColor: '#1a1a1a',
                color: 'white',
                marginBottom: '1rem',
              }}
            />

            <div className="collaborators-section">
              <h4>Colaboradores:</h4>
              <div className="user-search">
                <input
                  type="text"
                  placeholder="Buscar usuarios..."
                  value={userSearch.query}
                  onChange={e => {
                    setUserSearch(prev => ({
                      ...prev,
                      query: e.target.value,
                    }));
                    searchUsers(e.target.value);
                  }}
                />

                {userSearch.results.length > 0 && (
                  <ul className="user-suggestions">
                    {userSearch.results.map(user => (
                      <li key={user.id} onClick={() => handleAddCollaborator(user)}>
                        {user.username}
                      </li>
                    ))}
                  </ul>
                )}
              </div>

              <div className="current-collaborators user-tags-container">
                {collaborators.length > 0 ? (
                  collaborators.map(user => (
                    <span key={user.id + user.username} className="user-tag">
                      {user.username}
                      {/* Solo muestra X para usuarios válidos */}
                      {user.id > 0 && (
                        <button
                          type="button"
                          className="remove-collaborator"
                          onClick={() => handleRemoveCollaborator(user.id)}
                          title="Eliminar colaborador"
                          style={{
                            marginLeft: '0.5em',
                            background: 'none',
                            border: 'none',
                            color: '#e5534b',
                            fontWeight: 'bold',
                            cursor: 'pointer',
                            fontSize: '1em',
                          }}
                        >
                          ×
                        </button>
                      )}
                    </span>
                  ))
                ) : (
                  <span style={{ color: '#888' }}>Sin colaboradores</span>
                )}
              </div>
            </div>

            <div className="modal-actions">
              <button
                onClick={handleSave}
                disabled={loading}
                className="register-button"
              >
                {loading ? 'Guardando...' : 'Guardar Cambios'}
              </button>
              <button
                onClick={() => setEditMode(false)}
                className="register-button cancel-button"
              >
                Cancelar
              </button>
            </div>
          </>
        ) : (
          <>
            <h3 className="modal-title">{project.title}</h3>
            <div
              className="project-description"
              dangerouslySetInnerHTML={{ __html: project.description }}
            />

            {/* Vista de solo lectura */}
            {collaborators.length > 0 && (
              <div className="user-tags-container">
                <h4 style={{ marginBottom: '0.5rem' }}>Colaboradores:</h4>
                {collaborators.map((user, i) => (
                  <span key={user.id + user.username} className="user-tag">
                    {user.username}
                  </span>
                ))}
              </div>
            )}
          </>
        )}
      </div>
    </div>
  );
};

export default ProjectDetailModal;
