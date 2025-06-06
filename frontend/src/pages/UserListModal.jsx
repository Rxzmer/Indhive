import React, { useState } from 'react';
import './UserListModal.css';

const UserListModal = ({
  users,
  onClose,
  onDelete,
  search,
  setSearch,
  onUserUpdated,
  setToastMessage,
  setToastType
}) => {
  const [editId, setEditId] = useState(null);
  const [editForm, setEditForm] = useState({ username: '', email: '', roles: '' });

  const filtered = users.filter(u =>
    u.username.toLowerCase().includes(search.toLowerCase()) ||
    u.email.toLowerCase().includes(search.toLowerCase())
  );

  const handleEditClick = (user) => {
    setEditId(user.id);
    setEditForm({ username: user.username, email: user.email, roles: user.roles });
  };

  const handleEditChange = (e) => {
    setEditForm({ ...editForm, [e.target.name]: e.target.value });
  };

  const handleEditSave = async () => {
    const token = localStorage.getItem('token');
    const apiUrl = process.env.REACT_APP_API_URL;

    try {
      const res = await fetch(`${apiUrl}/api/users/${editId}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify(editForm),
      });

      if (!res.ok) {
        const msg = await res.text();
        if (setToastType) setToastType('error');
        if (setToastMessage) setToastMessage(msg || 'Error al guardar los cambios');
        return;
      }

      setEditId(null);
      if (setToastType) setToastType('success');
      if (setToastMessage) setToastMessage('Usuario actualizado correctamente');

      if (onUserUpdated) onUserUpdated();
    } catch (err) {
      if (setToastType) setToastType('error');
      if (setToastMessage) setToastMessage('Error inesperado: ' + err.message);
    }
  };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-card user-list-modal" style={{ width: '90%', maxWidth: '1200px' }} onClick={e => e.stopPropagation()}>
        <h3 className="modal-title">Lista de Usuarios</h3>

        <input
          type="text"
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          placeholder="Buscar por nombre o email"
          className="register-input search-input"
        />

        <div className="user-table-wrapper">
          <table className="user-table styled-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Usuario</th>
                <th>Email</th>
                <th>Rol</th>
                <th>Acciones</th>
              </tr>
            </thead>
            <tbody>
              {filtered.map(u => (
                <tr key={u.id}>
                  <td>{u.id}</td>
                  <td>
                    {editId === u.id ? (
                      <input className="table-input" name="username" value={editForm.username} onChange={handleEditChange} />
                    ) : (
                      u.username
                    )}
                  </td>
                  <td>
                    {editId === u.id ? (
                      <input className="table-input" name="email" value={editForm.email} onChange={handleEditChange} />
                    ) : (
                      u.email
                    )}
                  </td>
                  <td>
                    {editId === u.id ? (
                      <select className="table-select" name="roles" value={editForm.roles} onChange={handleEditChange}>
                        <option value="ROLE_USER">Usuario</option>
                        <option value="ROLE_ADMIN">Administrador</option>
                        <option value="ROLE_USER,ROLE_CREATOR">Creador</option>
                      </select>
                    ) : (
                      <span className="role-tag">
                        {u.roles.split(',').map(r => r.replace('ROLE_', '')).join(', ')}
                      </span>
                    )}
                  </td>
                  <td>
                    {editId === u.id ? (
                      <div className="edit-actions animated">
                        <button onClick={handleEditSave} className="register-button">Guardar</button>
                        <button onClick={() => {
                          const el = document.querySelector('.edit-actions.animated');
                          if (el) {
                            el.classList.add('fade-out');
                            setTimeout(() => {
                              el.classList.remove('fade-out');
                              setEditId(null);
                            }, 200);
                          } else {
                            setEditId(null);
                          }
                        }} className="register-button">Cancelar</button>
                      </div>
                    ) : (
                      <div className="edit-actions">
                        <button onClick={() => handleEditClick(u)} className="register-button">Editar</button>
                        <button onClick={() => {
                          if (window.confirm('¿Estás seguro de que deseas eliminar este usuario?')) {
                            onDelete(u.id);
                          }
                        }} className="register-button delete-button red-button">Eliminar</button>
                      </div>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

      </div>
    </div>
  );
};

export default UserListModal;
