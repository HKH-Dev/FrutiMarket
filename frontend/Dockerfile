# Etapa 1: compila Angular
FROM node:24-alpine AS compilacion
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci
COPY . .
RUN npx ng build

# Etapa 2: nginx sin root sirve la app y hace de proxy hacia el backend
FROM nginxinc/nginx-unprivileged:1.29-alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=compilacion /app/dist/frontend/browser /usr/share/nginx/html
EXPOSE 8080
