# 📋 ESPECIFICACIÓN DE REQUERIMIENTOS DE SOFTWARE (SRS)
## Proyecto: MediRemind

---

## 1. Requerimientos Funcionales (RF)

| Código | Requerimiento Funcional | Descripción Detallada | Criterio de Aceptación | Componente Responsable |
| :--- | :--- | :--- | :--- | :--- |
| **RF-01** | **Autenticación con Google** | El sistema debe permitir al usuario iniciar sesión mediante su cuenta de Google utilizando la API moderna de Credential Manager. | El usuario visualiza su nombre, correo y foto de perfil tras autenticarse con éxito. | `AuthRepository.kt`, `LoginScreen.kt` |
| **RF-02** | **Modo Invitado / Offline** | El sistema debe posibilitar el uso total de la app sin requerir conexión a internet ni cuenta de Google vinculada. | Se puede acceder a todas las funciones en modo local sin bloqueos de inicio de sesión. | `AuthRepository.kt` |
| **RF-03** | **Alarmas de Precisión Exacta** | El sistema debe programar alarmas de milisegundos exactos que despierten el dispositivo aun en reposo (*Doze Mode*). | La alarma suena en el minuto exacto programado mediante `AlarmManager.setExactAndAllowWhileIdle`. | `NotificationHelper.kt`, `AlarmReceiver.kt` |
| **RF-04** | **Canales de Notificación** | El sistema debe registrar canales de notificación con sonido de alarma y vibración habilitados en Android 8+. | La notificación se muestra en la pantalla de bloqueo y activa el timbre configurado. | `NotificationHelper.kt` |
| **RF-05** | **Acciones en Notificación** | La notificación debe incorporar botones interactivos directos para "Tomar" y "Posponer 10m". | El usuario puede presionar "Tomar" y el stock se descuenta sin necesidad de abrir la aplicación. | `NotificationActionReceiver.kt` |
| **RF-06** | **Persistencia Post-Reinicio** | El sistema debe restaurar automáticamente todas las alarmas activas al encender o reiniciar el teléfono. | Las alarmas programadas antes del apagado continúan sonando con normalidad tras encender el equipo. | `BootReceiver.kt` |
| **RF-07** | **Registro Extemporáneo de Toma** | El sistema debe permitir registrar una toma que no se realizó al sonar la alarma, capturando la hora real de ingesta. | El usuario puede seleccionar la hora y minuto exactos en que consumió el fármaco mediante `TimePickerDialog`. | `RecordDoseDialog.kt`, `ReminderRepository.kt` |
| **RF-08** | **Cálculo de Retraso de Toma** | El sistema debe comparar la hora programada de la alarma contra la hora real de toma y calcular la diferencia. | Si la diferencia supera 10 minutos, se muestra una etiqueta con el tiempo exacto de retraso (ej. `+1h 45m`). | `DoseHistoryEntity.kt`, `HistoryScreen.kt` |
| **RF-09** | **Base de Datos de Fármacos** | El sistema debe almacenar nombre comercial, principio activo, presentación, concentración y vía. | Cada registro incluye forma farmacéutica (tableta, jarabe, etc.) y compuesto químico diferenciado. | `MedicationEntity.kt`, `AppDatabase.kt` |
| **RF-10** | **Control de Inventario y Alertas** | El sistema debe descontar existencias por cada toma y alertar cuando el stock sea menor o igual al umbral. | Se muestra un banner preventivo de color rojo cuando el stock es $\le 5$ unidades. | `MedicationDao.kt`, `HomeScreen.kt` |
| **RF-11** | **Catálogo Internacional OpenFDA** | El sistema debe permitir buscar medicamentos en tiempo real en la base de datos nacional NDC de la FDA. | Al buscar un compuesto o marca, se listan los resultados oficiales con opción de importación directa. | `OpenFdaApiService.kt`, `DrugSearchScreen.kt` |
| **RF-12** | **Radar GPS de Farmacias** | El sistema debe calcular la distancia geodésica a farmacias cercanas utilizando coordenadas GPS del dispositivo. | Se listan establecimientos ordenados por proximidad métrica con botón para abrir la ruta en Google Maps. | `LocationHelper.kt`, `PharmaciesScreen.kt` |
| **RF-13** | **Geovallas Inteligentes** | El sistema debe permitir registrar perímetros ("Casa", "Trabajo") y notificar si hay tomas pendientes al ingresar. | Al cruzar el perímetro de 150m, se emite una notificación contextual de recordatorio. | `GeofenceManager.kt`, `PlacesScreen.kt` |
| **RF-14** | **Historial y Bitácora Médica** | El sistema debe mantener una bitácora inmutable de todas las dosis tomadas, pospuestas u omitidas. | Cada evento almacena fecha, hora programada, hora real, cantidad y notas clínicas del usuario. | `DoseHistoryDao.kt`, `HistoryScreen.kt` |
| **RF-15** | **Métricas de Adherencia Semanal** | El sistema debe calcular y graficar el porcentaje de adherencia terapéutica de los últimos 7 días. | Se muestra una tarjeta con el porcentaje: `(Tomadas / Total) * 100` y el desglose de cumplimiento. | `ReminderRepository.kt`, `HomeScreen.kt` |

---

## 2. Requerimientos No Funcionales (RNF)

| Código | Categoría | Requerimiento No Funcional | Métrica / Verificación |
| :--- | :--- | :--- | :--- |
| **RNF-01** | **Compatibilidad** | La aplicación debe ser compatible desde Android 8.0 (API 26) hasta Android 14 (API 34). | Verificado en emuladores y dispositivos físicos con minSdk=26 y targetSdk=34. |
| **RNF-02** | **Puntualidad** | La desviación temporal máxima para el disparo de alarmas críticas no debe superar $\pm 1$ segundo. | `AlarmManager.setExactAndAllowWhileIdle()` con soporte para `USE_EXACT_ALARM`. |
| **RNF-03** | **Eficiencia Energética**| La aplicación no debe consumir más del $1.5\%$ de la carga total de batería en un ciclo de 24 horas en reposo. | Cero servicios en primer plano continuos; geofencing delegado al procesador de bajo consumo. |
| **RNF-04** | **Disponibilidad** | El 100% de las funciones esenciales de registro, alarmas y consulta de botiquín deben operar offline. | Almacenamiento local SQLite con Room sin dependencia de servidores propietarios. |
| **RNF-05** | **Rendimiento UI** | La interfaz declarativa Jetpack Compose debe mantener una tasa de refresco constante de 60 a 120 FPS. | Uso de inmutabilidad en estados de Compose y recomposiciones dirigidas con `remember`. |
| **RNF-06** | **Seguridad** | Los datos médicos no deben ser accesibles por otras aplicaciones instaladas en el dispositivo móvil. | Base de datos SQLite alojada en el sandbox privado de Android (`MODE_PRIVATE`). |
| **RNF-07** | **Cifrado de Red** | Toda comunicación externa hacia APIs de terceros debe emplear canales cifrados TLS 1.3 / HTTPS. | OkHttpClient con validación de certificados y rechazo estricto de tráfico en texto plano. |
| **RNF-08** | **Mantenibilidad** | El código fuente debe estructurarse bajo los principios de Clean Architecture y SOLID. | Separación en capas `data`, `notification`, `location` y `ui` con ViewModels desacoplados. |
| **RNF-09** | **Usabilidad** | La interfaz debe adherirse a las directrices de diseño accesible de Material Design 3. | Contraste de color certificado WCAG AA, tipografía escalable e iconografía estandarizada. |
| **RNF-10** | **Tolerancia a Fallos** | Si la API externa de OpenFDA no está disponible, el sistema debe degradar con el catálogo local. | Manejo con bloques `Result.fold` y fallback automático a la tabla `PreloadedMedications`. |

---

## 3. Matriz de Trazabilidad: Requerimientos vs. Código Fuente

| Requerimiento | Archivos de Implementación Principal |
| :--- | :--- |
| **RF-01, RF-02** | `AuthRepository.kt`, `AuthViewModel.kt`, `LoginScreen.kt` |
| **RF-03, RF-04** | `NotificationHelper.kt`, `AlarmReceiver.kt` |
| **RF-05** | `NotificationActionReceiver.kt`, `NotificationHelper.kt` |
| **RF-06** | `BootReceiver.kt`, `AndroidManifest.xml` |
| **RF-07, RF-08** | `RecordDoseDialog.kt`, `DoseHistoryEntity.kt`, `ReminderRepository.kt`, `HistoryScreen.kt` |
| **RF-09, RF-10** | `MedicationEntity.kt`, `MedicationDao.kt`, `AddEditMedicationScreen.kt`, `MedicationListScreen.kt` |
| **RF-11** | `OpenFdaApiService.kt`, `OpenFdaDrugDto.kt`, `DrugSearchScreen.kt` |
| **RF-12, RF-13** | `LocationHelper.kt`, `GeofenceManager.kt`, `PharmaciesScreen.kt`, `PlacesScreen.kt` |
| **RF-14, RF-15** | `DoseHistoryDao.kt`, `ReminderViewModel.kt`, `HistoryScreen.kt`, `HomeScreen.kt` |
