# 📝 Registro de Cambios (Changelog)

Todos los cambios notables en este proyecto serán documentados en este archivo.  
El formato está basado en [Keep a Changelog](https://keepachangelog.com/es-ES/1.0.0/) y este proyecto se adhiere a [Semantic Versioning](https://semver.org/lang/es/).

---

## [1.0.0] - 2026-10-29

### ✨ Añadido
- **Autenticación con Google**: Soporte para inicio de sesión unificado con Google Identity Services y Credential Manager (`androidx.credentials`), además de modo invitado offline y modo de prueba para emuladores.
- **Alarmas Exactas en Segundo Plano**: Disparo de alarmas en el milisegundo exacto mediante `AlarmManager.setExactAndAllowWhileIdle()` con soporte para modo Doze y Android 13/14 (`USE_EXACT_ALARM`).
- **Canal de Notificaciones de Alta Prioridad**: Alertas audibles con vibración, visualización en pantalla de bloqueo y botones de acción rápida directa (`Tomar` y `Posponer 10m`) mediante `NotificationActionReceiver`.
- **Restauración Post-Reinicio**: Receptor `BootReceiver` que reconstruye automáticamente todas las alarmas activas en el kernel de Android tras encender o reiniciar el teléfono.
- **Registro Extemporáneo de Tomas (Hora Real)**: Diálogo interactivo `RecordDoseDialog` que permite al usuario registrar tomas cuando la alarma ya sonó, capturando la hora exacta (`actionTimeMillis`), comparándola con la programada (`scheduledTimeMillis`) y calculando dinámicamente el retraso terapéutico (`+1h 45m de retraso`).
- **Base de Datos Clínica Relacional (Room SQLite)**:
  - Tablas: `medications`, `reminders`, `dose_history`, `saved_places`.
  - Campos clínicos: nombre comercial, compuesto activo, forma farmacéutica, concentración, vía de administración, inventario restante y umbral de alerta.
  - Catálogo precargado (`PreloadedMedications`) con decenas de fármacos esenciales de la OMS para uso 100% fuera de línea.
- **Integración con OpenFDA REST API**: Búsqueda en vivo de millones de fármacos en la base de datos oficial NDC de la FDA con importación de compuestos y presentaciones al botiquín local en un solo clic.
- **Geolocalización GPS y Radar de Farmacias**: Cálculo trigonométrico de distancias geodésicas (Haversine), detección de establecimientos 24 horas y enlace de navegación directo hacia Google Maps.
- **Geovallas Inteligentes (Geofencing)**: Monitoreo perimetral de bajo consumo para "Casa" y "Trabajo" (radio de 150m), con alertas preventivas al arribar si existen medicamentos pendientes.
- **Métricas e Historial de Adherencia**: Bitácora inmutable de eventos con cálculo de tasa semanal de cumplimiento (`(Tomadas / Total) * 100`).
- **Documentación de Ingeniería Completa**: Documento técnico de arquitectura, diccionario de funciones, diagramas en Mermaid y especificación de requerimientos de software en `/docs`.
