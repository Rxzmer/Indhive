import React, { useEffect, useState, useCallback } from 'react';
import './Register.css';
import './Dashboard.css';
import './Modal.css';
import background from '../assets/background.jpg';
import logo from '../assets/LogoInd.png';
import { Link } from 'react-router-dom';
import CreateUserModal from './CreateUserModal';
import CreateProjectModal from './CreateProjectModal';
import UserListModal from './UserListModal';
import ProjectListModal from './ProjectListModal';
import Footer from './Footer';
import ProjectDetailModal from './ProjectDetailModal';
import Toast from './Toast';

const Dashboard = () => {
  const [projects, setProjects] = useState([]);
  const [users, setUsers] = useState([]);
  const [showUsersModal, setShowUsersModal] = useState(false);
  const [userInfo, setUserInfo] = useState({ username: '', email: '', roles: '' });
  const [searchUser, setSearchUser] = useState('');
  const [showCreateUserModal, setShowCreateUserModal] = useState(false);
  const [showCreateProjectModal, setShowCreateProjectModal] = useState(false);
  const [showProjectListModal, setShowProjectListModal] = useState(false);
  const [toastMessage, setToastMessage] = useState('');
  const [toastType, setToastType] = useState('info');
  const [selectedProject, setSelectedProject] = useState(null);
  const [deletingProjectId, setDeletingProjectId] = useState(null);
  const [showConfirmModal, setShowConfirmModal] = useState(false);
  const [projectToDelete, setProjectToDelete] = useState(null);
  const [showOwnProjects, setShowOwnProjects] = useState(false);  // Estado para alternar visibilidad de proyectos

  const isAdmin = userInfo.roles?.includes('ROLE_ADMIN');
  const isCreator = userInfo.roles?.includes('CREATOR');
  const token = localStorage.getItem('token');
  const apiUrl = process.env.REACT_APP_API_URL;

  const fetchProyectos = useCallback(() => {
    fetch(`${apiUrl}/api/projects`, {
      headers: { Authorization: `Bearer ${token}` },
    })
      .then((res) => res.json())
      .then((data) => {
        setProjects(data);
      })
      .catch((err) => {
        setToastType('error');
        setToastMessage('No se pudieron cargar los proyectos');
      });
  }, [apiUrl, token]);

  useEffect(() => {
    fetch(`${apiUrl}/api/auth/me`, {
      headers: { Authorization: `Bearer ${token}` },
    })
      .then((res) => res.json())
      .then(data => {
        setUserInfo({ username: data.username, email: data.email, id: data.id, roles: data.roles });
        fetchProyectos();
      })
      .catch(err => {
        setToastType('error');
        setToastMessage('No se pudo cargar la información del usuario');
      });
  }, [apiUrl, token, fetchProyectos]);

  const fetchUsers = () => {
    fetch(`${apiUrl}/api/users`, {
      headers: { Authorization: `Bearer ${token}` },
    })
      .then((res) => res.json())
      .then(setUsers)
      .catch((err) => {
        setToastType('error');
        setToastMessage('No se pudieron cargar los usuarios');
      });
  };

  // Filtrar proyectos para mostrar los propios o todos los proyectos
  const filteredProjects = showOwnProjects
    ? projects.filter((p) => p.ownerUsername === userInfo.username)  // Filtra proyectos del usuario
    : projects.filter((p) => p.title.toLowerCase().includes(searchUser.toLowerCase())); // Todos los proyectos

  const handleDeleteProject = (id) => {
    setShowConfirmModal(true);
    setProjectToDelete(id);
  };

  const confirmDeleteProject = async () => {
    if (!projectToDelete) return;

    setDeletingProjectId(projectToDelete);

    try {
      const res = await fetch(`${apiUrl}/api/projects/${projectToDelete}`, {
        method: 'DELETE',
        headers: { Authorization: `Bearer ${token}` },
      });

      if (!res.ok) {
        let errorMsg = 'Error desconocido al eliminar el proyecto';
        try {
          const contentType = res.headers.get('content-type');
          if (contentType && contentType.includes('application/json')) {
            const errorData = await res.json();
            errorMsg = errorData.message || errorMsg;
          } else {
            errorMsg = await res.text() || errorMsg;
          }
        } catch {
          // No hay contenido o error al leer
        }
        throw new Error(errorMsg);
      }

      setProjects(prev => prev.filter(project => project.id !== projectToDelete));
      setToastType('success');
      setToastMessage('Proyecto eliminado correctamente');

      fetchProyectos();
    } catch (err) {
      setToastType('error');
      setToastMessage(`Error al eliminar el proyecto: ${err.message}`);
      fetchProyectos();
    } finally {
      setDeletingProjectId(null);
      setShowConfirmModal(false);
      setProjectToDelete(null);
    }
  };

  const cancelDeleteProject = () => {
    setShowConfirmModal(false);
    setProjectToDelete(null);
  };

  const handleDeleteUser = async (id) => {
    if (!token) {
      setToastType('error');
      setToastMessage('No estás autenticado. Vuelve a iniciar sesión.');
      return;
    }

    try {
      const res = await fetch(`${apiUrl}/api/users/${id}`, {
        method: 'DELETE',
        headers: { Authorization: `Bearer ${token}` }
      });

      if (!res.ok) {
        const msg = await res.text();
        throw new Error(msg || 'Error al eliminar usuario');
      }

      setUsers(users.filter((u) => u.id !== id));
      setToastType('success');
      setToastMessage('Usuario eliminado correctamente');
    } catch (err) {
      setToastType('error');
      setToastMessage(`Error al eliminar: ${err.message}`);
    }
  };

  const toggleShowOwnProjects = () => {
    setShowOwnProjects(!showOwnProjects);  // Alterna entre mostrar los proyectos del usuario o todos
  };

  const commonActions = [
    { label: 'CREAR PROYECTO', onClick: () => setShowCreateProjectModal(true) },
    { label: 'LISTAR PROYECTOS', onClick: () => setShowProjectListModal(true) },
  ];

  const adminActions = [
    { label: 'CREAR USUARIO', onClick: () => setShowCreateUserModal(true) },
    { label: 'VER USUARIOS', onClick: () => { setShowUsersModal(true); fetchUsers(); } },
  ];

  const handleLogout = () => {
    localStorage.removeItem('token');
    window.location.href = '/';
  };

  const handleBecomeCreator = async () => {
    try {
      const res = await fetch(`${apiUrl}/api/users/me/creator`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`,
        },
      });

      const data = await res.json();
      if (res.ok && data.token) {
        localStorage.setItem('token', data.token);
        setUserInfo(prev => ({ ...prev, roles: 'ROLE_USER,ROLE_CREATOR' }));
        setToastType('success');
        setToastMessage('¡Ahora eres creador!');
      } else {
        setToastType('error');
        setToastMessage('No se pudo actualizar tu rol.');
      }
    } catch (err) {
      setToastType('error');
      setToastMessage('Error de red');
    }
  };

  return (
    <div className={`register-container ${showCreateUserModal || showUsersModal ? 'modal-open' : ''}`}>
      <div className="register-background" style={{ backgroundImage: `url(${background})` }} />

      <div className="dashboard-toolbar">
        <Link to="/" onClick={handleLogout} className="nav-link">LOG OUT</Link>
        <input
          type="text"
          placeholder="Buscar proyectos..."
          value={searchUser}
          onChange={(e) => setSearchUser(e.target.value)}
          className="register-input search-input"
        />
      </div>

      <div className="dashboard-layout">
        <div className="dashboard-sidebar">
          <div className="dashboard-avatar">
            {userInfo.username.charAt(0).toUpperCase()}
          </div>

          <h2 className="dashboard-title">{userInfo.username.toUpperCase()}</h2>
          {isAdmin && <span className="admin-tag">Admin</span>}
          <p className="dashboard-info">{userInfo.email}</p>

          <div className="dashboard-button-group">
            {/* Botón para alternar entre ver proyectos propios y todos los proyectos */}
            <button
              onClick={toggleShowOwnProjects}  // Alterna entre ver proyectos propios y todos los proyectos
              className="register-button dashboard-button"
            >
              {showOwnProjects ? '[PROYECTOS INDHIVE ]' : '[ MIS PROYECTOS ]'}  {/* Usando íconos ASCII */}
            </button>
            {[...commonActions, ...(isAdmin ? adminActions : [])].map((btn, index) => (
              <button
                key={index}
                className="register-button dashboard-button"
                onClick={btn.onClick}
              >
                {btn.label}
              </button>
            ))}
          </div>
        </div>

        <div className="dashboard-content">
          <div className="dashboard-header">
            <img src={logo} alt="Indhive" className="dashboard-logo" />
          </div>

          <h2 className="section-title">TABLERO DE USUARIO</h2>

          <div className="projects-grid">
            {filteredProjects.map((p) => (
              <div key={p.id} className="project-card" onClick={() => setSelectedProject(p)}>
                {/* La X de eliminación solo se muestra al ADMIN y al propietario del proyecto */}
                {(isAdmin || p.ownerUsername === userInfo.username) && (
                  <button
                    onClick={(e) => {
                      e.preventDefault();
                      e.stopPropagation();
                      handleDeleteProject(p.id);
                    }}
                    className="delete-project-button"
                    title="Eliminar proyecto"
                    disabled={deletingProjectId === p.id}
                  >
                    {deletingProjectId === p.id ? '⏳' : '✕'}
                  </button>
                )}
                <h4>{p.title}</h4>
                <div
                  className="project-description"
                  dangerouslySetInnerHTML={{
                    __html: (() => {
                      const temp = document.createElement('div');
                      temp.innerHTML = p.description;
                      const text = temp.textContent || temp.innerText || '';
                      return text.length > 250
                        ? `<p>${text.slice(0, 250)}...</p>`
                        : `<p>${text}</p>`;
                    })()
                  }}
                />
              </div>
            ))}
          </div>

          {showCreateUserModal && (
            <CreateUserModal onClose={() => setShowCreateUserModal(false)} onUserCreated={fetchUsers} />
          )}

          {showUsersModal && (
            <UserListModal
              users={users}
              onClose={() => setShowUsersModal(false)}
              onDelete={handleDeleteUser}
              search={searchUser}
              setSearch={setSearchUser}
              onUserUpdated={fetchUsers}
              setToastMessage={setToastMessage}
              setToastType={setToastType}
            />
          )}

          {!isAdmin && !isCreator && (
            <button className="become-creator-button" onClick={handleBecomeCreator}>
              ⭐ ¡Hazte Creador!
            </button>
          )}

          {showCreateProjectModal && (
            <CreateProjectModal
              onClose={() => setShowCreateProjectModal(false)}
              onProjectCreated={fetchProyectos}
            />
          )}

          {selectedProject && (
            <ProjectDetailModal
              project={selectedProject}
              onClose={() => setSelectedProject(null)}
              onUpdated={fetchProyectos}
            />
          )}

          {showProjectListModal && (
            <ProjectListModal
              onClose={() => setShowProjectListModal(false)}
              onDelete={handleDeleteProject}
            />
          )}

          {/* Modal de confirmación de eliminación */}
          {showConfirmModal && (
            <div className="confirm-delete-modal">
              <div className="modal-content">
                <h4>¿Estás seguro de que deseas eliminar este proyecto?</h4>
                <button onClick={confirmDeleteProject} className="register-button">Aceptar</button>
                <button onClick={cancelDeleteProject} className="register-button cancel-button">Cancelar</button>
              </div>
            </div>
          )}

          <Toast
            message={toastMessage}
            type={toastType}
            onClose={() => setToastMessage('')}
          />
        </div>
      </div>
      <Footer position="left" />
    </div>
  );
};

export default Dashboard;
