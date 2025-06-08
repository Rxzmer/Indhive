# 🎮 PixelBridge (Indhive)

**PixelBridge** es una plataforma web pensada como un MVP funcional que conecta desarrolladores indie con artistas emergentes de distintas disciplinas interesadas en el desarrollo de videojuegos: ilustradores, músicos, actores de voz y más.

## 🚀 Objetivo

Crear un espacio accesible para talentos emergentes en la industria del videojuego, permitiendo:
- Mostrar perfiles personalizados
- Buscar colaboradores por habilidades
- Comunicarse directamente dentro de la plataforma

Todo esto con una base sólida para futuras funcionalidades como contratos digitales, valoraciones y sistemas de pago.

---

## 🛠️ Tecnologías

- **Backend:** Java + Spring Boot  
- **Frontend:** Thymeleaf + Bootstrap  
- **Base de datos:** PostgreSQL (contenedorizado con Docker)  
- **ORM:** JPA (Hibernate)  
- **Metodología:** Scrum (iterativo y documentado)

---

## 📦 Estructura del Proyecto

```plaintext
src/
├── main/
│   ├── java/com/indhive/
│   │   ├── controllers/        # Controladores REST
│   │   ├── dto/                # Data Transfer Objects
│   │   ├── models/             # Entidades JPA
│   │   ├── repositories/       # Interfaces de persistencia
│   │   ├── services/           # Lógica de negocio
│   │   └── ...                 # Otros componentes
│   └── resources/
│       ├── templates/          # Vistas Thymeleaf
│       └── application.yml     # Configuración
