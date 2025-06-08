import React, { useState, useEffect } from 'react';
import ReactQuill from 'react-quill';
import 'react-quill/dist/quill.snow.css';
import './Modal.css';

const ProjectDetailModal = ({ project, onClose, onUpdated }) => {
  const [editMode, setEditMode] = useState(false);
  const [formData, setFormData] = useState({
    title: project.title,
    description: project.description
  });
  const [collaborators, setCollaborators] = useState(project.collaborators || []);
  const [userSearch, setUserSearch] = useState({ query: '', results: [] });
  const [loading, setLoading] = useState(false);
  const [permissions, setPermissions] = useState({
    canEdit: false,
    isAdmin: false,
    isOwner: false
  });

  const token = localStorage.getItem('token');
  const apiUrl = process.env.REACT_APP_API_URL;

  useEffect(() => {
    if (Array.isArray(project.collaborators)) {
      setCollaborators(project.collaborators);
    } else {
      setCollaborators([]);
    }
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
          isOwner
        });
      } catch (err) {
        console.error("Error checking permissions:", err);
      }
    };

    checkPermissions();
  }, [apiUrl, token, project.ownerId]);

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
            !collaborators.some(c => c.id === u.id) &&
            u.id !== project.ownerId
          )
        }));
      } else {
        console.error("La respuesta del servidor no es un array");
        setUserSearch(prev => ({ ...prev, results: [] }));
      }
    } catch (err) {
      console.error("Error searching users:", err);
      setUserSearch(prev => ({ ...prev, results: [] }));
    }
  };

  const handleChange = (field, value) => {
    setFormData(prev => ({ ...prev, [field]: value }));
  };

  const handleSave = async () => {
    setLoading(true);
    try {
      const res = await fetch(`${apiUrl}/api/projects/${project.id}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({
          title: formData.title,
          description: formData.description,
          collaboratorIds: collaborators.map(c => c.id)
        })
      });

      if (!res.ok) throw new Error('Error al guardar el proyecto');

      const res2 = await fetch(`${apiUrl}/api/projects/${project.id}`, {
        headers: { Authorization: `Bearer ${token}` },
      });

      const fullProject = await res2.json();
      onUpdated(fullProject);
      setEditMode(false);
    } catch (err) {
      console.error("Error saving project:", err);
    } finally {
      setLoading(false);
    }
  };

  const handleAddCollaborator = (user) => {
    if (!collaborators.some(c => c.id === user.id)) {
      setCollaborators(prev => [...prev, user]);
    }
    setUserSearch(prev => ({ ...prev, query: '', results: [] }));
  };

  const handleRemoveCollaborator = async (userId) => {
    try {
      await fetch(`${apiUrl}/api/projects/${project.id}/collaborators/${userId}`, {
        method: 'DELETE',
        headers: { Authorization: `Bearer ${token}` },
      });

      setCollaborators(prev => prev.filter(c => c.id !== userId));
    } catch (err) {
      console.error("Error removing collaborator:", err);
    }
  };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-card" onClick={(e) => e.stopPropagation()}>
        {project.bannerUrl && (
          <div className="banner-container">
            <img src={project.bannerUrl} alt="Banner del proyecto" className="project-banner" />
          </div>
        )}

        {permissions.canEdit && (
          <button
            className="register-close"
            onClick={() => setEditMode(!editMode)}
            title={editMode ? "Cancelar edición" : "Editar proyecto"}
          >
            {editMode ? '✕' : '✎'}
          </button>
        )}

        {editMode ? (
          <>
            <input
              className="register-input"
              value={formData.title}
              onChange={(e) => handleChange('title', e.target.value)}
              placeholder="Título del proyecto"
            />

            <label className="description-label">Descripción:</label>
            <ReactQuill
              theme="snow"
              value={formData.description}
              onChange={(value) => handleChange('description', value)}
              modules={{
                toolbar: [
                  ['bold', 'italic', 'underline'],
                  [{ list: 'bullet' }],
                  ['clean']
                ]
              }}
              style={{
                backgroundColor: '#1a1a1a',
                color: 'white',
                marginBottom: '1rem'
              }}
            />

            <div className="collaborators-section">
              <h4>Colaboradores:</h4>

              <div className="user-search">
                <input
                  type="text"
                  placeholder="Buscar usuarios..."
                  value={userSearch.query}
                  onChange={(e) => {
                    setUserSearch(prev => ({ ...prev, query: e.target.value }));
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

              <div className="current-collaborators">
                {collaborators.map(user => (
                  <span key={user.id} className="user-tag">
                    {user.username}
                    <button
                      onClick={() => handleRemoveCollaborator(user.id)}
                      className="remove-collaborator"
                    >
                      ×
                    </button>
                  </span>
                ))}
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

            {collaborators.length > 0 && (
              <div className="user-tags-container">
                <h4 style={{ marginBottom: '0.5rem' }}>Colaboradores:</h4>
                {collaborators.map(user => (
                  <span key={user.id} className="user-tag">
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
