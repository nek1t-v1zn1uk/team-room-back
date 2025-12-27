# TeamRoom: LMS Platform with Real-Time Video & Messaging

**TeamRoom** is a comprehensive educational platform built with **Kotlin** and **Spring Boot**. It features real-time messaging via WebSockets and deep integration with **Jitsi Meet** for high-quality video conferencing.

The project’s standout feature is a **custom Lua-based integration** for Jitsi (Prosody), allowing the backend to manage user affiliations and track meeting life cycles in real-time.

---

## 🎯 Project Overview

The core objective of **TeamRoom** is to create a unified, innovative platform for digital educational transformation. It integrates essential learning tools into a single, stable, and intuitive ecosystem, enhancing communication efficiency, material organization, and participant interaction.

### 🏗 System Architecture & Logic

The platform is designed around the concept of **Courses**, where each course serves as an autonomous learning environment:

* **Content & Assignments:** Every course hosts structured theoretical materials and an assignment system. Students can submit responses directly through the platform, while instructors have dedicated tools for reviewing and grading work.
* **Dynamic Course Feed:** A central hub that automatically aggregates all updates, including new materials, upcoming assignment deadlines, and important announcements.
* **Hierarchical Communication:**
  * **Course-Specific Chats:** Multiple dedicated chat rooms can be created within a single course for modular discussions.
  * **Global Messenger:** A full-featured standalone messaging system for direct peer-to-peer and group communication outside of specific courses.
* **Embedded Video Conferencing:** Video meetings are integrated directly into the frontend. Access control and moderator privileges (Moderator/Member roles) are dynamically managed by the backend via a custom Jitsi integration.

---

## 🚀 Key Features

* **Video Conferencing:** Fully containerized Jitsi Meet integration.
* **Custom Jitsi Controller:** A custom-built Lua module for Prosody that bridges Jitsi events with the Spring Boot backend via webhooks.
* **Real-time Messaging:** Messenger powered by WebSockets (STOMP protocol).
* **Identity & Access Management:** Comprehensive user authentication and role-based permission system.
* **Cloud Storage:** Integrated with pCloud API for handling educational materials.
* **Security:** Role-based access control and JWT-based authentication for both API and Jitsi rooms.
* **Automated Infrastructure:** Entire stack is Dockerized, featuring a pre-configured Nginx reverse proxy.
* **Cross-Platform Ready:** The REST API is designed to be client-agnostic, supporting future web, mobile, and desktop applications.

---

## 🏗 Architecture

The system consists of two main decoupled components that communicate through an Nginx Reverse Proxy and Webhooks:

1. **Backend Service:** Kotlin Spring Boot application handling logic, DB, and WebSockets.
2. **Jitsi Stack:** A cluster of Docker containers providing the video engine.

### The Lua Webhook Module (`mod_token_affiliation.lua`)

Unlike standard integrations, this project uses a custom Lua plugin for the Jitsi **Prosody** server. It intercepts XMPP events (room creation, user join/leave) and dispatches them to the Spring Boot API. This allows the backend to:

* Grant "Moderator" or "Member" rights based on JWT claims.
* Track exactly when a meeting ends to clean up resources.

---

## 🛠 Tech Stack

* **Language:** Kotlin
* **Framework:** Spring Boot (Data JPA, Web, WebSocket, Security)
* **Database:** PostgreSQL
* **Communication:** STOMP over WebSocket
* **Video:** Jitsi Meet (Docker version)
* **External APIs:** pCloud
* **DevOps:** Docker, Docker Compose, Nginx

---

## 🚦 Quick Start

### Prerequisites

* Docker & Docker Compose
* A domain name with SSL (Let's Encrypt supported) or Dynamic DNS

### Installation

1. **Clone the repository:**
```bash
git clone https://github.com/nek1t-v1zn1uk/team-room-back.git
cd team-room-back
```


2. **Configure Environment:**
   Create a `.env` file in the root directory based on `.env.example`.
3. **Deployment:**
   The project requires Jitsi to be running. Follow the detailed [Deployment Guide](./DOCUMENTATION.md) to set up the Jitsi containers and the custom Lua plugin.
4. **Launch the Backend:**
```bash
docker-compose up -d

```



The API documentation will be available at: `https://<your-domain>/swagger/index.html`

