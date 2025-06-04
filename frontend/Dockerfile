FROM node:18-alpine AS base
WORKDIR /app
COPY package*.json ./
RUN npm install

# DEV STAGE
FROM base AS dev
COPY . .
ENV CHOKIDAR_USEPOLLING=true
EXPOSE 3000
CMD ["npm", "start"]

# PROD STAGE
FROM base AS build
COPY . .
ENV NODE_OPTIONS=--openssl-legacy-provider
RUN npm run build

FROM nginx:1.25-alpine AS prod
COPY --from=build /app/build /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
