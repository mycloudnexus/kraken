# syntax=docker/dockerfile:1.2

FROM asia-southeast1-docker.pkg.dev/beehive-mgmt/app-base-containers/node:20.12-alpine AS builder

WORKDIR /opt/app

COPY package.json /opt/app
COPY package-lock.json /opt/app
COPY tsconfig.json /opt/app
COPY tsconfig.node.json /opt/app
COPY vite.config.ts /opt/app
COPY index.html /opt/app
COPY .env.production /opt/app/.env
COPY public /opt/app/public
COPY src /opt/app/src

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

COPY nginx.conf /etc/nginx/conf.d/defalt.conf
COPY bin/entrypoint.sh /docker-entrypoint.d/react-entrypoint.sh

USER 11020
