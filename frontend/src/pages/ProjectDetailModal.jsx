import React, { useState, useEffect } from 'react';
import ReactQuill from 'react-quill';
import 'react-quill/dist/quill.snow.css';
import './Modal.css';

const ProjectDetailModal = ({ project, onClose, onUpdated }) => {
  const [isEditing, setIsEditing] = useState(false);
  const [title, setTitle] = useState(project.title);
  const [description, setDescription] = useState(project.description);
  const [canEdit, setCanEdit] = useState(false);
  const token = localStorage.getItem('token');
  const apiUrl = process.env.REACT_APP_API_URL;

  useEffect(() => {
    // Obtener usuario actual para saber si es dueño o admin
    fetch(`${apiUrl}/api/auth/me`, {
      headers: { Authorization: `Bearer ${token}` },
    })
      .then(res => res.json())
      .then(user => {
        const isAdmin = user.roles?.includes('ADMIN');
        const isOwner = user.username === project.owner;
        setCanEdit(isAdmin || isOwner);
      })
      .catch(console.error);
  }, [apiUrl, token, project.owner]);

  const modules = {
    toolbar: [
      ['bold', 'italic', 'underline'],
      [{ list: 'bullet' }],
      ['clean']
    ]
  };

  const handleSave = async () => {
    try {
      const res = await fetch(`${apiUrl}/api/projects/${project.id}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({ title, description })
      });

      if (res.ok) {
        onUpdated(); // recarga lista de proyectos
        setIsEditing(false);
      } else {
        alert('Error al guardar');
      }
    } catch (err) {
      console.error(err);
    }
  };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-card" onClick={(e) => e.stopPropagation()}>
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
            />
            <ReactQuill
              theme="snow"
              value={description}
              onChange={setDescription}
              modules={modules}
              style={{
                backgroundColor: 'white',
                borderRadius: '8px',
                color: 'black',
                marginBottom: '1rem'
              }}
            />
            <div className="modal-buttons">
              <button className="register-button" onClick={handleSave}>Guardar</button>
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

        {project.collaborators && project.collaborators.length > 0 && (
          <div style={{ marginTop: '1rem' }}>
            <h4>Colaboradores:</h4>
            <ul>
              {project.collaborators.map((colab, idx) => (
                <li key={idx}>{colab.username || colab}</li>
              ))}
            </ul>
          </div>
        )}
      </div>
    </div>
  );
};

export default ProjectDetailModal;
