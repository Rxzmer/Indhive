# Etapa 1: Build de la app React
FROM node:18-alpine AS builder
WORKDIR /app

# Copia package.json y package-lock.json
COPY package*.json ./

# Instala dependencias
RUN npm install

# Copia el resto del código
COPY . .

# Compila la app para producción
RUN npm run build

# Etapa 2: Servir con NGINX
FROM nginx:1.25-alpine
COPY --from=builder /app/build /usr/share/nginx/html

# Elimina la configuración por defecto y copia la personalizada (opcional)
# COPY nginx.conf /etc/nginx/conf.d/default.conf

# Expone el puerto por defecto de NGINX
EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
