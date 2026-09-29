/**
 * Dirección base de la API REST del backend.
 *
 * @remarks
 * **Es el principal obstáculo de despliegue del frontend.** El proyecto no define carpeta de entornos ni sustitución de
 * archivos en la configuración de compilación, de modo que este valor se embarca tal cual en la compilación de producción.
 * Desplegar en cualquier entorno que no sea el de desarrollo exige editar este archivo y recompilar.
 *
 * El origen correspondiente debe figurar además en la configuración de CORS del backend.
 */
export const API_BASE = 'http://localhost:8080';
