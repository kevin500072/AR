# Despliegue de CognoKinet en internet

La app habla con la API Ktor; la API verifica los ID tokens de Google y guarda cuentas, clases y progreso en PostgreSQL. Render publica la API con un subdominio HTTPS y conecta la base de datos a la API dentro de su red.

## 1. Revisar y desplegar el servidor

1. Sube estos cambios al repositorio GitHub de CognoKinet.
2. Crea o inicia sesión en Render y conecta el repositorio.
3. Elige **New > Blueprint**, selecciona el repositorio y deja que Render lea `render.yaml`.
4. Revisa los recursos y sus precios antes de confirmar la creación. El archivo configura un servicio API `0.5c-512mb` y PostgreSQL `0.1c-256mb`; son recursos alojados, no un servidor en tu PC.
5. Render creará la API, la base PostgreSQL, generará `JWT_SECRET` y conectará el servicio a la base. Espera a que el deploy quede **Live**.
6. Abre `https://<nombre-del-servicio>.onrender.com/health`. Debe responder `{"status":"ok"}`. La URL pública de Render ya usa HTTPS; no abras puertos del router.

El cliente OAuth de Google usado por el servidor está configurado como `GOOGLE_WEB_CLIENT_ID`. En Google Auth Platform, agrega las cuentas que probarán la app como usuarios de prueba mientras el consentimiento OAuth siga en modo de pruebas. Antes de distribuirla públicamente, termina la configuración/verificación del consentimiento según corresponda.

## 2. Apuntar la app Android a la API pública

Copia la URL HTTPS real del servicio y configúrala localmente en Gradle, sin editar ni volver a subir el código:

- En Windows, abre o crea `%USERPROFILE%\\.gradle\\gradle.properties`.
- Añade una línea, reemplazando el dominio con el que muestra Render:
  `cognokinetApiBaseUrl=https://<nombre-del-servicio>.onrender.com`
- Sincroniza Gradle y vuelve a compilar/instalar la app.

También se admite la variable de entorno `COGNOKINET_API_BASE_URL`. El valor Gradle `cognokinetApiBaseUrl` tiene prioridad. Si no configuras ninguno, se conserva la URL LAN de desarrollo `http://192.168.100.33:8080`.

## Datos locales existentes

La base SQLite local no se copia ni se sincroniza automáticamente al crear PostgreSQL. Los perfiles, clases y progresos guardados localmente seguirán solo en ese archivo. Si ya contienen datos que deban conservarse, hay que migrarlos de forma controlada antes de que los usuarios cambien al servidor alojado. El archivo de base de datos local está excluido del contexto Docker y de Git.

## Variables del servidor

Render configura `PORT` para el servidor. `JWT_SECRET` lo genera Render; `DATABASE_URL`, `DB_USER` y `DB_PASSWORD` se enlazan a PostgreSQL. No publiques secretos, credenciales de base ni archivos de claves en GitHub.

Render administra el certificado HTTPS y mantiene API y base en línea según los planes seleccionados. Revisa límites, respaldo/retención y precio vigentes en Render antes de usarlo con datos reales de alumnos.
