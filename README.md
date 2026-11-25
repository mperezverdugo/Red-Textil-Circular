# Red Textil Circular ♻️🧵
**Evaluación Parcial 4 — DSY1105 Desarrollo de Aplicaciones Móviles**

Plataforma móvil y microservicios para la articulación de talleres y emprendimientos de reciclaje textil, suprarreciclaje (*upcycling*) y moda sostenible en Chile.

---

## 1. Nombre del Proyecto
**Red Textil Circular** (Aplicación Móvil Jetpack Compose + Backend Microservicios Spring Boot + Consumo de API Externa Wikipedia).

---

## 2. Integrantes
* **Integrante 1:** [Matias Perez]
* **Integrante 2:** [Javier muñoz]
* **Integrante 2:** [Felipe Aviles]
* **Docente:** [Nancy Bernal]
* **Asignatura:** Desarrollo de Aplicaciones Móviles (DSY1105)

---

## 3. Funcionalidades Principales

### 📱 Aplicación Móvil (Android - Jetpack Compose)
1. **Autenticación y Roles:**
   * Soporte diferenciado para tres roles: **Emprendedora textil**, **Cliente consciente** y **Administradora de fundación**.
   * Gestión de sesión reactiva con persistencia local en SQLite (Room) y autenticación remota vía JWT con Spring Boot.
2. **Catálogo de Creaciones Textiles:**
   * CRUD completo de productos textiles (crear, listar, editar y eliminar).
   * Explicación de técnicas sustentables (upcycling, reparación, materiales recuperados).
3. **Mercado Textil para Clientes:**
   * Búsqueda en tiempo real por nombre de taller o comuna.
   * Filtros combinados por Región, Comuna y Rubro textil.
   * Vista previa de portafolio y acceso directo a chat de contacto.
4. **Mensajería Interna y Chat Bidireccional:**
   * Consulta directa por creaciones específicas vinculando el producto al chat.
   * Polling incremental en tiempo real para recepción sincronizada de mensajes.
   * Notificaciones locales y simulación interactiva para pruebas guiadas.
5. **Panel y Auditoría Administrativa:**
   * Moderación de fichas de emprendimientos (activar, ocultar, dar de baja).
   * Monitoreo de eventos, métricas regionales y automatización de correos de seguimiento.
6. **Consumo de API Externa (Wikipedia REST API):**
   * Módulo educativo completamente desacoplado que no interfiere con Room ni con los microservicios locales.
   * Extracción en vivo de conceptos fundamentales: *Reciclaje textil*, *Economía circular*, *Suprarreciclaje* y *Moda sostenible*.
   * Sanitización de HTML devuelto por la API y renderizado asíncrono de portadas vía Coil.
   * Acceso accesible desde la pestaña inferior de navegación del Cliente, banner en Explorar y tarjeta temática en el Inicio de la Emprendedora.

### 🧩 Microservicios Backend (Spring Boot 3 + Java + H2/JPA)
1. **`ms-usuarios` (Puerto 8081):**
   * Registro, login y emisión de tokens JWT seguros.
   * Directorio público y administración de fichas de emprendedoras.
2. **`ms-mercado` (Puerto 8082):**
   * Gestión del catálogo de productos y asociaciones de artesanas.
   * Bandejas de mensajería, conversaciones y trazabilidad de lecturas.

---

## 4. Endpoints Utilizados

### 🌍 API Externa (Wikipedia REST API)
* **Base URL:** `https://es.wikipedia.org/api/rest_v1/`
* **Directrices Wikimedia:** Cabecera `User-Agent: TextilCircularApp/1.0 (contacto@textilcircular.cl; https://github.com/mperezverdugo/Red-Textil-Circular)`
* **Endpoints:**
  * `GET page/summary/Reciclaje_textil`: Definición, procesos de reciclaje y reducción de huella hídrica/carbono.
  * `GET page/summary/Economía_circular`: Modelo regenerativo aplicado a la producción y consumo textil.
  * `GET page/summary/Suprarreciclaje`: Concepto de *upcycling* o transformación de descartes en bienes de mayor valor.
  * `GET page/summary/Moda_sostenible`: Diseño ético, trazabilidad y consumo responsable.

### 🧩 Microservicio `ms-usuarios` (Puerto 8081)
* **Base URL Emulador:** `http://10.0.2.2:8081/` | **Base URL Local:** `http://localhost:8081/`
* **Endpoints:**
  * `POST /auth/login`: Autentica credenciales y emite token JWT con rol.
  * `POST /auth/registro/cliente`: Registra nuevo cliente en la plataforma.
  * `POST /auth/registro/emprendedora`: Registra nueva artesana y crea su ficha base.
  * `GET /emprendedoras`: Lista y filtra artesanas por texto, región, comuna y rubro.
  * `GET /emprendedoras/{id}`: Obtiene el detalle público de una artesana.
  * `GET /admin/fichas`: (Rol ADMIN) Obtiene todas las fichas para moderación.
  * `PUT /admin/fichas/{id}/estado`: (Rol ADMIN) Modifica el estado de una ficha.

### 🧩 Microservicio `ms-mercado` (Puerto 8082)
* **Base URL Emulador:** `http://10.0.2.2:8082/` | **Base URL Local:** `http://localhost:8082/`
* **Endpoints:**
  * `GET /productos?emprendedoraId={id}`: Lista productos de una artesana.
  * `GET /productos/{id}`: Consulta detalle de un producto textil.
  * `POST /productos`: Guarda nuevo producto textil (requiere Bearer token).
  * `PUT /productos/{id}`: Edita datos de un producto (requiere Bearer token).
  * `DELETE /productos/{id}`: Elimina un producto del catálogo (requiere Bearer token).
  * `GET /conversaciones`: Lista conversaciones del usuario autenticado.
  * `POST /conversaciones`: Inicia o recupera conversación entre cliente y artesana.
  * `GET /conversaciones/{id}/mensajes?despuesDeId={id}`: Consulta incremental de mensajes para polling.
  * `POST /conversaciones/{id}/mensajes`: Envía mensaje en el chat.
  * `PUT /conversaciones/{id}/leer`: Marca mensajes como leídos.

---

## 5. Pasos para Ejecutar el Proyecto

### Requisitos Previos
* **Java Development Kit (JDK):** Versión 17 o 21 instalada.
* **Android Studio:** Ladybug, Koala o superior con Android SDK (API 34/35).
* **Conexión a Internet:** Para resolver dependencias Gradle y consultar la API de Wikipedia.

### Paso 1: Levantar los Microservicios Backend

Abre dos terminales en la carpeta raíz del proyecto:

**Terminal 1 — Microservicio de Usuarios:**
```bash
cd backend/ms-usuarios
./gradlew bootRun
```
*El servicio iniciará en `http://localhost:8081`.*

**Terminal 2 — Microservicio de Mercado:**
```bash
cd backend/ms-mercado
./gradlew bootRun
```
*El servicio iniciará en `http://localhost:8082`.*

> **Nota:** Ambos microservicios incluyen base de datos local H2 y precarga automática de datos de prueba (`DatosIniciales.java`).

### Paso 2: Ejecutar las Pruebas Unitarias

**Pruebas de la Aplicación Móvil (Android):**
```bash
./gradlew testDebugUnitTest
```
*Ejecuta más de 110 pruebas unitarias con cobertura sobre el 80% de la lógica (ViewModels, Repositorios Room, Validadores, Retrofit MockWebServer y Módulo de Wikipedia).*

**Pruebas de los Microservicios:**
```bash
cd backend/ms-usuarios && ./gradlew test
cd ../ms-mercado && ./gradlew test
```

### Paso 3: Lanzar la Aplicación Móvil en Android Studio

1. Abre la carpeta `Red-Textil-Circular-Evaluacion-2` (o la subcarpeta `android`) en **Android Studio**.
2. Verifica que el archivo `local.properties` apunte correctamente a tu SDK (`sdk.dir=/ruta/a/Android/Sdk`).
3. Sincroniza el proyecto con Gradle (**Sync Project with Gradle Files**).
4. Selecciona un emulador Android (API 26 o superior) o dispositivo físico conectado por USB.
5. Presiona **Run 'app'** (`Shift + F10`).

---

## 6. Credenciales de Prueba y Acceso a Contenidos

| Rol | Correo Electrónico | Contraseña | ¿Qué permite hacer? |
| :--- | :--- | :--- | :--- |
| **Emprendedora** | `camila.morales@textilcircular.cl` | `emprendedora123` | Gestionar catálogo, ver ficha, responder mensajes, ver educación. |
| **Cliente** | `francisca.tapia@correo.cl` | `cliente123` | Explorar mercado, ver portafolios, chatear con artesanas, aprender sobre reciclaje textil. |
| **Administradora** | `admin@textilcircular.cl` | `admin123` | Panel de control, moderación de fichas, auditoría y métricas. |

### Cómo Acceder a la Sección Educativa (API de Wikipedia)
* **Como Cliente:**
  1. En la barra de navegación inferior, presiona la pestaña **Educación** (icono de información).
  2. Alternativamente, en la pestaña **Explorar**, presiona la tarjeta destacada superior *"Aprende sobre Textil Circular"*.
* **Como Emprendedora:**
  1. En la pantalla de **Inicio**, desplázate hacia abajo y presiona la tarjeta *"Aprende sobre Textil Circular"*.
* **Interacción en Vivo:**
  * Observarás los artículos cargados directamente en tiempo real desde la API de Wikipedia (`Reciclaje textil`, `Economía circular`, `Suprarreciclaje` y `Moda sostenible`).
  * Cada tarjeta incluye imagen oficial de Wikimedia, etiqueta temática pedagógica, extracto sanitizado y el botón **"Leer artículo completo en Wikipedia"** para abrir el enlace original en el navegador.

---

## 7. Generación del APK Firmado y Llave (.jks)

El proyecto cuenta con la configuración técnica requerida en `android/app/build.gradle.kts` dentro del bloque `signingConfigs` y `buildTypes.release`:

```kotlin
signingConfigs {
    create("release") {
        storeFile = file("keystore.jks")
        storePassword = "password123"
        keyAlias = "textilcircular"
        keyPassword = "password123"
    }
}
buildTypes {
    release {
        isMinifyEnabled = false
        signingConfig = signingConfigs.getByName("release")
        ...
    }
}
```

### Ubicación de Archivos Generados
* **Llave Digital (.jks):**
  * `release/keystore.jks` (Copia en raíz para entrega directa)
  * `android/app/keystore.jks` (Archivo activo referenciado por Gradle)
* **APK Firmado Release:**
  * `release/app-release.apk` (Archivo compilado y firmado listo para distribución)
  * `android/app/build/outputs/apk/release/app-release.apk`

### Comando para Compilar el APK Firmado:
```bash
./gradlew assembleRelease
```

### Verificación de Firma con `apksigner`:
```text
Verifies
Verified using v1 scheme (JAR signing): false
Verified using v2 scheme (APK Signature Scheme v2): true
Number of signers: 1
```
*(Firma digital válida y aprobada con esquema V2).*

---
