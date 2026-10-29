# 🔒 Política de Seguridad

La seguridad y la privacidad de los datos de salud de los pacientes son de máxima prioridad en **MediRemind**.

---

## 🛡️ Versiones Soportadas

Actualmente, solo la versión más reciente recibe parches de seguridad activos:

| Versión | Soportada |
| :--- | :--- |
| `1.0.x` | ✅ Sí |
| `< 1.0.0` | ❌ No |

---

## 🚨 Reporte de Vulnerabilidades

Si descubres una vulnerabilidad o fallo de seguridad en este proyecto, te solicitamos que **NO** abras un issue público en GitHub. En su lugar, sigue el protocolo de divulgación responsable:

1. Envía un correo electrónico a la dirección de soporte o seguridad del proyecto con el asunto:  
   `[Vulnerabilidad de Seguridad] MediRemind - Resumen Breve`.
2. Proporciona una descripción detallada que incluya:
   - Tipo de vulnerabilidad y componente afectado (ej. base de datos Room, permisos GPS, Google Sign-In).
   - Pasos para reproducir la vulnerabilidad o código de prueba de concepto (PoC).
   - Impacto potencial en la privacidad del paciente.
3. El equipo acusará recibo en un plazo no mayor a **48 horas** y mantendrá comunicación continua durante la fase de mitigación y liberación del parche.

---

## 🔐 Principios de Privacidad en MediRemind

- **Aislamiento Local (Sandbox):** Toda la información médica (fármacos, dosis, horarios y bitácora) se almacena exclusivamente en la base de datos local SQLite del dispositivo dentro de la sandbox privada de la aplicación (`/data/data/com.mediremind.app/`). Ningún dato de salud se transmite a servidores no autorizados.
- **Tránsito Cifrado:** Las consultas externas hacia la API pública de la FDA y Google Identity se realizan estrictamente bajo canales seguros **HTTPS / TLS 1.3**.
- **Control de Permisos de Ubicación:** Las coordenadas GPS solo se leen bajo demanda cuando el usuario accede explícitamente a la pestaña de radar de farmacias o cuando se cruza una geovalla registrada por el propio usuario.
