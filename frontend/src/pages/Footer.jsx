// Footer.jsx
import React from 'react';
import './Footer.css';
import githubIcon from '../assets/github.png'; // Ruta correcta desde la carpeta components a assets

const Footer = ({ position }) => {
  return (
    <div className={`footer ${position}`}>
      <p>© 2025 Indhive. Por Carlos González | 
        <a href="mailto:carloslaboral87@gmail.com">carloslaboral87@gmail.com</a>
      </p>
      <a href="https://github.com/Rxzmer" target="_blank" rel="noopener noreferrer">
        <img src={githubIcon} alt="GitHub" className="github-icon" />
      </a>
    </div>
  );
};

export default Footer;
