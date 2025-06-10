import React, { useState, useEffect } from 'react';
import ReactQuill from 'react-quill';
import 'react-quill/dist/quill.snow.css';
import './Modal.css';

const ProjectDetailModal = ({ project, onClose, onUpdated }) => {
  const token = localStorage.getItem('token');
  const apiUrl = process.env.REACT_APP_API_URL;

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

  useEffect(() => {
    if (Array.isArray(project.collaborators)) {
      setCollaborators(project.collaborators);
    } else if (Array.isArray(project.collaboratorUsernames)) {
      setCollaborators(project.collaboratorUsernames.map((username, i) => ({
        id: -100 - i,
        username,
      })));
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

  const handleChange = (field, value) => {
    setFormData(prev => ({ ...prev, [field]: value }));
  };

  const searchUsers = async (query) => {
    if (query.length < 2) return setUserSearch({ query, results: [] });

    try {
      const res = await fetch(`${apiUrl}/api/users?search=${encodeURIComponent(query)}`, {
        headers: { Authorization: `Bearer ${token}` },
      });

      if (!res.ok) throw new Error();
      const data = await res.json();

      setUserSearch({
        query,
        results: data.filter(u =>
          !collaborators.some(c => c.username === u.username) &&
          u.id !== project.ownerId
        ),
      });
    } catch {
      setUserSearch({ query, results: [] });
    }
  };

  const handleAddCollaborator = (user) => {
    setCollaborators([...collaborators, user]);
    setUserSearch({ query: '', results: [] });
  };

  const handleRemoveCollaborator = (id) => {
    setCollaborators(collaborators.filter(u => u.id !== id));
  };

  const handleSave = async () => {
  setLoading(true);
  try {
    if (!formData.title.trim()) {
      alert('El título es obligatorio');
      setLoading(false);
      return;
    }

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

    if (!res.ok) {
      const msg = await res.text();
      throw new Error(msg || 'Error desconocido al guardar el proyecto');
    }

    const updatedProject = await (await fetch(`${apiUrl}/api/projects/${project.id}`, {
      headers: { Authorization: `Bearer ${token}` },
    })).json();

    onUpdated(updatedProject);
    setEditMode(false);
    onClose(); 
  } catch (err) {
    console.error('Error al guardar proyecto:', err.message || err);
    alert('Error: ' + err.message);
  } finally {
    setLoading(false);
  }
};

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-card" onClick={e => e.stopPropagation()}>
        {editMode ? (
          <form onSubmit={(e) => { e.preventDefault(); handleSave(); }} className="modal-form">
            <h3 className="modal-title">{project.title}</h3>
            {project.owner && (
              <div className="project-owner-info">
                <div className="owner-name">{project.owner.username}</div>
                <div className="owner-email">{project.owner.email}</div>
              </div>
            )}



            <input
              type="text"
              value={formData.title}
              onChange={e => handleChange('title', e.target.value)}
              placeholder="Título"
              required
            />
            <label style={{ marginBottom: '0.5rem', display: 'block' }}>Descripción detallada:</label>
            <ReactQuill
              theme="snow"
              value={formData.description}
              onChange={(value) => handleChange('description', value)}
            />

            <div className="user-autocomplete">
              <input
                type="text"
                placeholder="Buscar usuarios colaboradores"
                value={userSearch.query}
                onChange={(e) => {
                  const val = e.target.value;
                  setUserSearch(prev => ({ ...prev, query: val }));
                  searchUsers(val);
                }}
              />
              {userSearch.results.length > 0 && (
                <ul className="suggestions-list">
                  {userSearch.results.map(u => (
                    <li key={u.id} onClick={() => handleAddCollaborator(u)}>
                      {u.username}
                    </li>
                  ))}
                </ul>
              )}
              <div className="selected-users">
                {collaborators.map(u => (
                  <span key={u.id + u.username} className="user-tag">
                    {u.username}
                    {u.id > 0 && (
                      <button onClick={() => handleRemoveCollaborator(u.id)} disabled={loading}>
                        x
                      </button>
                    )}
                  </span>
                ))}
              </div>
            </div>

            <div className="modal-buttons">
              <button type="submit" className="register-button" disabled={loading}>
                {loading ? 'Guardando...' : 'Guardar'}
              </button>
              <button type="button" className="register-button" onClick={() => setEditMode(false)}>
                Cancelar
              </button>
            </div>
          </form>
        ) : (
          <>
            <h3 className="modal-title">{project.title}</h3>
            <div className="project-description" dangerouslySetInnerHTML={{ __html: project.description }} />
            {collaborators.length > 0 && (
              <div className="user-tags-container">
                <h4 style={{ marginBottom: '0.5rem' }}>Colaboradores:</h4>
                {collaborators.map(u => (
                  <span key={u.id + u.username} className="user-tag">{u.username}</span>
                ))}
              </div>
            )}
            {permissions.canEdit && (
              <div className="modal-buttons">
                <button className="register-button" onClick={() => setEditMode(true)}>
                  Editar
                </button>
              </div>
            )}
          </>
        )}
      </div>
    </div>
  );
};

export default ProjectDetailModal;
