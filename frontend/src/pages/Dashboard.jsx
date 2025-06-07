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
  // Estados
  const [projects, setProjects] = useState([]);
  const [users, setUsers] = useState([]);
  const [userInfo, setUserInfo] = useState({ username: '', email: '', roles: '' });
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedProject, setSelectedProject] = useState(null);
  const [toast, setToast] = useState({ message: '', type: 'info' });
  const [modals, setModals] = useState({
    createUser: false,
    createProject: false,
    userList: false,
    projectList: false,
    confirmDelete: false
  });
  const [projectToDelete, setProjectToDelete] = useState(null);
  const [showOwnProjects, setShowOwnProjects] = useState(false);

  // Constantes derivadas
  const token = localStorage.getItem('token');
  const apiUrl = process.env.REACT_APP_API_URL;
  const isAdmin = userInfo.roles?.includes('ROLE_ADMIN');
  const isCreator = userInfo.roles?.includes('CREATOR');

  // Fetch data
  const fetchProjects = useCallback(async () => {
    try {
      const res = await fetch(`${apiUrl}/api/projects`, {
        headers: { Authorization: `Bearer ${token}` },
      });
      const data = await res.json();
      setProjects(data);
    } catch (err) {
      showToast('No se pudieron cargar los proyectos', 'error');
    }
  }, [apiUrl, token]);

  const fetchUserInfo = useCallback(async () => {
    try {
      const res = await fetch(`${apiUrl}/api/auth/me`, {
        headers: { Authorization: `Bearer ${token}` },
      });
      const data = await res.json();
      setUserInfo({ 
        username: data.username, 
        email: data.email, 
        id: data.id, 
        roles: data.roles 
      });
      await fetchProjects();
    } catch (err) {
      showToast('No se pudo cargar la información del usuario', 'error');
    }
  }, [apiUrl, token, fetchProjects]);

  useEffect(() => {
    fetchUserInfo();
  }, [fetchUserInfo]);

  // Helpers
  const showToast = (message, type = 'info') => {
    setToast({ message, type });
  };

  const toggleModal = (modalName) => {
    setModals(prev => ({ ...prev, [modalName]: !prev[modalName] }));
  };

  // Handlers
  const handleProjectUpdated = (updatedProject) => {
    setProjects(prev => 
      prev.map(p => p.id === updatedProject.id ? updatedProject : p)
    );
    setSelectedProject(updatedProject);
    showToast('Proyecto actualizado correctamente', 'success');
  };

  const handleDeleteProject = async (confirmed) => {
    if (!confirmed || !projectToDelete) {
      setProjectToDelete(null);
      toggleModal('confirmDelete');
      return;
    }

    try {
      await fetch(`${apiUrl}/api/projects/${projectToDelete}`, {
        method: 'DELETE',
        headers: { Authorization: `Bearer ${token}` },
      });
      
      setProjects(prev => prev.filter(p => p.id !== projectToDelete));
      showToast('Proyecto eliminado correctamente', 'success');
    } catch (err) {
      showToast('Error al eliminar el proyecto', 'error');
    } finally {
      setProjectToDelete(null);
      toggleModal('confirmDelete');
    }
  };

  const handleLogout = () => {
    localStorage.removeItem('token');
    window.location.href = '/';
  };

  // Filtrado de proyectos
  const filteredProjects = projects.filter(p => 
    showOwnProjects 
      ? p.ownerUsername === userInfo.username
      : p.title.toLowerCase().includes(searchTerm.toLowerCase())
  );

  return (
    <div className={`register-container ${Object.values(modals).some(Boolean) ? 'modal-open' : ''}`}>
      <div className="register-background" style={{ backgroundImage: `url(${background})` }} />

      {/* Barra superior */}
      <div className="dashboard-toolbar">
        <Link to="/" onClick={handleLogout} className="nav-link">LOG OUT</Link>
        <input
          type="text"
          placeholder="Buscar proyectos..."
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          className="register-input search-input"
        />
      </div>

      {/* Contenido principal */}
      <div className="dashboard-layout">
        {/* Sidebar */}
        <div className="dashboard-sidebar">
          <div className="dashboard-avatar">
            {userInfo.username.charAt(0).toUpperCase()}
          </div>

          <h2 className="dashboard-title">{userInfo.username.toUpperCase()}</h2>
          {isAdmin && <span className="admin-tag">Admin</span>}
          <p className="dashboard-info">{userInfo.email}</p>

          <div className="dashboard-button-group">
            <button
              onClick={() => setShowOwnProjects(!showOwnProjects)}
              className="register-button dashboard-button"
            >
              {showOwnProjects ? '[TODOS LOS PROYECTOS]' : '[MIS PROYECTOS]'}
            </button>

            <button
              onClick={() => toggleModal('createProject')}
              className="register-button dashboard-button"
            >
              CREAR PROYECTO
            </button>

            <button
              onClick={() => toggleModal('projectList')}
              className="register-button dashboard-button"
            >
              LISTAR PROYECTOS
            </button>

            {isAdmin && (
              <>
                <button
                  onClick={() => toggleModal('createUser')}
                  className="register-button dashboard-button"
                >
                  CREAR USUARIO
                </button>

                <button
                  onClick={() => {
                    toggleModal('userList');
                    fetch(`${apiUrl}/api/users`, {
                      headers: { Authorization: `Bearer ${token}` },
                    })
                      .then(res => res.json())
                      .then(setUsers)
                      .catch(() => showToast('Error al cargar usuarios', 'error'));
                  }}
                  className="register-button dashboard-button"
                >
                  VER USUARIOS
                </button>
              </>
            )}
          </div>
        </div>

        {/* Área de contenido */}
        <div className="dashboard-content">
          <div className="dashboard-header">
            <img src={logo} alt="Indhive" className="dashboard-logo" />
          </div>

          <h2 className="section-title">TABLERO DE USUARIO</h2>

          {/* Grid de proyectos */}
          <div className="projects-grid">
            {filteredProjects.map(project => (
              <div 
                key={project.id} 
                className="project-card" 
                onClick={() => setSelectedProject(project)}
              >
                {(isAdmin || project.ownerUsername === userInfo.username) && (
                  <button
                    onClick={(e) => {
                      e.stopPropagation();
                      setProjectToDelete(project.id);
                      toggleModal('confirmDelete');
                    }}
                    className="delete-project-button"
                    title="Eliminar proyecto"
                  >
                    ✕
                  </button>
                )}
                <h4>{project.title}</h4>
                <div className="project-description">
                  {project.description.replace(/<[^>]*>/g, '').substring(0, 250)}
                  {project.description.length > 250 && '...'}
                </div>
              </div>
            ))}
          </div>

          {/* Botón para hacerse creador */}
          {!isAdmin && !isCreator && (
            <button 
              className="become-creator-button"
              onClick={async () => {
                try {
                  const res = await fetch(`${apiUrl}/api/users/me/creator`, {
                    method: 'PUT',
                    headers: {
                      'Content-Type': 'application/json',
                      Authorization: `Bearer ${token}`,
                    },
                  });
                  
                  if (res.ok) {
                    const data = await res.json();
                    localStorage.setItem('token', data.token);
                    setUserInfo(prev => ({ ...prev, roles: data.roles }));
                    showToast('¡Ahora eres creador!', 'success');
                  } else {
                    throw new Error('No se pudo actualizar tu rol');
                  }
                } catch (err) {
                  showToast(err.message, 'error');
                }
              }}
            >
              ⭐ ¡Hazte Creador!
            </button>
          )}
        </div>
      </div>

      {/* Modales */}
      {modals.createUser && (
        <CreateUserModal 
          onClose={() => toggleModal('createUser')} 
          onUserCreated={() => {
            showToast('Usuario creado correctamente', 'success');
            toggleModal('createUser');
          }}
        />
      )}

      {modals.userList && (
        <UserListModal
          users={users}
          onClose={() => toggleModal('userList')}
          onDelete={async (userId) => {
            try {
              await fetch(`${apiUrl}/api/users/${userId}`, {
                method: 'DELETE',
                headers: { Authorization: `Bearer ${token}` },
              });
              setUsers(prev => prev.filter(u => u.id !== userId));
              showToast('Usuario eliminado correctamente', 'success');
            } catch (err) {
              showToast('Error al eliminar usuario', 'error');
            }
          }}
        />
      )}

      {modals.createProject && (
        <CreateProjectModal
          onClose={() => toggleModal('createProject')}
          onProjectCreated={() => {
            fetchProjects();
            showToast('Proyecto creado correctamente', 'success');
            toggleModal('createProject');
          }}
        />
      )}

      {selectedProject && (
        <ProjectDetailModal
          project={selectedProject}
          onClose={() => setSelectedProject(null)}
          onUpdated={handleProjectUpdated}
        />
      )}

      {modals.projectList && (
        <ProjectListModal
          projects={projects}
          onClose={() => toggleModal('projectList')}
          onDelete={(projectId) => {
            setProjectToDelete(projectId);
            toggleModal('confirmDelete');
          }}
        />
      )}

      {modals.confirmDelete && (
        <div className="confirm-delete-modal">
          <div className="modal-content">
            <h4>¿Estás seguro de que deseas eliminar este proyecto?</h4>
            <button 
              onClick={() => handleDeleteProject(true)} 
              className="register-button"
            >
              Aceptar
            </button>
            <button 
              onClick={() => handleDeleteProject(false)} 
              className="register-button cancel-button"
            >
              Cancelar
            </button>
          </div>
        </div>
      )}

      {/* Toast y Footer */}
      <Toast
        message={toast.message}
        type={toast.type}
        onClose={() => setToast({ message: '', type: 'info' })}
      />
      <Footer position="left" />
    </div>
  );
};

export default Dashboard;