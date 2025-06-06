import React from 'react';
import './Modal.css';

const ProjectDetailModal = ({ project, onClose }) => {
  if (!project) return null;

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-card" onClick={(e) => e.stopPropagation()}>
        <button
          className="register-close"
          onClick={onClose}
          title="Cerrar"
        >
          ✕
        </button>

        <div
          className="project-description"
          style={{ marginBottom: '1rem' }}
          dangerouslySetInnerHTML={{ __html: project.description }}
        />

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

        <div className="modal-buttons">
          <button className="register-button" onClick={onClose}>Cerrar</button>
        </div>
      </div>
    </div>
  );
};

export default ProjectDetailModal;
