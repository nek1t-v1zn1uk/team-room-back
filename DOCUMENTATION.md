# Deployment Guide

This guide provides step-by-step instructions for deploying the TeamRoom ecosystem, including the Spring Boot backend and the integrated Jitsi Meet stack.

## System Requirements

### Hardware

* **OS:** Linux (Ubuntu 22.04 LTS or newer recommended).
* **CPU:** Quad-core x86-64, 2.0 GHz or higher (Jitsi is resource-intensive).
* **RAM:** Minimum 8 GB (Allocated: 4GB Jitsi, 2GB JVM/Spring, 2GB OS & PostgreSQL).
* **Disk Space:** At least 50 GB of free space.

### Software & Network

* **Docker Engine:** v20.10 or newer.
* **Docker Compose:** v2.0 or newer.
* **Network:** Static IP or configured Dynamic DNS (DDNS).
* **Open Ports:** 
  * **TCP 80:** HTTP (Domain validation/Certbot).
  * **TCP 443:** HTTPS (Web traffic).
  * **UDP 10000:** Jitsi Media streams.



---

## Pre-Installation Setup

1. **Server Preparation:** Install Linux, Docker, Docker Compose, and Git.
2. **DDNS Configuration:** Register a domain name and set up a DDNS client on your server or router.
3. **Port Forwarding:** Ensure your router forwards the following ports to the server's local IP:
* `TCP 80` → `SERVER_IP:80`
* `TCP 443` → `SERVER_IP:443`
* `UDP 10000` → `SERVER_IP:10000`



---

## Installation Steps

### 1. Jitsi Meet Configuration

The system uses the stable Jitsi Docker version.

1. Clone the official Jitsi Docker repository and follow the [Quick Start guide](https://jitsi.github.io/handbook/docs/devops-guide/devops-guide-docker/#quick-start).
2. **Edit the `.env` file** in the Jitsi directory with the following variables:
    ```bash
    HTTP_PORT=8000
    HTTPS_PORT=8443
    PUBLIC_URL=https://<YOUR_DOMAIN>
    BACKEND_WEBHOOK_URL=http://host.docker.internal:8081/api/jitsi/event
    
    # Auth & Security
    ENABLE_AUTH=1
    ENABLE_GUESTS=0
    AUTH_TYPE=jwt
    JWT_APP_ID=<YOUR_APP_ID>
    JWT_APP_SECRET=<YOUR_APP_SECRET>
    JWT_TOKEN_AUTH_MODULE=token_affiliation
    
    # SSL
    ENABLE_LETSENCRYPT=1
    LETSENCRYPT_DOMAIN=<YOUR_DOMAIN>
    LETSENCRYPT_EMAIL=<YOUR_EMAIL>
    ```
3. Modify `docker-compose.yml` in the Jitsi directory:
* Add `extra_hosts: ["host.docker.internal:host-gateway"]` to the **web** and **prosody** services.
* Add `BACKEND_WEBHOOK_URL` to the **prosody** environment variables.



### 2. Custom Nginx Configuration

To route traffic to the backend and Swagger UI, create a file at `~/.jitsi-meet-cfg/web/nginx-custom/backend.conf` (or copy one from repository):
```nginx
location ^~ /swagger/ {
    proxy_pass http://host.docker.internal:8081/swagger-ui/;
    proxy_set_header Host $host;
}
location ^~ /api/ {
    proxy_pass http://host.docker.internal:8081/api/;
    proxy_set_header Host $host;
}
location ^~ /ws/ {
    proxy_pass http://host.docker.internal:8081/ws/;
    proxy_http_version 1.1;
    proxy_set_header Upgrade $http_upgrade;
    proxy_set_header Connection "upgrade";
    proxy_set_header Host $host;
}
```

### 3. Custom Lua Plugin

Create the file `~/.jitsi-meet-cfg/prosody/prosody-plugins-custom/mod_token_affiliation.lua` and paste the module code provided in the repository.

### 4. Backend Deployment

1. Clone the TeamRoom repository.
2. Create a `.env` file in the root directory (use `.env.example` as a template).
3. Launch the services:
```bash
docker compose up -d
```



---

## Verification

Access the Swagger documentation at: `https://<YOUR_DOMAIN>/swagger/index.html` to ensure the API is reachable through the Nginx proxy.
