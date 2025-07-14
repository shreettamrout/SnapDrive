# SnapDrive - Cloud Storage Web Application

SnapDrive is a full-stack cloud storage solution inspired by Google Drive. It enables users to securely upload, manage, and share their files with intuitive folder structures and access control. Designed using modern tools like Spring Boot, MinIO, and ReactJS, SnapDrive is ideal for both personal and enterprise use.

---

## 📄 Table of Contents

* [Tech Stack](#tech-stack)
* [Features](#features)
* [System Requirements](#system-requirements)
* [Installation Guide](#installation-guide)

  * [Backend Setup](#backend-setup)
  * [Frontend Setup](#frontend-setup)
* [Environment Configuration](#environment-configuration)
* [API Endpoints](#api-endpoints)
* [Project Structure](#project-structure)
* [License](#license)
* [Acknowledgements](#acknowledgements)

---

## 🌐 Tech Stack

### Backend

* Java 21
* Spring Boot 3.x
* Spring Security with JWT
* Hibernate (JPA)
* MinIO (S3-Compatible Object Storage)
* MySQL / PostgreSQL
* Redis (Token Storage / Blacklisting)
* JavaMailSender (Email Notifications)

### Frontend

* ReactJS with Vite
* TailwindCSS
* Axios for API Integration

---

## ✨ Features

* Secure registration and login with JWT & refresh tokens
* Upload, download, and preview files
* Folder creation, deletion, and organization
* Public and private file visibility settings
* Share files with role-based access control (READ / WRITE)
* Email notifications (welcome, login alert)
* Track used storage per user
* MinIO-based object storage with secure access

---

## ⚙️ System Requirements

* Java 21+
* Maven
* Node.js v18+
* Docker (for running MinIO locally)
* MySQL / PostgreSQL
* Redis

---

## ⚡ Installation Guide

### Backend Setup

1. **Clone the Repository**

```bash
git clone https://github.com/<your-username>/snapdrive.git
cd snapdrive/backend
```

2. **Configure Database**
   Create a new database named `snapdrive`. Update the database properties in `application.yml` accordingly.

3. **Run MinIO (S3-compatible storage)**

```bash
docker run -p 9000:9000 -p 9001:9001 \
  -e MINIO_ROOT_USER=minio \
  -e MINIO_ROOT_PASSWORD=minio123 \
  -v /data:/data \
  minio/minio server /data --console-address ":9001"
```

4. **Start the Backend Server**

```bash
./mvnw spring-boot:run
```

---

### Frontend Setup

1. **Navigate to Frontend Directory**

```bash
cd ../frontend
```

2. **Install Dependencies**

```bash
npm install
```

3. **Run React App**

```bash
npm run dev
```

---

## 📝 Environment Configuration

### Backend (`application.yml`)

```yaml
minio:
  url: http://localhost:9000
  access-key: minio
  secret-key: minio123
  bucket: snapdrive-bucket
```

### Frontend (`.env`)

```
VITE_API_BASE_URL=http://localhost:8080/api
```

---

## 🔍 API Endpoints

### Authentication

* `POST /api/auth/register`
* `POST /api/auth/login`
* `POST /api/auth/refresh`
* `POST /api/auth/logout`

### File Management

* `POST /api/files/upload`
* `GET /api/files/download/{fileId}`
* `DELETE /api/files/{fileId}`
* `POST /api/files/share`
* `GET /api/files/search?query={filename}`

### Folder Management

* `POST /api/folders/create`
* `PUT /api/folders/move`
* `DELETE /api/folders/delete/{folderId}`

---

## 📁 Project Structure

```
snapdrive/
├── backend/
│   ├── src/main/java/com/snapdrive/...
│   └── src/main/resources/
├── frontend/
│   ├── src/
│   ├── public/
│   └── vite.config.js
└── README.md
```


---

## 🙏 Acknowledgements

* [Spring Boot](https://spring.io/projects/spring-boot)
* [MinIO](https://min.io/)
* [ReactJS](https://reactjs.org/)
* [TailwindCSS](https://tailwindcss.com/)

---

For questions, contributions, or feedback, feel free to create an issue or pull request in the repository.
