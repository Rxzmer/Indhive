import React, { useEffect, useState } from 'react';
import './Register.css';
import background from '../assets/background.jpg';
import logo from '../assets/LogoInd.png';
import { Link } from 'react-router-dom';
import EditProfileModal from './EditProfileModal';

const Dashboard = () => {
  const [projects, setProjects] = useState([]);
  const [users, setUsers] = useState([]);
  const [showUsers, setShowUsers] = useState(false);
  const [userInfo, setUserInfo] = useState({ username: '', email: '', roles: '' }); 
  const [showEditModal, setShowEditModal] = useState(false);
  const [editData, setEditData] = useState({ username: '', email: '', password: '' });
  const [searchUser, setSearchUser] = useState('');
  const [anchorPosition, setAnchorPosition] = useState(null);

  const isAdmin = userInfo.roles?.includes('ADMIN'); 


  const token = localStorage.getItem('token');
  const apiUrl = process.env.REACT_APP_API_URL;

  // eslint-disable-next-line react-hooks/exhaustive-deps
  useEffect(() => {
    fetch(`${apiUrl}/api/projects`, {
      headers: { Authorization: `Bearer ${token}` },
    })
      .then((res) => res.json())
      .then(setProjects)
      .catch(console.error);

    fetch(`${apiUrl}/api/auth/me`, {
      headers: { Authorization: `Bearer ${token}` },
    })
      .then((res) => res.json())
      .then(data => {
        setUserInfo({ username: data.username, email: data.email, id: data.id, roles: data.roles });
        setEditData({ username: data.username, email: data.email, password: '' });
      })
      .catch(console.error);
  }, []);

  const fetchUsers = () => {
    fetch(`${apiUrl}/api/users`, {
      headers: { Authorization: `Bearer ${token}` },
    })
      .then((res) => res.json())
      .then(setUsers)
      .catch(console.error);
  };

  const handleDeleteUser = async (id) => {
    if (!window.confirm('¿Eliminar este usuario?')) return;
    await fetch(`${apiUrl}/api/users/${id}`, {
      method: 'DELETE',
      headers: { Authorization: `Bearer ${token}` },
    });
    setUsers(users.filter((u) => u.id !== id));
  };

  const handleEditSubmit = async () => {
    const body = {
      username: editData.username,
      email: editData.email,
    };

    try {
      await fetch(`${apiUrl}/api/users/${userInfo.id}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify(body),
      });

      if (editData.password.trim()) {
        const res = await fetch(`${apiUrl}/api/users/me/password`, {
          method: 'PUT',
          headers: {
            'Content-Type': 'application/json',
            Authorization: `Bearer ${token}`,
          },
          body: JSON.stringify({ password: editData.password }),
        });

        const text = await res.text();

        if (res.status === 401) {
          alert("Tu sesión ha expirado. No se pudo cambiar la contraseña.");
          localStorage.removeItem('token');
          window.location.href = '/login';
          return;
        }

        if (!res.ok) {
          alert("Error al cambiar la contraseña: " + text);
          return;
        }

        alert("Contraseña cambiada correctamente. Se cerrará tu sesión por seguridad.");
        localStorage.removeItem('token');
        window.location.href = '/login';
        return;
      }

      setShowEditModal(false);
      window.location.reload();

    } catch (err) {
      alert("Error inesperado: " + err.message);
    }
  };

  const filteredProjects = projects.filter((p) =>
    p.title.toLowerCase().includes(searchUser.toLowerCase())
  );

  const adminActions = [
    { label: 'CREAR PROYECTO', path: '/create-project' },
    { label: 'CREAR USUARIO', path: '/create-user' },
    { label: 'VER USUARIOS', onClick: () => { setShowUsers(true); fetchUsers(); } },
    { label: 'LISTAR PROYECTOS', path: '/projects' },
  ];

  const handleLogout = () => {
    localStorage.removeItem('token');
    window.location.href = '/';
  };

  return (
    <div className="register-container">
      <div
        className="register-background"
        style={{ backgroundImage: `url(${background})` }}
      />

      <div className="landing-header" style={{ zIndex: 3 }}>
        <Link to="/" onClick={handleLogout} className="nav-link">LOG OUT</Link>
      </div>

      <div style={{ display: 'flex', height: '100vh', width: '100%', zIndex: 1 }}>
        <div style={{ width: '350px', padding: '2rem', backgroundColor: 'rgba(27,31,39,0.6)', backdropFilter: 'blur(10px)', display: 'flex', flexDirection: 'column', alignItems: 'center', position: 'relative' }}>
          <div style={{
            width: '90px',
            height: '90px',
            borderRadius: '50%',
            backgroundColor: 'white',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            fontSize: '2.5rem',
            fontWeight: 'bold',
            color: '#1e1e1e',
            marginTop: '1rem',
            marginBottom: '1.5rem',
            border: '2px solid white'
          }}>
            {userInfo.username.charAt(0).toUpperCase()}
          </div>

          <div
            onClick={(e) => {
              const rect = e.currentTarget.getBoundingClientRect();
              setAnchorPosition({ top: rect.top, left: rect.right });
              setShowEditModal(true);
            }}
            style={{
              position: 'absolute',
              top: '1.2rem',
              right: '1.2rem',
              cursor: 'pointer',
              color: 'white',
              fontSize: '1.2rem'
            }}
            title="Editar perfil"
          >
            ✎
          </div>

          <h2 className="dashboard-title" style={{ color: 'white', marginBottom: '0.2rem' }}>{userInfo.username.toUpperCase()}</h2>
          <p className="dashboard-info" style={{ color: 'white', marginBottom: '1rem' }}>{userInfo.email}</p>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem', marginTop: '0.5rem', width: '100%' }}>
            {adminActions.map((btn, index) => (
              <button
                key={index}
                className="register-button"
                style={{
                  width: '100%',
                  backgroundColor: 'white',
                  color: '#000',
                  fontWeight: 'bold',
                  boxShadow: '0 2px 6px rgba(0,0,0,0.2)',
                  transition: 'all 0.3s',
                  cursor: 'pointer'
                }}
                onMouseOver={e => e.currentTarget.style.backgroundColor = '#f0f0f0'}
                onMouseOut={e => e.currentTarget.style.backgroundColor = 'white'}
                onClick={btn.onClick || (() => window.location.href = btn.path)}>
                {btn.label}
              </button>
            ))}
          </div>
        </div>

        <div style={{ flex: 1, padding: '2rem', overflowY: 'auto', color: 'white' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
            <img src={logo} alt="Indhive" style={{ height: '100px' }} />
            <input
              type="text"
              placeholder="Buscar..."
              value={searchUser}
              onChange={(e) => setSearchUser(e.target.value)}
              className="register-input"
              style={{
                maxWidth: '300px',
                height: '32px',
                padding: '4px 8px',
                fontSize: '14px',
                marginTop: '0.5rem'
              }}
            />
          </div>

          <h2 style={{ marginBottom: '1rem' }}>PROYECTOS RECIENTES</h2>

          <div style={{ display: 'flex', flexWrap: 'wrap', gap: '1rem' }}>
            {filteredProjects.map((p) => (
              <div key={p.id} style={{ background: '#1e2228', padding: '1rem', borderRadius: '8px', width: '250px', color: 'white', boxShadow: '0 2px 6px rgba(0,0,0,0.3)' }}>
                <h4>{p.title}</h4>
                <p style={{ fontSize: '0.85rem' }}>{p.description}</p>
              </div>
            ))}
          </div>

          {showUsers && (
            <div style={{ marginTop: '2rem' }}>
              <h3>USUARIOS</h3>
              <table style={{ width: '100%', borderCollapse: 'collapse', marginTop: '1rem', backgroundColor: '#2c2f36', color: 'white', textAlign: 'left' }}>
                <thead>
                  <tr>
                    <th style={{ padding: '0.5rem' }}>ID</th>
                    <th style={{ padding: '0.5rem' }}>Usuario</th>
                    <th style={{ padding: '0.5rem' }}>Email</th>
                    <th style={{ padding: '0.5rem' }}>Rol</th>
                    <th style={{ padding: '0.5rem' }}>Acciones</th>
                  </tr>
                </thead>
                <tbody>
                  {users
                    .filter(u => u.username.toLowerCase().includes(searchUser.toLowerCase()) || u.email.toLowerCase().includes(searchUser.toLowerCase()))
                    .map((u) => (
                      <tr key={u.id}>
                        <td style={{ padding: '0.5rem' }}>{u.id}</td>
                        <td style={{ padding: '0.5rem' }}>{u.username}</td>
                        <td style={{ padding: '0.5rem' }}>{u.email}</td>
                        <td style={{ padding: '0.5rem' }}>{u.roles}</td>
                        <td style={{ padding: '0.5rem' }}>
                          <button
                            onClick={() => handleDeleteUser(u.id)}
                            style={{ color: 'red', border: 'none', background: 'none', cursor: 'pointer' }}>
                            Eliminar User
                          </button>
                        </td>
                      </tr>
                    ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>

      {showEditModal && (
        <EditProfileModal
          userInfo={userInfo}
          editData={editData}
          setEditData={setEditData}
          onClose={() => setShowEditModal(false)}
          onSave={handleEditSubmit}
          anchorPosition={anchorPosition}
        />
      )}
    </div>
  );
};

export default Dashboard;
