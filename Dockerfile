# syntax=docker/dockerfile:1.2

FROM asia-southeast1-docker.pkg.dev/beehive-mgmt/app-base-containers/node:20.12-alpine AS builder

WORKDIR /opt/app

COPY package.json /opt/app
COPY package-lock.json /opt/app
COPY tsconfig.json /opt/app
COPY tsconfig.node.json /opt/app
COPY vite.config.ts /opt/app
COPY index.html /opt/app
# Shouldn't use hard coded environment variables if you want the app to work
# in more than one environment
COPY .env /opt/app
COPY public /opt/app/public
COPY src /opt/app/src

RUN npm ci --ignore-scripts

RUN npm run build

RUN ls -al /opt/app/dist

FROM nginxinc/nginx-unprivileged:bookworm

USER 0

RUN apt-get update && apt-get upgrade -y

USER 101

# Copy compiled UI assets to nginx www directory
WORKDIR /usr/share/nginx/html
COPY --from=builder /opt/app/dist .
