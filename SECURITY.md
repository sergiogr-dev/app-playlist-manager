# Playlist Manager - OAuth2 JWT Authentication

Esta implementación añade autenticación y autorización OAuth2 con JWT a la aplicación Playlist Manager, siguiendo las mejores prácticas de seguridad.

## 🔒 Características de Seguridad

### ✅ Implementado
- **Autenticación JWT sin servicios externos**
- **Tokens de acceso y refresh separados**
- **Gestión de roles (USER, ADMIN, MODERATOR)**
- **Endpoints protegidos por roles**
- **Manejo de errores de seguridad**
- **Validación de tokens**
- **Limpieza automática de tokens expirados**
- **CORS configurado**
- **Documentación Swagger con autenticación**

### 🛡️ Seguridad Implementada
- Encriptación de contraseñas con BCrypt (12 rounds)
- Tokens JWT firmados con HMAC-SHA512
- Tokens de refresh con expiración automática
- Revocación de tokens en logout
- Validación estricta de tokens
- Headers de seguridad configurados

## 🚀 Guía de Uso

### 1. Configuración Inicial

Las siguientes variables de entorno se pueden configurar (valores por defecto incluidos):

```yaml
# Configuración JWT
JWT_SECRET: "dGhpcyBpcyBhIHZlcnkgc2VjdXJlIGtleSBmb3IgSldUIHRva2VuIHNpZ25pbmcgYW5kIHZhbGlkYXRpb24gd2l0aCA1MTIgYml0cw=="
JWT_EXPIRATION: 3600000 # 1 hora
JWT_REFRESH_EXPIRATION: 86400000 # 24 horas
JWT_ISSUER: "app-playlist-manager"

# Configuración CORS
CORS_ALLOWED_ORIGINS: "http://localhost:3000,http://localhost:8080"
CORS_ALLOWED_METHODS: "GET,POST,PUT,DELETE,OPTIONS"
CORS_ALLOWED_HEADERS: "*"
CORS_ALLOW_CREDENTIALS: true
```

### 2. Usuarios de Prueba

El sistema incluye usuarios predefinidos para testing:

| Usuario | Email | Contraseña | Roles |
|---------|-------|------------|-------|
| admin | admin@example.com | password123 | ADMIN, USER |
| user1 | user1@example.com | password123 | USER |
| user2 | user2@example.com | password123 | USER |

### 3. Endpoints de Autenticación

#### Registro de Usuario
```bash
POST /api/v1/auth/register
Content-Type: application/json

{
  "username": "nuevo_usuario",
  "email": "nuevo@example.com",
  "password": "password123",
  "firstName": "Nombre",
  "lastName": "Apellido"
}
```

#### Login
```bash
POST /api/v1/auth/login
Content-Type: application/json

{
  "username": "admin",
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

#### Refresh Token
```bash
POST /api/v1/auth/refresh
Content-Type: application/json

{
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9..."
}
```

#### Validar Token
```bash
GET /api/v1/auth/verify-token?token=eyJhbGciOiJIUzUxMiJ9...
```

#### Información del Usuario Actual
```bash
GET /api/v1/auth/me
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
```

#### Logout
```bash
POST /api/v1/auth/logout
Content-Type: application/json

{
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9..."
}
```

### 4. Uso de Tokens en Requests

Para acceder a endpoints protegidos, incluye el token en el header Authorization:

```bash
GET /api/v1/playlists
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
```

### 5. Estructura de Permisos

#### Endpoints Públicos (sin autenticación)
- `POST /api/v1/auth/**` - Endpoints de autenticación
- `GET /api/v1/auth/verify-token` - Validación de tokens
- `/actuator/**` - Endpoints de monitoreo
- `/swagger-ui/**` - Documentación Swagger
- `/h2-console/**` - Consola H2 (solo desarrollo)

#### Endpoints de Usuario (requiere rol USER o ADMIN)
- `/api/v1/users/**` - Gestión de usuarios
- `/api/v1/playlists/**` - Gestión de playlists

#### Endpoints de Administración (requiere rol ADMIN)
- `/api/v1/admin/**` - Endpoints administrativos

## 🔧 Configuración de Desarrollo

### 1. Iniciar la Aplicación
```bash
mvn spring-boot:run
```

### 2. Acceder a Swagger UI
```
http://localhost:8080/swagger-ui.html
```

### 3. Consola H2 (Base de Datos)
```
URL: http://localhost:8080/h2-console
JDBC URL: jdbc:h2:mem:playlist_manager_db
Username: sa
Password: (vacío)
```

## 🛠️ Arquitectura de Seguridad

### Componentes Principales

1. **JwtUtil**: Generación, validación y extracción de información de tokens JWT
2. **ReactiveUserDetailsService**: Carga de información de usuarios para autenticación reactiva
3. **JwtAuthenticationManager**: Gestión de autenticación con tokens JWT
4. **JwtServerSecurityContextRepository**: Repositorio de contexto de seguridad para WebFlux
5. **SecurityConfig**: Configuración principal de Spring Security
6. **AuthenticationService**: Lógica de negocio para autenticación y autorización

### Flujo de Autenticación

1. **Login**: Usuario envía credenciales → Validación → Generación de tokens
2. **Request Protegido**: Cliente envía token → Validación → Extracción de usuario → Autorización
3. **Refresh**: Cliente envía refresh token → Validación → Nuevos tokens
4. **Logout**: Cliente envía refresh token → Revocación de tokens

### Manejo de Errores

- **401 Unauthorized**: Credenciales inválidas, token expirado o inválido
- **403 Forbidden**: Permisos insuficientes para el recurso
- **400 Bad Request**: Datos de entrada inválidos

## 🔍 Testing con Postman/cURL

### Ejemplo Completo de Flujo

1. **Registro**:
```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "password123",
    "firstName": "Test",
    "lastName": "User"
  }'
```

2. **Login**:
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123"
  }'
```

3. **Acceder a Endpoint Protegido**:
```bash
curl -X GET http://localhost:8080/api/v1/playlists \
  -H "Authorization: Bearer <tu_access_token>"
```

4. **Refresh Token**:
```bash
curl -X POST http://localhost:8080/api/v1/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "<tu_refresh_token>"
  }'
```

## 📊 Monitoreo y Logging

La aplicación incluye logging detallado para:
- Intentos de autenticación
- Validación de tokens
- Errores de seguridad
- Accesos a recursos protegidos

Los logs se pueden encontrar en la consola de la aplicación y incluyen:
- Nivel INFO: Operaciones exitosas
- Nivel ERROR: Errores de autenticación y autorización
- Nivel DEBUG: Información detallada de depuración

## 🔐 Consideraciones de Seguridad

1. **En Producción**:
   - Cambiar el JWT_SECRET por uno más seguro
   - Configurar HTTPS
   - Ajustar tiempos de expiración según necesidades
   - Configurar CORS específicamente para dominios permitidos

2. **Buenas Prácticas Implementadas**:
   - Contraseñas encriptadas con BCrypt
   - Tokens con expiración
   - Separación de access y refresh tokens
   - Validación estricta de entrada
   - Manejo seguro de errores (no exposición de información sensible)
   - Limpieza automática de tokens expirados

## 🚨 Troubleshooting

### Problemas Comunes

1. **Token Expirado**: Usar refresh token para obtener nuevo access token
2. **CORS Error**: Verificar configuración de orígenes permitidos
3. **401 en Swagger**: Usar el botón "Authorize" en Swagger UI con "Bearer <token>"
4. **Base de Datos**: Reiniciar aplicación para resetear datos de prueba

### Logs Útiles

```bash
# Ver logs de autenticación
grep "Authentication" logs/app.log

# Ver errores de token
grep "Token" logs/app.log

# Ver accesos denegados
grep "Access denied" logs/app.log
```

¡La implementación OAuth2 JWT está lista para usar! 🎉
