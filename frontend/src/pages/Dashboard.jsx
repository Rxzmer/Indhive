import React, { useEffect, useState } from 'react';
import './Register.css';
import './Dashboard.css';
import './Modal.css';
import background from '../assets/background.jpg';
import logo from '../assets/LogoInd.png';
import { Link } from 'react-router-dom';
import CreateUserModal from './CreateUserModal';
import CreateProjectModal from './CreateProjectModal';
import UserListModal from './UserListModal';

const Dashboard = () => {
  const [projects, setProjects] = useState([]);
  const [users, setUsers] = useState([]);
  const [showUsersModal, setShowUsersModal] = useState(false);
  const [userInfo, setUserInfo] = useState({ username: '', email: '', roles: '' });
  const [searchUser, setSearchUser] = useState('');
  const [showCreateUserModal, setShowCreateUserModal] = useState(false);
  const [showCreateProjectModal, setShowCreateProjectModal] = useState(false);

  const isAdmin = userInfo.roles?.includes('ADMIN');

  const token = localStorage.getItem('token');
  const apiUrl = process.env.REACT_APP_API_URL;

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
      })
      .catch(console.error);
  }, [apiUrl, token]);

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

  const filteredProjects = projects.filter((p) =>
    p.title.toLowerCase().includes(searchUser.toLowerCase())
  );

  const commonActions = [
    { label: 'CREAR PROYECTO', onClick: () => setShowCreateProjectModal(true) },
    { label: 'LISTAR PROYECTOS', path: '/projects' },
  ];

  const adminActions = [
    { label: 'CREAR USUARIO', onClick: () => setShowCreateUserModal(true) },
    { label: 'VER USUARIOS', onClick: () => { setShowUsersModal(true); fetchUsers(); } },
  ];

  const handleLogout = () => {
    localStorage.removeItem('token');
    window.location.href = '/';
  };

  return (
    <div className={`register-container ${showCreateUserModal || showUsersModal ? 'modal-open' : ''}`}>
      <div className="register-background" style={{ backgroundImage: `url(${background})` }} />

      <div className="landing-header" style={{ zIndex: 3 }}>
        <Link to="/" onClick={handleLogout} className="nav-link">LOG OUT</Link>
      </div>

      <div className="dashboard-layout">
        <div className="dashboard-sidebar">
          <div className="dashboard-avatar">
            {userInfo.username.charAt(0).toUpperCase()}
          </div>

          <h2 className="dashboard-title">{userInfo.username.toUpperCase()}</h2>
          <p className="dashboard-info">{userInfo.email}</p>

          <div className="dashboard-button-group">
            {[...commonActions, ...(isAdmin ? adminActions : [])].map((btn, index) => (
              <button
                key={index}
                className="register-button dashboard-button"
                onClick={btn.onClick || (() => window.location.href = btn.path)}
              >
                {btn.label}
              </button>
            ))}
          </div>
        </div>

        <div className="dashboard-content">
          <div className="dashboard-header">
            <img src={logo} alt="Indhive" className="dashboard-logo" />
            <input
              type="text"
              placeholder="Buscar..."
              value={searchUser}
              onChange={(e) => setSearchUser(e.target.value)}
              className="register-input search-input"
            />
          </div>

          <h2 className="section-title">TABLERO DE USUARIO</h2>

          <div className="projects-grid">
            {filteredProjects.map((p) => (
              <div key={p.id} className="project-card">
                <h4>{p.title}</h4>
                <p className="project-description">{p.description}</p>
              </div>
            ))}
          </div>
        </div>
      </div>

      {showCreateUserModal && (
        <CreateUserModal
          onClose={() => setShowCreateUserModal(false)}
          onUserCreated={fetchUsers}
        />
      )}

      {showUsersModal && (
        <UserListModal
          users={users}
          onClose={() => setShowUsersModal(false)}
          onDelete={handleDeleteUser}
          search={searchUser}
          setSearch={setSearchUser}
        />
      )}

      {showCreateProjectModal && (
        <CreateProjectModal
          onClose={() => setShowCreateProjectModal(false)}
          onProjectCreated={() => {
            // recarga proyectos
            fetch(`${apiUrl}/api/projects`, { headers: { Authorization: `Bearer ${token}` } })
              .then(res => res.json())
              .then(setProjects);
            setShowCreateProjectModal(false);
          }}
        />
      )}

    </div>
  );
};

export default Dashboard;
