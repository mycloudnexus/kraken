# syntax=docker/dockerfile:1.2

FROM docker.io/library/node:20-alpine as builder

RUN apk --no-cache update && \
    apk upgrade && \
    apk --no-cache add dumb-init && \
    rm -rf /var/cache/apk/* && \
    addgroup -g 11020 -S app && \
    adduser -S app -u 11020 -s /bin/false && \
    mkdir /opt/app

WORKDIR /opt/app

COPY kraken-app/kraken-app-portal/package.json /opt/app
COPY kraken-app/kraken-app-portal/package-lock.json /opt/app
COPY kraken-app/kraken-app-portal/tsconfig.json /opt/app
COPY kraken-app/kraken-app-portal/tsconfig.node.json /opt/app
COPY kraken-app/kraken-app-portal/vite.config.ts /opt/app
COPY kraken-app/kraken-app-portal/index.html /opt/app
COPY kraken-app/kraken-app-portal/.env.production /opt/app/.env
COPY kraken-app/kraken-app-portal/public /opt/app/public
COPY kraken-app/kraken-app-portal/src /opt/app/src



RUN npm ci --ignore-scripts
RUN npm run build

FROM docker.io/nginxinc/nginx-unprivileged:bookworm

USER 0

# Copy compiled UI assets to nginx www directory
WORKDIR /usr/share/nginx/html
COPY --from=builder /opt/app/dist .

RUN apt-get update \
    && apt-get upgrade -y \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/* \
    && chown -R 11020:11020 /usr/share/nginx/html/*

COPY kraken-app/kraken-app-portal/nginx.conf /etc/nginx/conf.d/defalt.conf
COPY kraken-app/kraken-app-portal/bin/entrypoint.sh /docker-entrypoint.d/react-entrypoint.sh

USER 11020