# App Playlist Manager

API para la gestión de playlists de música, permitiendo a los usuarios crear, consultar y eliminar playlists. La aplicación integra autenticación basada en JWT y consume servicios externos como la API de Spotify para obtener información de mercados disponibles.

## Arquitectura

El proyecto sigue una arquitectura hexagonal (Puertos y Adaptadores) simplificada para separar la lógica de negocio de los detalles de infraestructura.

- **Domain**: Contiene los modelos de negocio (`Playlist`, `User`, etc.), las excepciones y las interfaces de los repositorios (puertos de salida).
- **Application**: Contiene la lógica de negocio y los casos de uso (servicios). Implementa los puertos de entrada y utiliza los puertos de salida.
- **Infrastructure**: Implementa los adaptadores para tecnologías externas.
  - **Persistence**: Adaptadores para la base de datos (JPA, H2).
  - **Web**: Controladores de la API REST (adaptadores de entrada).
  - **WebClient**: Adaptador para consumir la API de Spotify.
  - **Security**: Configuración de Spring Security y gestión de JWT.

## Tecnologías y Versiones

- **Java**: 21
- **Spring Boot**: 3.5.5
- **Spring WebFlux**: Para programación reactiva.
- **Spring Data JPA**: Para la persistencia de datos.
- **Spring Security**: Para la autenticación y autorización.
- **H2 Database**: Base de datos en memoria.
- **Lombok**: Para reducir el código boilerplate.
- **MapStruct**: Para el mapeo de DTOs y entidades.
- **JWT (jjwt)**: 0.12.6 para la generación y validación de tokens.
- **Springdoc OpenAPI**: 2.4.0 para la documentación de la API.

## Endpoints Expuestos

### Autenticación (`/api/v1/auth`)

- `POST /register`: Registra un nuevo usuario.
- `POST /login`: Autentica un usuario y devuelve un `accessToken` y `refreshToken`.
- `POST /refresh`: Refresca el `accessToken` usando un `refreshToken`.
- `POST /logout`: Invalida el `refreshToken`.
- `GET /verify-token`: Valida un `accessToken`.
- `GET /me`: Obtiene la información del usuario actual a partir del token.

### Playlists (`/lists`)

- `POST /`: Crea una nueva playlist.
- `GET /{listName}`: Obtiene una playlist por su nombre.
- `GET /`: Obtiene todas las playlists.
- `DELETE /{listName}`: Elimina una playlist por su nombre.

### Spotify (`/api/v1/spotify`)

- `GET /markets`: Obtiene la lista de mercados disponibles en Spotify.

## Documentación de la API (OpenAPI)

Una vez que la aplicación está en ejecución, puedes acceder a la documentación interactiva de la API a través de Swagger UI en la siguiente URL:

[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

## Consola de H2
La consola de H2 está disponible para inspeccionar la base de datos en memoria. Puedes acceder a ella en:

[http://localhost:8082](http://localhost:8082)

- **Controlador JDBC**: `org.h2.Driver`
- **JDBC URL**: `jdbc:h2:mem:playlist_manager_db`
- **User Name**: `sa`
- **Password**: (dejar vacío)

## Instrucciones de Uso

### 1. Autenticación

Para acceder a los endpoints protegidos, primero debes obtener un token de acceso.

1.  **Registra un usuario** (si no tienes uno) usando el endpoint `POST /api/v1/auth/register`.
2.  **Inicia sesión** con tus credenciales en `POST /api/v1/auth/login`. La respuesta incluirá un `accessToken` y un `refreshToken`.

**Ejemplo de petición de login:**

```bash
POST /api/v1/auth/login
Content-Type: application/json

{
  "username": "user1",
  "password": "password123"
}
```

**Respuesta:**

```json
{
  "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 3600
}
```

### 2. Uso del Token

Para realizar peticiones a los endpoints protegidos (como los de `/lists`), debes incluir el `accessToken` en la cabecera `Authorization` de la siguiente manera:

```bash
GET /lists
Authorization: Bearer <tu_access_token>
```

Para más detalles sobre la configuración de seguridad, los usuarios de prueba y los flujos de autenticación, consulta el fichero [SECURITY.md](SECURITY.md).

## Cómo ejecutar la aplicación

Puedes iniciar la aplicación utilizando el siguiente comando de Maven:

```bash
mvn spring-boot:run
```
