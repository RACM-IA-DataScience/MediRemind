# 📐 DIAGRAMAS DE ARQUITECTURA Y FLUJOS DEL SISTEMA

Este documento contiene los diagramas técnicos modelados en **Mermaid** que describen la arquitectura, la base de datos relacional, las secuencias de ejecución y los flujos de navegación de la aplicación **MediRemind**.

---

## 1. Diagrama de Arquitectura de Capas (Clean Architecture + MVVM)

```mermaid
flowchart TD
    subgraph UI_Layer["Capa de Presentación (Jetpack Compose & ViewModels)"]
        A1["Pantallas Compose (HomeScreen, MedicationList, etc.)"]
        A2["RecordDoseDialog (Hora Real / Retraso)"]
        A3["ViewModels (Auth, Medication, Reminder, Location)"]
        A1 <--> A3
        A2 <--> A3
    end

    subgraph Domain_Repository_Layer["Capa de Repositorios (Business Logic)"]
        B1["MedicationRepository"]
        B2["ReminderRepository"]
        B3["LocationRepository"]
        B4["AuthRepository"]
    end

    subgraph Local_Data_Layer["Capa de Persistencia Local (Room SQLite)"]
        C1["AppDatabase"]
        C2["MedicationDao"]
        C3["ReminderDao"]
        C4["DoseHistoryDao"]
        C5["SavedPlaceDao"]
        C6["PreloadedMedications (Catálogo Offline)"]
    end

    subgraph Hardware_Background_Layer["Subsistema de Sistema Operativo & Hardware"]
        D1["AlarmManager (Exact & AllowWhileIdle)"]
        D2["AlarmReceiver & BootReceiver"]
        D3["NotificationActionReceiver"]
        D4["NotificationHelper & Channels"]
        D5["FusedLocationProviderClient"]
        D6["GeofencingClient & GeofenceReceiver"]
    end

    subgraph External_Services_Layer["Servicios Externos y Nube"]
        E1["API de OpenFDA (FDA NDC Drug Database)"]
        E2["Google Identity Services / Credential Manager"]
        E3["Google Maps Platform / Navigation Intent"]
    end

    A3 --> Domain_Repository_Layer
    B1 --> C2
    B1 --> E1
    B2 --> C3
    B2 --> C4
    B2 --> D1
    B3 --> C5
    B3 --> D5
    B3 --> D6
    B3 --> E3
    B4 --> E2

    C1 --> C2
    C1 --> C3
    C1 --> C4
    C1 --> C5
    C1 --> C6

    D1 --> D2
    D2 --> D4
    D4 --> D3
    D3 --> C2
    D3 --> C4
    D3 --> C3
```

---

## 2. Diagrama Entidad-Relación de la Base de Datos (Room SQLite)

```mermaid
erDiagram
    MEDICATIONS ||--o{ REMINDERS : "tiene programados"
    MEDICATIONS ||--o{ DOSE_HISTORY : "registra historial de"
    REMINDERS ||--o{ DOSE_HISTORY : "genera eventos en"

    MEDICATIONS {
        INTEGER id PK "autoGenerate = true"
        TEXT name "Nombre comercial (ej. Tempra)"
        TEXT activeCompound "Principio activo (ej. Paracetamol)"
        TEXT presentation "Tabletas, Jarabe, etc."
        TEXT concentration "Concentración (500 mg)"
        TEXT administrationRoute "Oral, Tópica, etc."
        TEXT instructions "Indicaciones médicas"
        INTEGER currentStock "Inventario físico restante"
        TEXT unit "Unidad (pastillas, ml)"
        INTEGER lowStockThreshold "Umbral de alerta (5)"
        TEXT colorHex "Color distintivo en UI"
        BOOLEAN isCustom "Creado por usuario vs catálogo"
        INTEGER createdAt "Timestamp epoch de creación"
    }

    REMINDERS {
        INTEGER id PK "autoGenerate = true"
        INTEGER medicationId FK "Ref a MEDICATIONS.id ON DELETE CASCADE"
        INTEGER hour "Hora del día (0-23)"
        INTEGER minute "Minuto de alarma (0-59)"
        TEXT daysOfWeek "Días activos (1..7)"
        REAL dosageAmount "Cantidad a ingerir (1.0)"
        BOOLEAN isEnabled "Alarma activa o en pausa"
        INTEGER snoozeMinutes "Minutos para posponer (10)"
        BOOLEAN soundEnabled "Emisión de audio"
        BOOLEAN vibrateEnabled "Emisión de vibración"
        INTEGER lastActionTimestamp "Última acción atendida"
    }

    DOSE_HISTORY {
        INTEGER id PK "autoGenerate = true"
        INTEGER medicationId "ID del medicamento tomado"
        INTEGER reminderId "ID del recordatorio (opcional)"
        TEXT medicationName "Nombre histórico"
        TEXT activeCompound "Compuesto químico"
        TEXT dosageTaken "Cantidad tomada ('1.0' o '0')"
        INTEGER scheduledTimeMillis "Hora programada original"
        INTEGER actionTimeMillis "Hora real en que se tomó"
        TEXT status "TAKEN | SKIPPED | SNOOZED"
        TEXT notes "Observaciones del paciente"
    }

    SAVED_PLACES {
        INTEGER id PK "autoGenerate = true"
        TEXT name "Nombre del lugar (Casa, Oficina)"
        TEXT address "Dirección o etiqueta"
        REAL latitude "Coordenada GPS latitud"
        REAL longitude "Coordenada GPS longitud"
        REAL radiusMeters "Radio geovalla (150m)"
        BOOLEAN notifyOnEntry "Notificar al ingresar"
        BOOLEAN notifyOnExit "Notificar al salir"
        BOOLEAN isEnabled "Monitoreo activo"
    }
```

---

## 3. Diagrama de Secuencia: Disparo de Alarma Exacta y Acción Rápida en Notificación

Este flujo describe cómo se emite la alarma en el milisegundo exacto, incluso si el dispositivo se encuentra en reposo profundo (*Doze Mode*), y cómo el paciente puede confirmar la toma directamente desde la barra de notificaciones.

```mermaid
sequenceDiagram
    autonumber
    actor Usuario
    participant OS_Alarm as "Android AlarmManager"
    participant Receiver as "AlarmReceiver"
    participant NotifHelper as "NotificationHelper"
    participant ActionReceiver as "NotificationActionReceiver"
    participant RoomDB as "Room AppDatabase"

    Note over OS_Alarm: El dispositivo está en reposo profundo (Doze Mode)
    OS_Alarm->>Receiver: RTC_WAKEUP (Hora exacta de la alarma)
    activate Receiver
    Receiver->>NotifHelper: showMedicationNotification()
    NotifHelper-->>Usuario: Muestra Notificación de Alta Prioridad con Sonido y Botones
    Receiver->>RoomDB: Reprograma siguiente ciclo para mañana (Dispatchers.IO)
    deactivate Receiver

    Usuario->>NotifHelper: Presiona botón "✅ Tomar"
    NotifHelper->>ActionReceiver: ACTION_TAKE (extra: reminderId, medicationId)
    activate ActionReceiver
    ActionReceiver->>NotifHelper: Descarta la notificación visual
    ActionReceiver->>RoomDB: medicationDao.decrementStock(medicationId, 1)
    ActionReceiver->>RoomDB: doseHistoryDao.insertHistory(scheduledTime, actionTime = NOW)
    ActionReceiver->>RoomDB: reminderDao.updateLastAction(reminderId, NOW)
    ActionReceiver-->>Usuario: Muestra Toast de confirmación de dosis registrada
    deactivate ActionReceiver
```

---

## 4. Diagrama de Secuencia: Registro Extemporáneo de Toma con Selección de Hora Real

Este flujo resuelve el requerimiento específico cuando la alarma sonó previamente (por ejemplo, a las 08:00 AM), el paciente no pudo tomar la dosis en ese momento, pero horas después (ej. 09:45 AM) registra la toma indicando la hora exacta en que ingirió el fármaco.

```mermaid
sequenceDiagram
    autonumber
    actor Usuario
    participant HomeScreen as "HomeScreen (Compose UI)"
    participant Dialog as "RecordDoseDialog"
    participant TimePicker as "TimePickerDialog (Nativo Android)"
    participant ViewModel as "ReminderViewModel"
    participant Repository as "ReminderRepository"
    participant RoomDB as "Room AppDatabase"

    Note over HomeScreen: Dosis de las 08:00 AM muestra etiqueta "Alarma sonó"
    Usuario->>HomeScreen: Toca botón "Poner Hora" en la tarjeta de dosis
    HomeScreen->>Dialog: Abre RecordDoseDialog con datos precargados
    activate Dialog
    
    Usuario->>Dialog: Toca opción "Elegir otra hora específica"
    Dialog->>TimePicker: Muestra selector de reloj
    Usuario->>TimePicker: Selecciona 09:45 AM y confirma
    TimePicker-->>Dialog: actualHour = 9, actualMinute = 45
    
    Dialog-->>Usuario: Muestra comparativa: "Alarma: 08:00 AM ➡️ Toma: 09:45 AM (+1h 45m retraso)"
    Usuario->>Dialog: Escribe nota: "Retraso por junta laboral" y pulsa "Confirmar Toma"
    
    Dialog->>ViewModel: recordManualDose(medId, remId, dose, scheduledMillis, actualMillis, notes)
    deactivate Dialog
    
    activate ViewModel
    ViewModel->>Repository: recordManualDoseTaken(...)
    activate Repository
    Repository->>RoomDB: medicationDao.decrementStock(medicationId, dose)
    Repository->>RoomDB: doseHistoryDao.insertHistory(status="TAKEN", scheduledTime, actualTime, notes)
    Repository->>RoomDB: reminderDao.updateLastAction(reminderId, actualTime)
    Repository-->>ViewModel: Retorno de éxito
    deactivate Repository
    ViewModel->>ViewModel: refreshStats() (Recalcula adherencia semanal)
    deactivate ViewModel
    
    ViewModel-->>HomeScreen: Estado actualizado: el historial muestra "+1h 45m de retraso"
```

---

## 5. Diagrama de Secuencia: Radar de Farmacias y Monitoreo de Geovallas

```mermaid
sequenceDiagram
    autonumber
    actor Usuario
    participant App as "MediRemind (PharmaciesScreen / PlacesScreen)"
    participant FusedGPS as "FusedLocationProviderClient"
    participant GeofenceAPI as "Google Play Services Geofencing"
    participant Maps as "Google Maps App (Intent Externo)"

    Usuario->>App: Abre pestaña "Farmacias"
    App->>FusedGPS: getCurrentLocation(PRIORITY_HIGH_ACCURACY)
    FusedGPS-->>App: Location (Lat: 19.4326, Lon: -99.1332)
    App->>App: Calcula distancias geodésicas (Haversine)
    App-->>Usuario: Lista farmacias ordenadas por cercanía ("350 m", "1.2 km")

    Usuario->>App: Presiona "Cómo Llegar"
    App->>Maps: startActivity(Intent("google.navigation:q=lat,lon"))
    Maps-->>Usuario: Inicia navegación paso a paso hacia la farmacia seleccionada

    Note over GeofenceAPI: El usuario se traslada y entra a su casa (Radio 150m)
    GeofenceAPI->>App: Dispara GeofenceBroadcastReceiver (GEOFENCE_TRANSITION_ENTER)
    App->>App: Verifica si existen recordatorios del día pendientes de tomar
    App-->>Usuario: Notificación: "¡Llegaste a Casa! Recuerda revisar tus medicamentos pendientes."
```

---

## 6. Diagrama de Flujo de Navegación de la Aplicación

```mermaid
flowchart TD
    Start([Inicio de la App]) --> AuthCheck{¿Usuario autenticado o Invitado?}
    
    AuthCheck -- No --> LoginScreen["Pantalla de Login\n- Iniciar con Google\n- Modo de Prueba\n- Modo Invitado"]
    LoginScreen --> HomeScreen
    
    AuthCheck -- Sí --> HomeScreen["Pantalla Principal (Dashboard)\n- Resumen de Adherencia %\n- Alertas de Stock Bajo\n- Próximas Dosis de Hoy\n- Botones: 'Tomar Ya' y 'Poner Hora'"]

    subgraph BottomNav["Barra de Navegación Inferior"]
        HomeScreen <--> MedsScreen["Mis Medicamentos\n- Buscador local\n- Chips de Presentación y Compuesto\n- Acceso a catálogo OpenFDA\n- Registrar toma manual"]
        HomeScreen <--> PharmScreen["Radar de Farmacias\n- Localización GPS\n- Distancias reales Haversine\n- Navegación directa en Google Maps"]
        HomeScreen <--> HistoryScreen["Historial y Métricas\n- Tomas completadas vs omitidas\n- Hora programada vs hora real\n- Cálculo dinámico de retraso"]
    end

    HomeScreen --> AddMedScreen["Formulario Agregar/Editar Medicina\n- Nombre, Compuesto, Presentación, Vía\n- Concentración, Stock inicial y Unidad\n- Programación de alarma integrada"]
    MedsScreen --> AddMedScreen
    MedsScreen --> OpenFdaScreen["Buscador Internacional OpenFDA\n- Conexión REST NDC en vivo\n- Importación al botiquín en 1 clic"]
    PharmScreen --> PlacesScreen["Configuración de Geovallas\n- Definir Casa / Trabajo\n- Radio perimetral (150m)\n- Alertas al llegar o salir"]
```
