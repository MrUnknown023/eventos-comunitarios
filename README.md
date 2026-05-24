# Eventos Comunitarios 🏙️

Aplicación móvil diseñada para la gestión, organización y participación en eventos comunitarios.

## 📋 Características y Estado del Proyecto

### 1. Autenticación (🛡️ Implementado)
* **Registro y Acceso:** Sistema de login mediante correo electrónico y contraseña gestionado a través de Firebase Authentication.
* **Redes Sociales:** Integración con Google Sign-In para un acceso rápido y seguro.
* **Control de Roles (RBAC):** Diferenciación de permisos entre perfil "Organizador" y "Usuario" gestionado a través de perfiles en base de datos.

### 2. Gestión de Eventos (📅 Parcialmente Implementado)
* **CRUD de Eventos (Implementado):** Los Organizadores pueden crear, actualizar y eliminar eventos (título, descripción, lugar) almacenados en tiempo real en la nube.
* Agregar ingreso de fecha en formulario para el ingreso de eventos.
* **Visualización (Implementado):** Los usuarios regulares pueden explorar la lista de eventos comunitarios de forma reactiva.
* **Participación y Notificaciones (Pendiente):** Desarrollo del sistema para que los usuarios confirmen su asistencia (RSVP) y reciban alertas sobre cambios o recordatorios.

### 3. Interacción Social (💬 Pendiente)
* **Comentarios y Calificaciones:** Módulo para que los asistentes dejen feedback valorativo en eventos finalizados.
* **Compartir:** Integración de intenciones (Intents) para compartir los eventos en redes sociales externas y mediante correo electrónico.

### 4. Historial (⏱️ Pendiente)
* **Registro de Actividad:** Vista dedicada para consultar el historial de eventos pasados y la asistencia confirmada de los usuarios.

### 5. Uso de licencias Creative Commons (⏱️ Pendiente)

### 6. Mejora visual de la app (⏱️ Pendiente)

### 7. MockUps de la UI (leer lineamientos (entregables) del proyecto) (⏱️ Pendiente)
---

## 🛠️ Tecnologías Utilizadas
* **Lenguaje:** Kotlin
* **Interfaz Gráfica:** Jetpack Compose (Arquitectura MVVM)
* **Backend as a Service:** Firebase (Authentication, Cloud Firestore)
* **Autenticación Externa:** Google Sign-In API
