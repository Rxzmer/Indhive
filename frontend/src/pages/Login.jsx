import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import logo from '../assets/LogoInd.png';
import background from '../assets/background.jpg';
import './Landing.css'; // Usamos mismos estilos base para mantener coherencia

const fadeUp = {
  hidden: { opacity: 0, y: 40 },
  visible: { opacity: 1, y: 0, transition: { duration: 0.8, ease: 'easeOut' } },
};

const staggerContainer = {
  hidden: {},
  visible: {
    transition: {
      staggerChildren: 0.25,
    },
  },
};

const Login = () => {
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');

  const handleSubmit = (e) => {
    e.preventDefault();
    // Ejemplo simple: login ficticio
    if (email === 'user@indihive.com' && password === '1234') {
      navigate('/dashboard');
    } else {
      setError('Credenciales incorrectas');
    }
  };

  return (
    <div className="landing-container">
      <div
        className="landing-background"
        style={{ backgroundImage: `url(${background})` }}
      />

      <div className="landing-header">
        <a href="/" className="nav-link">
          <span className="hover-bullet">•</span> VOLVER
        </a>
      </div>

      <div className="landing-columns">
        <motion.div
          className="landing-left"
          variants={staggerContainer}
          initial="hidden"
          animate="visible"
        >
          <motion.img src={logo} alt="Indhive" className="landing-logo" variants={fadeUp} />
          <motion.h2 className="landing-slogan" variants={fadeUp}>
            Inicia Sesión
          </motion.h2>

          <motion.form className="login-form" onSubmit={handleSubmit} variants={fadeUp}>
            <input
              type="email"
              placeholder="Correo electrónico"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              className="login-input"
              required
            />
            <input
              type="password"
              placeholder="Contraseña"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className="login-input"
              required
            />
            <button type="submit" className="login-button">
              Entrar
            </button>
            {error && <div className="login-error">{error}</div>}
          </motion.form>
        </motion.div>

        <div className="landing-right" />
      </div>
    </div>
  );
};

export default Login;
