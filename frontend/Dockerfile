# BASE
FROM node:18-alpine AS base
WORKDIR /app
COPY package*.json ./
RUN npm install

# DEV STAGE
FROM base AS dev
WORKDIR /app
COPY . .
RUN npm install  # 👈 obligatorio para instalar cambios recientes (como react-quill)

ENV CHOKIDAR_USEPOLLING=true
EXPOSE 3000
CMD ["npm", "start"]

# BUILD STAGE (para producción)
FROM base AS build
WORKDIR /app
COPY . .

ARG REACT_APP_API_URL
ENV REACT_APP_API_URL=$REACT_APP_API_URL

ENV NODE_OPTIONS=--openssl-legacy-provider
RUN npm run build

# PROD STAGE (NGINX)
FROM nginx:1.25-alpine AS prod
COPY --from=build /app/build /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
