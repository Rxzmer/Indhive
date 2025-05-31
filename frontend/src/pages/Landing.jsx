import { Link } from 'react-router-dom';
import logo from '../assets/LogoInd.png';
import background from '../assets/background.jpg';

const Landing = () => {
  return (
    <div className="landing-container">
      <div className="landing-background" />

      <div className="landing-header">
        <Link to="/login" className="nav-link">LOG IN</Link>
        <Link to="/register" className="nav-link">REGISTRATE</Link>
      </div>

      <div className="landing-columns">
        <div className="landing-left">
          <img src={logo} alt="Indhive" className="landing-logo" />
          <h2 className="slogan-type">DONDE NO TENER EXPERIENCIA ES UNA VENTAJA</h2>

          <p className="landing-paragraph">
            ¿Eres un desarrollador indie que busca artistas gráficos, músicos, productores, guionistas o actores de voz para ese videojuego que tanto trabajo y esfuerzo te está costando?
          </p>

          <p className="landing-paragraph">
            ¿O tal vez eres un creativo sin experiencia que está deseando comenzar en el cada vez más creciente mundo de los videojuegos?
          </p>

          <p className="landing-paragraph">
            Indhive nace con la misión de daros a conocer entre vosotros y facilitar la comunicación teniendo un lugar donde conectaros fácilmente para que podáis tener vuestra primera oportunidad.
          </p>

          <p className="landing-paragraph">
            No tener experiencia es un requisito fundamental en Indhive. ¿A qué esperas? Crea tu cuenta <strong>ahora</strong>.
          </p>
        </div>

        <div className="landing-right" />
      </div>
    </div>
  );
};

export default Landing;
