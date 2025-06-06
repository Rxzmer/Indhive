// src/components/ProjectForm.jsx
import React, { useEffect, useState } from 'react';
import ReactQuill from 'react-quill';
import 'react-quill/dist/quill.snow.css';

const ProjectForm = ({ initialData = {}, onSubmit, users = [], onCancel }) => {
  const [title, setTitle] = useState(initialData.title || '');
  const [description, setDescription] = useState(initialData.description || '');
  const [selectedIds, setSelectedIds] = useState(
    initialData.collaboratorIds || []
  );

  const toggleCollaborator = (id) => {
    setSelectedIds(prev =>
      prev.includes(id) ? prev.filter(uid => uid !== id) : [...prev, id]
    );
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    onSubmit({ title, description, collaboratorIds: selectedIds });
  };

  return (
    <form onSubmit={handleSubmit}>
      <input
        className="register-input"
        type="text"
        placeholder="Título del proyecto"
        value={title}
        onChange={(e) => setTitle(e.target.value)}
        required
      />
      <ReactQuill theme="snow" value={description} onChange={setDescription} />

      <div className="collaborator-list">
        <h4>Colaboradores</h4>
        {users.map((u) => (
          <label key={u.id} className="collaborator-option">
            <input
              type="checkbox"
              checked={selectedIds.includes(u.id)}
              onChange={() => toggleCollaborator(u.id)}
            />
            {u.username}
          </label>
        ))}
      </div>

      <div className="modal-buttons">
        <button className="register-button" type="submit">Guardar</button>
        <button className="register-button" type="button" onClick={onCancel}>Cancelar</button>
      </div>
    </form>
  );
};

export default ProjectForm;
