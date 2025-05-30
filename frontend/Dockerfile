# Etapa 1: Build de la app con Node
FROM node:18-alpine AS builder

WORKDIR /app
COPY package*.json ./
RUN npm install

COPY . .
RUN npm run build

# Etapa 2: Servir la app con NGINX
FROM nginx:stable-alpine

# Elimina el default config
RUN rm -rf /etc/nginx/conf.d/default.conf

# Copia nuestra config personalizada
COPY nginx.conf /etc/nginx/conf.d

# Copia los archivos estáticos de React
COPY --from=builder /app/build /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
