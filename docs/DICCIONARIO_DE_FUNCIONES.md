# 📖 DICCIONARIO DE FUNCIONES Y REFERENCIA DE API: MEDIREMIND

Este documento detalla todas las entidades, interfaces, Data Access Objects (DAOs), repositorios, servicios remotos, manejadores de eventos en segundo plano y ViewModels de la aplicación **MediRemind**.

---

## Índice de Módulos
1. [Capa de Persistencia y Modelos de Datos (`data.local`)](#1-capa-de-persistencia-y-modelos-de-datos)
2. [Capa de Acceso a Datos (`data.local.dao`)](#2-capa-de-acceso-a-datos-daos)
3. [Capa de Servicios Remotos y API (`data.remote`)](#3-capa-de-servicios-remotos-y-api)
4. [Capa de Lógica de Negocio y Repositorios (`data.repository`)](#4-capa-de-repositorios)
5. [Capa de Notificaciones y Alarmas (`notification`)](#5-capa-de-notificaciones-y-alarmas)
6. [Capa de Ubicación y Geovallas (`location`)](#6-capa-de-ubicación-y-geovallas)
7. [Capa de Estado y ViewModels (`ui.viewmodels`)](#7-capa-de-viewmodels)
8. [Capa de Interfaz de Usuario y Diálogos (`ui.screens`)](#8-capa-de-interfaz-de-usuario)

---

## 1. Capa de Persistencia y Modelos de Datos

### 1.1 `MedicationEntity`
*Paquete:* `com.mediremind.app.data.local.entities`  
Representa un registro de fármaco o tratamiento en la tabla `medications`.

| Campo | Tipo | Descripción |
| :--- | :--- | :--- |
| `id` | `Long` | Clave primaria autoincremental (`autoGenerate = true`). |
| `name` | `String` | Nombre comercial o denominación de referencia (ej. "Tempra", "Motrin"). |
| `activeCompound` | `String` | Principio activo o compuesto químico (ej. "Paracetamol", "Ibuprofeno"). |
| `presentation` | `String` | Forma farmacéutica (Tabletas, Cápsulas, Jarabe, Inyección, etc.). |
| `concentration` | `String` | Dosificación unitaria (ej. "500 mg", "100 mg / 5 ml"). |
| `administrationRoute`| `String` | Vía de administración clínica ("Oral", "Inhalatoria", "Tópica", etc.). |
| `instructions` | `String` | Recomendaciones de consumo y precauciones contraindicadas. |
| `currentStock` | `Int` | Cantidad física disponible en el botiquín del paciente. |
| `unit` | `String` | Unidad métrica de conteo ("pastillas", "ml", "dosis", "disparos"). |
| `lowStockThreshold`| `Int` | Límite mínimo para detonar alertas preventivas de desabastecimiento. |
| `colorHex` | `String` | Código hexadecimal para personalización de tarjeta en UI. |
| `isCustom` | `Boolean` | `true` si fue creado manualmente por el paciente; `false` si es del catálogo. |
| `createdAt` | `Long` | Marca de tiempo epoch en milisegundos de la creación del registro. |

---

### 1.2 `ReminderEntity`
*Paquete:* `com.mediremind.app.data.local.entities`  
Define la recurrencia y configuración de alarma vinculada a un medicamento.

| Campo | Tipo | Descripción |
| :--- | :--- | :--- |
| `id` | `Long` | Identificador único autogenerado. |
| `medicationId` | `Long` | Clave foránea referenciando `MedicationEntity.id` (con `CASCADE` al borrar). |
| `hour` | `Int` | Hora del día programada en formato 24 horas (0 a 23). |
| `minute` | `Int` | Minuto programado (0 a 59). |
| `daysOfWeek` | `String` | Cadena representativa de días habilitados (ej. "1,2,3,4,5,6,7"). |
| `dosageAmount` | `Double` | Magnitud a ingerir en cada toma (ej. 1.0 pastilla, 10.0 ml). |
| `isEnabled` | `Boolean` | Bandera de activación/pausa de la alarma. |
| `snoozeMinutes` | `Int` | Intervalo de posposición por defecto (10 minutos). |
| `soundEnabled` | `Boolean` | Emisión de alerta auditiva en el canal. |
| `vibrateEnabled` | `Boolean` | Emisión de pulso de vibración. |
| `lastActionTimestamp`| `Long`| Última vez que el usuario atendió este recordatorio. |

---

### 1.3 `DoseHistoryEntity`
*Paquete:* `com.mediremind.app.data.local.entities`  
Auditoría cronológica de cada evento de dosis.

| Campo | Tipo | Descripción |
| :--- | :--- | :--- |
| `id` | `Long` | Clave primaria autoincremental. |
| `medicationId` | `Long` | ID del medicamento consumido u omitido. |
| `reminderId` | `Long?` | ID del recordatorio origen (nulo si fue una toma manual). |
| `medicationName` | `String` | Nombre histórico del medicamento. |
| `activeCompound` | `String` | Principio activo registrado. |
| `dosageTaken` | `String` | Cantidad consumida o "0" si fue omitida. |
| `scheduledTimeMillis`| `Long`| Hora precisa en que correspondía tomar la dosis según la alarma. |
| `actionTimeMillis` | `Long` | Hora real y efectiva en que el usuario realizó la ingesta. |
| `status` | `String` | Estado: `"TAKEN"` (Tomada), `"SKIPPED"` (Omitida), `"SNOOZED"` (Pospuesta). |
| `notes` | `String` | Observaciones capturadas por el paciente. |

#### Métodos de Extensión:
- `getDelayFormatted(): String?`: Compara `actionTimeMillis` contra `scheduledTimeMillis` y devuelve una cadena legible como `"+1h 45m de retraso"`, `"A tiempo"` o `"Toma fuera de horario"`.
- `isDelayed(): Boolean`: Devuelve `true` si la dosis fue consumida con más de 15 minutos de retraso.
- `getFormattedScheduledTime(): String`: Retorna la hora programada en formato `hh:mm a`.
- `getFormattedActionTime(): String`: Retorna la hora real en formato `dd/MM/yyyy • hh:mm a`.

---

### 1.4 `SavedPlaceEntity`
*Paquete:* `com.mediremind.app.data.local.entities`  
Lugares de interés perimetral para disparadores de geovalla.

| Campo | Tipo | Descripción |
| :--- | :--- | :--- |
| `id` | `Long` | Identificador del lugar. |
| `name` | `String` | Nombre asignado (ej. "Casa", "Oficina"). |
| `address` | `String` | Dirección física o etiqueta de ubicación. |
| `latitude` | `Double` | Coordenada GPS de latitud. |
| `longitude` | `Double` | Coordenada GPS de longitud. |
| `radiusMeters` | `Float` | Radio circular perimetral (valor estándar 150.0m). |
| `notifyOnEntry` | `Boolean` | Notificar cuando el usuario ingresa al perímetro. |
| `notifyOnExit` | `Boolean` | Notificar cuando el usuario sale del perímetro. |
| `isEnabled` | `Boolean` | Estado de monitoreo activo de la geovalla. |

---

## 2. Capa de Acceso a Datos (DAOs)

### 2.1 `MedicationDao`
*Paquete:* `com.mediremind.app.data.local.dao`

- `getAllMedications(): Flow<List<MedicationEntity>>`: Emite reactivamente la lista de todos los medicamentos ordenados alfabéticamente.
- `getMedicationById(id: Long): suspend MedicationEntity?`: Obtiene un medicamento por su identificador único.
- `searchMedications(query: String): Flow<List<MedicationEntity>>`: Filtra medicamentos en SQLite por coincidencia parcial en nombre comercial o principio activo.
- `getLowStockMedications(): Flow<List<MedicationEntity>>`: Devuelve reactivamente los medicamentos donde `currentStock <= lowStockThreshold`.
- `insertMedication(medication: MedicationEntity): suspend Long`: Inserta o actualiza un medicamento, retornando el ID asignado.
- `insertAll(medications: List<MedicationEntity>): suspend Unit`: Inserción masiva utilizada en la inicialización de la base de datos.
- `updateMedication(medication: MedicationEntity): suspend Unit`: Actualiza los valores de un registro existente.
- `decrementStock(medicationId: Long, amount: Int = 1): suspend Unit`: Ejecuta una consulta atómica SQL `UPDATE medications SET currentStock = MAX(0, currentStock - :amount) WHERE id = :medicationId`.
- `deleteMedication(medication: MedicationEntity): suspend Unit`: Elimina un medicamento de la base de datos.
- `getCount(): suspend Int`: Cuenta el total de medicamentos almacenados.

---

### 2.2 `ReminderDao`
*Paquete:* `com.mediremind.app.data.local.dao`

- `getAllActiveReminders(): Flow<List<ReminderEntity>>`: Obtiene todos los recordatorios con `isEnabled = 1`.
- `getRemindersForMedication(medicationId: Long): Flow<List<ReminderEntity>>`: Obtiene recordatorios asociados a un medicamento específico.
- `getActiveRemindersWithMedication(): Flow<List<ReminderWithMedication>>`: Realiza una consulta `INNER JOIN` entre `reminders` y `medications`, devolviendo los datos combinados para la lista de tomas del día.
- `getReminderWithMedicationById(reminderId: Long): suspend ReminderWithMedication?`: Busca una alarma combinada por su ID.
- `getReminderById(id: Long): suspend ReminderEntity?`: Obtiene el recordatorio directo por ID.
- `insertReminder(reminder: ReminderEntity): suspend Long`: Inserta o reemplaza un recordatorio.
- `updateReminder(reminder: ReminderEntity): suspend Unit`: Modifica un recordatorio.
- `deleteReminder(reminder: ReminderEntity): suspend Unit`: Elimina un recordatorio.
- `updateLastAction(reminderId: Long, timestamp: Long): suspend Unit`: Actualiza la marca de tiempo del último evento registrado.

---

### 2.3 `DoseHistoryDao`
*Paquete:* `com.mediremind.app.data.local.dao`

- `getAllHistory(): Flow<List<DoseHistoryEntity>>`: Emite el historial completo ordenado descendentemente por fecha/hora de acción.
- `getHistoryForMedication(medicationId: Long): Flow<List<DoseHistoryEntity>>`: Filtra el historial para un fármaco determinado.
- `getTodayHistory(startOfDayMillis: Long): Flow<List<DoseHistoryEntity>>`: Devuelve los eventos registrados desde las 00:00:00 del día en curso.
- `getTakenCountSince(sinceMillis: Long): suspend Int`: Cuenta el total de dosis con estado `"TAKEN"` a partir de una marca de tiempo.
- `getSkippedCountSince(sinceMillis: Long): suspend Int`: Cuenta el total de dosis omitidas (`"SKIPPED"`).
- `insertHistory(history: DoseHistoryEntity): suspend Long`: Registra un evento en la bitácora.
- `deleteHistory(history: DoseHistoryEntity): suspend Unit`: Elimina un registro individual.
- `clearHistory(): suspend Unit`: Vacía completamente la tabla de historial.

---

### 2.4 `SavedPlaceDao`
*Paquete:* `com.mediremind.app.data.local.dao`

- `getAllPlaces(): Flow<List<SavedPlaceEntity>>`: Retorna el catálogo completo de zonas de geovalla.
- `getActivePlacesSync(): suspend List<SavedPlaceEntity>`: Obtiene sincrónicamente las zonas activas para sincronizar con el cliente de geofencing.
- `getPlaceById(id: Long): suspend SavedPlaceEntity?`: Busca un lugar por su identificador.
- `insertPlace(place: SavedPlaceEntity): suspend Long`: Inserta una nueva zona perimetral.
- `updatePlace(place: SavedPlaceEntity): suspend Unit`: Actualiza coordenadas, radio o estado de la zona.
- `deletePlace(place: SavedPlaceEntity): suspend Unit`: Elimina un lugar.

---

## 3. Capa de Servicios Remotos y API

### 3.1 `OpenFdaApiService`
*Paquete:* `com.mediremind.app.data.remote`  
Cliente Retrofit configurado para consultar el catálogo farmacéutico oficial de Estados Unidos.

- **Endpoint Base:** `https://api.fda.gov/`
- `searchNdc(query: String, limit: Int = 15): suspend OpenFdaResponse`:
  - **URL:** `GET drug/ndc.json`
  - **Parámetros:**
    - `search`: Consulta estructurada en formato Lucene (ej. `generic_name:"Ibuprofen"+brand_name:"Advil"`).
    - `limit`: Cantidad máxima de registros por solicitud.
  - **Retorno:** Objeto `OpenFdaResponse` con la lista de `OpenFdaDrugDto`.

---

## 4. Capa de Repositorios

### 4.1 `MedicationRepository`
*Paquete:* `com.mediremind.app.data.repository`

- `getAllMedications()`: Expone el flujo reactivo de medicamentos locales.
- `getMedicationById(id: Long)`: Búsqueda individual de medicamento.
- `searchLocalMedications(query: String)`: Búsqueda en base de datos local SQLite.
- `getLowStockMedications()`: Filtrado de medicamentos con inventario bajo.
- `insertMedication(medication: MedicationEntity)`: Inserción de medicamento.
- `updateMedication(medication: MedicationEntity)`: Actualización de datos.
- `deleteMedication(medication: MedicationEntity)`: Borrado de medicamento.
- `decrementStock(medicationId: Long, amount: Int)`: Reducción de unidades físicas en stock.
- `searchOnlineDrugs(query: String): suspend Result<List<OnlineDrugItem>>`:
  - Ejecuta la búsqueda en la API de OpenFDA con mecanismo de reintento inteligente: si la búsqueda con comillas exactas falla, ejecuta un fallback de búsqueda abierta con comodín (`*`).
  - Mapea los DTOs de red a modelos UI desacoplados (`OnlineDrugItem`).

---

### 4.2 `ReminderRepository`
*Paquete:* `com.mediremind.app.data.repository`

- `getActiveReminders()`: Retorna la lista reactiva de alarmas combinadas con datos clínicos del fármaco.
- `saveReminder(reminder: ReminderEntity): suspend Long`: Guarda el recordatorio en la base de datos y, si está activo, programa la alarma exacta en el hardware del teléfono a través de `NotificationHelper.scheduleExactAlarm`.
- `toggleReminder(reminderId: Long, isEnabled: Boolean)`: Activa o desactiva la alarma, coordinando el alta o cancelación en `AlarmManager`.
- `deleteReminder(reminder: ReminderEntity)`: Cancela la alarma del sistema y elimina el registro en base de datos.
- `recordDoseTaken(item: ReminderWithMedication, actualTimeMillis: Long, notes: String)`:
  - Calcula la hora exacta de la alarma programada para el día de hoy (`scheduledTimeMillis`).
  - Registra el consumo con la hora real provista (`actualTimeMillis`).
  - Descuenta el stock en `MedicationDao`.
  - Actualiza la marca de última acción en `ReminderDao`.
- `recordManualDoseTaken(medicationId, reminderId, medicationName, activeCompound, dosageAmount, scheduledTimeMillis, actualTimeMillis, notes)`:
  - Permite registrar tomas extemporáneas o de medicamentos adicionales sin recordatorio previo.
  - Almacena las notas clínicas y la hora real en que se consumió.
- `recordDoseSkipped(item: ReminderWithMedication, reason: String)`: Registra una dosis como omitida en la bitácora con su justificación.
- `getAdherenceStats(days: Int = 7): suspend AdherenceStats`:
  - Calcula la tasa de adherencia: `percentage = (taken / (taken + skipped)) * 100`.

---

### 4.3 `LocationRepository`
*Paquete:* `com.mediremind.app.data.repository`

- `getCurrentLocation(): suspend Location?`: Invoca de manera asíncrona a `LocationHelper.getCurrentLocation` usando la API de Google Play Services.
- `getNearbyPharmacies(latitude: Double, longitude: Double): List<PharmacyItem>`: Calcula las farmacias en el radio actual usando trigonometría esférica.
- `openInGoogleMaps(latitude: Double, longitude: Double)`: Abre Google Maps o el navegador en las coordenadas especificadas con la consulta `"farmacias cercanas"`.
- `getAllPlaces()`: Expone el listado de geovallas guardadas.
- `savePlace(place: SavedPlaceEntity): suspend Long`: Almacena la zona perimetral y registra la geovalla en el cliente de Google Play Services (`GeofenceManager`).
- `updatePlace(place: SavedPlaceEntity)`: Actualiza la zona y renueva la geovalla activa.
- `deletePlace(place: SavedPlaceEntity)`: Da de baja la geovalla en el sistema y elimina el registro local.

---

### 4.4 `AuthRepository`
*Paquete:* `com.mediremind.app.data.repository`

- `currentUser: StateFlow<UserProfile?>`: Estado reactivo de la sesión del usuario.
- `signInWithGoogle(webClientId: String): suspend Result<UserProfile>`: Realiza el flujo nativo de autenticación mediante Android Credential Manager y Google ID Token Credentials.
- `signInAsGuest(customName: String)`: Inicia una sesión local fuera de línea para usuarios que no desean vincular su correo.
- `signInMockGoogle(name: String, email: String)`: Simula una cuenta de Google para entornos de emulador o pruebas de integración rápidas.
- `signOut()`: Borra las preferencias de sesión local y emite un estado de usuario nulo.

---

## 5. Capa de Notificaciones y Alarmas

### 5.1 `NotificationHelper`
*Paquete:* `com.mediremind.app.notification`  
Objeto utilitario para la administración de notificaciones del sistema.

- `createNotificationChannels(context: Context)`: Crea los canales `channel_medication_reminders` (Alta importancia, sonido de alarma, vibración) y `channel_geofence_alerts` (Importancia por defecto).
- `showMedicationNotification(context, reminderId, medicationId, medicationName, compound, dosage, presentation)`:
  - Construye la notificación interactiva de máxima prioridad (`PRIORITY_MAX`).
  - Añade la acción `✅ Tomar` vinculada a `NotificationActionReceiver.ACTION_TAKE`.
  - Añade la acción `⏰ Posponer 10m` vinculada a `NotificationActionReceiver.ACTION_SNOOZE`.
  - Configura el intent principal para abrir `MainActivity`.
- `showGeofenceNotification(context: Context, placeName: String, alertMessage: String)`: Muestra la alerta de proximidad cuando el usuario arriba o abandona un lugar clave.
- `scheduleExactAlarm(context: Context, reminder: ReminderEntity, medication: MedicationEntity)`:
  - Calcula la próxima fecha de disparo mediante `calculateNextTriggerTime`.
  - Registra una alarma precisa e inmune al modo Doze usando `AlarmManager.setExactAndAllowWhileIdle()`.
- `cancelAlarm(context: Context, reminderId: Long)`: Cancela un `PendingIntent` de alarma activo.
- `calculateNextTriggerTime(hour: Int, minute: Int): Long`: Retorna la marca epoch de la hora programada; si la hora del día ya transcurrió, suma 1 día (`DAY_OF_YEAR + 1`).

---

### 5.2 Receptores de Difusión (Broadcast Receivers)

- **`AlarmReceiver`:**
  - Se activa en el milisegundo exacto programado por `AlarmManager`.
  - Extrae los parámetros del intent y llama a `NotificationHelper.showMedicationNotification`.
  - Lanza una corrutina asíncrona (`goAsync()`) para reprogramar automáticamente la alarma para el siguiente ciclo.
- **`NotificationActionReceiver`:**
  - Procesa las acciones directas de la notificación sin abrir la aplicación.
  - Para `ACTION_TAKE`: Descarta la notificación, descuenta el stock en la base de datos, registra el evento en `DoseHistoryEntity` con `scheduledTimeMillis` y `actionTimeMillis`, y muestra un mensaje de confirmación.
  - Para `ACTION_SNOOZE`: Programa una alarma de retraso de 10 minutos y registra el estado `"SNOOZED"`.
- **`BootReceiver`:**
  - Escucha `ACTION_BOOT_COMPLETED` y `ACTION_QUICKBOOT_POWERON`.
  - Lee todas las alarmas activas en SQLite y las vuelve a registrar en `AlarmManager` para evitar omisiones tras reinicios.

---

## 6. Capa de Ubicación y Geovallas

### 6.1 `LocationHelper`
*Paquete:* `com.mediremind.app.location`

- `getCurrentLocation(context: Context): suspend Location?`: Solicita la posición geográfica mediante `FusedLocationProviderClient` con `Priority.PRIORITY_HIGH_ACCURACY`.
- `getPharmaciesNear(userLat: Double, userLon: Double): List<PharmacyItem>`: Aplica cálculos geodésicos a puntos de referencia cercanos y ordena los resultados por proximidad en metros.
- `openGoogleMapsPharmacies(context: Context, latitude: Double, longitude: Double)`: Lanza un intent implícito a `geo:<lat>,<lon>?q=farmacias+cercanas` con fallback a la URL web de Google Maps.
- `calculateDistance(lat1, lon1, lat2, lon2): Float`: Implementación matemática de la fórmula del Semiverseno (Haversine) sobre un radio terrestre de 6,371 km.
- `formatDistance(meters: Float): String`: Formatea distancias en metros (`"350 m"`) o kilómetros (`"1.4 km"`).

---

### 6.2 `GeofenceManager` y `GeofenceBroadcastReceiver`
*Paquete:* `com.mediremind.app.location`

- `registerPlaceGeofence(place: SavedPlaceEntity, onSuccess, onError)`: Registra una geovalla circular en `GeofencingClient` con transición de entrada (`GEOFENCE_TRANSITION_ENTER`) y/o salida (`GEOFENCE_TRANSITION_EXIT`).
- `removePlaceGeofence(placeId: Long)`: Elimina la geovalla identificada por su ID.
- `GeofenceBroadcastReceiver.onReceive()`: Captura la transición perimetral, consulta si el usuario tiene tratamientos activos y envía la notificación contextual.

---

## 7. Capa de ViewModels

### 7.1 `AuthViewModel`
*Paquete:* `com.mediremind.app.ui.viewmodels`

- `currentUser: StateFlow<UserProfile?>`: Flujo con el perfil autenticado.
- `uiState: StateFlow<AuthUiState>`: Estado visual (indicador de carga y mensajes de error).
- `signInWithGoogle(webClientId: String)`: Ejecuta el flujo de inicio de sesión con Google.
- `signInAsGuest(name: String)`: Inicia sesión local de invitado.
- `signInMockGoogle(name: String, email: String)`: Simula inicio de sesión.
- `signOut()`: Cierra la sesión activa.

---

### 7.2 `MedicationViewModel`
*Paquete:* `com.mediremind.app.ui.viewmodels`

- `uiState: StateFlow<MedicationUiState>`: Lista de medicamentos, alertas de stock bajo y resultados de búsqueda local/remota.
- `onSearchQueryChanged(query: String)`: Búsqueda local inmediata con debounce de 500ms para consultas en OpenFDA.
- `saveMedication(medication: MedicationEntity, onComplete: () -> Unit)`: Inserta o actualiza un medicamento.
- `deleteMedication(medication: MedicationEntity)`: Elimina el fármaco.
- `updateStock(medicationId: Long, newStock: Int)`: Ajusta manualmente las existencias.

---

### 7.3 `ReminderViewModel`
*Paquete:* `com.mediremind.app.ui.viewmodels`

- `uiState: StateFlow<ReminderUiState>`: Recordatorios del día, historial cronológico y porcentaje de adherencia.
- `saveReminder(reminder: ReminderEntity, onComplete: () -> Unit)`: Registra un nuevo horario y programa la alarma.
- `toggleReminder(reminderId: Long, isEnabled: Boolean)`: Conmuta el estado de la alarma.
- `deleteReminder(reminder: ReminderEntity)`: Elimina el recordatorio.
- `markDoseTaken(item: ReminderWithMedication, actualTimeMillis: Long, notes: String)`: Registra la dosis tomada calculando la hora programada y la hora efectiva.
- `recordManualDose(...)`: Registra tomas libres o extemporáneas con hora personalizada.
- `markDoseSkipped(item: ReminderWithMedication, reason: String)`: Marca la dosis como omitida.
- `refreshStats()`: Recalcula la adherencia semanal.

---

### 7.4 `LocationViewModel`
*Paquete:* `com.mediremind.app.ui.viewmodels`

- `uiState: StateFlow<LocationUiState>`: Coordenadas actuales, lista de farmacias y lugares guardados.
- `loadUserLocationAndPharmacies()`: Adquiere las coordenadas GPS y actualiza el radar.
- `openInGoogleMaps()`: Dispara la navegación en el mapa.
- `savePlace(place: SavedPlaceEntity)`: Guarda una nueva zona de geovalla.
- `deletePlace(place: SavedPlaceEntity)`: Elimina la zona.

---

## 8. Capa de Interfaz de Usuario y Diálogos

### 8.1 `RecordDoseDialog`
*Paquete:* `com.mediremind.app.ui.screens.home`  
Diálogo interactivo para registrar la toma de un medicamento con hora exacta.

- **Selector de Medicamento:** Permite elegir cualquier fármaco del botiquín o autoselecciona el de la alarma.
- **Comparativa de Horas:** Muestra la hora programada de la alarma vs la hora real de ingesta.
- **Cálculo de Retraso:** Etiqueta dinámica que destaca el tiempo transcurrido (ej. `⚠️ +1h 45m de retraso`).
- **Ajustes Rápidos:** Chips para marcar la hora con presets: `"Justo ahora"`, `"-15 min"`, `"-30 min"`.
- **Selector de Reloj Nativo:** Invoca `TimePickerDialog` para elegir hora y minuto exactos.
- **Notas Clínicas:** Campo de texto opcional para justificaciones o detalles de ingesta.

---

### 8.2 Pantallas Principales (Compose)

| Pantalla | Archivo | Responsabilidad Principal |
| :--- | :--- | :--- |
| **LoginScreen** | `LoginScreen.kt` | Inicio de sesión con Google Credential Manager, Modo Invitado y Modo de Prueba. |
| **HomeScreen** | `HomeScreen.kt` | Dashboard diario: resumen de adherencia, alertas de stock bajo, dosis de hoy con botones `Tomar Ya` y `Poner Hora`. |
| **MedicationListScreen** | `MedicationListScreen.kt` | Gestión del botiquín: listado con chips de presentación, stock, búsqueda y registro de tomas manuales. |
| **AddEditMedicationScreen** | `AddEditMedicationScreen.kt` | Formulario completo: nombre comercial, compuesto activo, forma farmacéutica, concentración, vía, stock y alarma inicial. |
| **DrugSearchScreen** | `DrugSearchScreen.kt` | Buscador conectado a OpenFDA NDC API con importación en un clic al botiquín. |
| **PharmaciesScreen** | `PharmaciesScreen.kt` | Radar de farmacias cercanas con cálculo de distancia en metros/km y enlace a Google Maps. |
| **PlacesScreen** | `PlacesScreen.kt` | Configuración de geovallas para Casa y Trabajo con radio en metros y alertas de entrada/salida. |
| **HistoryScreen** | `HistoryScreen.kt` | Bitácora de tomas con visualización de hora programada, hora real, retraso y métricas de efectividad. |
