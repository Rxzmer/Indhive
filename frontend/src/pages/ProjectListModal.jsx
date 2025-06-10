import React, { useEffect, useState } from 'react';
import './Modal.css';

const ProjectListModal = ({ onClose }) => {
  const [projects, setProjects] = useState([]);
  const [search, setSearch] = useState('');
  const token = localStorage.getItem('token');
  const apiUrl = process.env.REACT_APP_API_URL;

  useEffect(() => {
    fetch(`${apiUrl}/api/projects`, {
      headers: { Authorization: `Bearer ${token}` },
    })
      .then((res) => res.json())
      .then(setProjects)
      .catch((err) => console.error('Error al obtener proyectos:', err));
  }, [apiUrl, token]);

  const filtered = projects.filter(p =>
    p.title.toLowerCase().includes(search.toLowerCase())
  );

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-card" style={{ width: '90%', maxWidth: '1000px' }} onClick={e => e.stopPropagation()}>
        <h3 className="modal-title">Lista de Proyectos</h3>

        <input
          type="text"
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          placeholder="Buscar por nombre"
          className="register-input search-input"
        />

        <div className="user-table-wrapper" style={{ marginTop: '1rem' }}>
          <table className="user-table styled-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Título</th>
                <th>Descripción</th>
                <th>Colaboradores</th>
              </tr>
            </thead>
            <tbody>
              {filtered.map(p => (
                <tr key={p.id}>
                  <td>{p.id}</td>
                  <td>{p.title}</td>
                  <td>
                    <div dangerouslySetInnerHTML={{ __html: p.description }} />
                  </td>
                  <td>
                    {(p.collaborators || []).map((c, idx) => (
                      <span key={idx} className="user-tag">
                        {c.username || c}
                      </span>
                    ))}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        <div class="table-container">
  <table class="modal-table">
  </table>
</div>
<div class="table-container">
  <table class="search-table">
  </table>
</div>

        <div className="modal-buttons">
          <button className="register-button" onClick={onClose}>Cerrar</button>
        </div>
      </div>
    </div>

    
  );
};

export default ProjectListModal;
