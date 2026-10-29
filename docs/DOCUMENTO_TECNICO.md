# 📑 DOCUMENTO TÉCNICO DE ARQUITECTURA E INGENIERÍA DE SOFTWARE
## MediRemind — Sistema Móvil Nativo para la Gestión y Adherencia Farmacológica

---

### Control del Documento
- **Proyecto:** MediRemind
- **Identificador de Versión:** 1.0.0
- **Fecha:** Octubre 2026
- **Estado:** Aprobado / Producción
- **Autoría:** Equipo de Ingeniería y Arquitectura Android

---

## 1. Introducción y Alcance del Sistema

### 1.1 Propósito
El propósito de este documento es definir formalmente la arquitectura, el diseño técnico, los componentes estructurales, los mecanismos de persistencia, las políticas de seguridad y los algoritmos operativos de la aplicación móvil **MediRemind**. Este documento sirve como especificación técnica integral para desarrolladores, auditores de software y personal de control de calidad.

### 1.2 Problema Clínico y Operativo
La falta de adherencia al tratamiento farmacológico representa una de las principales causas de complicaciones médicas crónicas a nivel global. Los pacientes a menudo:
1. Olvidan la hora exacta de la toma cuando la alarma suena en situaciones imprevistas (reuniones, desplazamientos, falta de agua inmediata).
2. Desconocen el principio activo o composición de sus fármacos genéricos frente a nombres comerciales.
3. Agotan su inventario de medicamentos sin previsión previa.
4. Carecen de un registro cronológico verídico que distinga entre la hora programada y la hora real en que se ingirió el medicamento.

### 1.3 Solución Propuesta
**MediRemind** resuelve estas problemáticas mediante:
- Alarmas exactas inmunes al modo de ahorro de batería (*Doze Mode*).
- Registro dual de marcas de tiempo (`scheduledTimeMillis` vs. `actionTimeMillis`) con cálculo automático de retrasos.
- Base de datos local precargada con catálogo farmacológico y sincronización en vivo con la API internacional de la FDA (*OpenFDA*).
- Radar GPS de farmacias cercanas y geovallas automáticas al arribar a zonas residenciales o laborales.
- Autenticación unificada con Google mediante la API moderna de *Credential Manager*.

---

## 2. Arquitectura Global del Software

La aplicación implementa una arquitectura desacoplada basada en los principios de **Clean Architecture**, el patrón de presentación **MVVM (Model-View-ViewModel)** y el **Flujo Unidireccional de Datos (UDF)** promovido por Google para Jetpack Compose.

```
┌──────────────────────────────────────────────────────────────┐
│                    CAPA DE PRESENTACIÓN                      │
│   Jetpack Compose (Material 3)  <── StateFlow / Events ──>   │
│   ViewModels (Auth, Medication, Reminder, Location)          │
└──────────────────────────────┬───────────────────────────────┘
                               │
┌──────────────────────────────▼───────────────────────────────┐
│                     CAPA DE DOMINIO Y REPOSITORIOS           │
│   MedicationRepository, ReminderRepository,                  │
│   LocationRepository, AuthRepository                         │
└──────────────┬───────────────────────────────┬───────────────┘
               │                               │
┌──────────────▼──────────────┐ ┌──────────────▼───────────────┐
│     CAPA LOCAL (ROOM DB)    │ │   SERVICIOS EXTERNOS / HARDWARE│
│   SQLite: medications,      │ │   OpenFDA REST API (Retrofit)│
│   reminders, dose_history,  │ │   AlarmManager (RTC_WAKEUP)  │
│   saved_places              │ │   FusedLocation & Geofencing │
│   PreloadedMedications      │ │   Google Credential Manager  │
└─────────────────────────────┘ └──────────────────────────────┘
```

### 2.1 Principios de Diseño Aplicados
- **Single Responsibility Principle (SRP):** Cada clase posee una única responsabilidad bien acotada (ej. `NotificationHelper` gestiona exclusivamente canales y constructores de notificaciones; `AlarmReceiver` gestiona la recepción del evento temporal).
- **Separation of Concerns (SoC):** La interfaz gráfica declarativa desconoce el origen de los datos (base de datos o red), limitándose a observar el `StateFlow` expuesto por los ViewModels.
- **Inversión de Dependencias (DIP):** Los repositorios abstraen las implementaciones directas de DAOs y clientes HTTP.

---

## 3. Especificación Técnica de Módulos

### 3.1 Módulo de Alarmas y Notificaciones de Alta Precisión
Las aplicaciones de salud requieren puntualidad estricta. Android somete los procesos en segundo plano a restricciones agresivas de energía (*Doze Mode* y *App Standby*). Para garantizar el disparo oportuno:

1. **Uso de `AlarmManager.setExactAndAllowWhileIdle()`:**
   - Permite que el sistema despierte temporalmente la CPU (`RTC_WAKEUP`) en el milisegundo exacto planificado.
   - Cuenta con soporte para el permiso especial de Android 13+ `android.permission.USE_EXACT_ALARM`.
2. **Receptores Asíncronos (`goAsync()`):**
   - El receptor `AlarmReceiver` invoca `goAsync()`, permitiendo que una corrutina en `Dispatchers.IO` reprograme el siguiente ciclo del recordatorio en SQLite sin bloquear el hilo principal ni provocar un ANR (*Application Not Responding*).
3. **Persistencia Post-Reinicio (`BootReceiver`):**
   - Las alarmas de Android son volátiles y se borran de la memoria del kernel al reiniciar el dispositivo.
   - `BootReceiver` intercepta `android.intent.action.BOOT_COMPLETED` y reconstruye todas las alarmas activas directamente desde la base de datos local.
4. **Acciones Interactivas en Notificación:**
   - Se configuran botones directos en la notificación (`PendingIntent.getBroadcast`) para `"Tomar"` y `"Posponer 10m"`, procesados por `NotificationActionReceiver`.

---

### 3.2 Módulo de Registro Extemporáneo y Análisis de Retraso
Una de las funcionalidades críticas es la capacidad de auditar la puntualidad farmacológica.

#### Algoritmo de Registro Temporal
Sea $T_{prog}$ la marca de tiempo epoch programada para la dosis (`scheduledTimeMillis`) y $T_{real}$ la marca de tiempo epoch en que el paciente confirma la toma (`actionTimeMillis`):

$$\Delta T = T_{real} - T_{prog}$$

$$\text{Retraso en minutos} = \left\lfloor \frac{\Delta T}{60 \times 1000} \right\rfloor$$

- Si $\Delta T \le 10 \text{ min}$: La dosis se clasifica internamente como **"A tiempo"** (insignia verde).
- Si $\Delta T > 10 \text{ min}$: La dosis se clasifica como **"Tomada con retraso"** (insignia ámbar), calculando dinámicamente las horas y minutos de demora (ej. `+1h 45m de retraso`).
- Si $T_{prog} = 0$: Se clasifica como **"Toma libre / manual fuera de horario"**.

El componente `RecordDoseDialog` ofrece ajuste por presets rápidos (`Justo ahora`, `-15 min`, `-30 min`) o selección milimétrica mediante `TimePickerDialog`, asegurando que la historia clínica del paciente refleje la realidad terapéutica.

---

### 3.3 Módulo de Base de Datos y Catálogo Farmacéutico

#### Persistencia Relacional con Room
Se implementó SQLite a través de la librería oficial Room `2.6.1`. El esquema incluye:
1. `medications`: Catálogo maestro de medicamentos del paciente (nombre comercial, principio activo, presentación, concentración, vía de administración, inventario actual y umbral de alerta).
2. `reminders`: Relación 1:N vinculada a `medications` con restricción de clave foránea y eliminación en cascada (`onDelete = ForeignKey.CASCADE`).
3. `dose_history`: Bitácora inmutable de eventos de toma (`TAKEN`), omisión (`SKIPPED`) y posposición (`SNOOZED`).
4. `saved_places`: Definición de zonas de interés perimetral con coordenadas geodésicas y radio.

#### Catálogo Híbrido (Offline Precargado + Online OpenFDA)
- **Modo Offline:** La base de datos incluye un callback de inicialización (`DatabaseCallback`) que inyecta fármacos esenciales (Analgésicos, Antibióticos, Antihipertensivos, Antidiabéticos, etc.) definidos en `PreloadedMedications.kt`.
- **Modo Online (OpenFDA REST API):** Mediante Retrofit se consulta el endpoint `https://api.fda.gov/drug/ndc.json`. Si el usuario busca un compuesto químico o nombre de marca internacional, el sistema obtiene el NDC oficial, la forma farmacéutica y las concentraciones activas certificadas, permitiendo su incorporación inmediata al botiquín local.

---

### 3.4 Módulo de Geolocalización y Radar de Farmacias
- **Posicionamiento GPS:** Utiliza `FusedLocationProviderClient` con el modo `Priority.PRIORITY_HIGH_ACCURACY`.
- **Cálculo de Distancia Geodésica (Fórmula del Semiverseno / Haversine):**

$$a = \sin^2\left(\frac{\Delta \phi}{2}\right) + \cos(\phi_1) \cdot \cos(\phi_2) \cdot \sin^2\left(\frac{\Delta \lambda}{2}\right)$$

$$c = 2 \cdot \text{atan2}\left(\sqrt{a}, \sqrt{1-a}\right)$$

$$d = R \cdot c \quad \text{donde } R = 6{,}371{,}000 \text{ metros}$$

- **Geovallas Perimetrales (Geofencing):** Emplea `GeofencingClient` de Google Play Services con un radio ajustable (estándar 150m). Al cruzar el perímetro de "Casa" o "Trabajo", el sistema evalúa si existen dosis pendientes del día y emite una notificación preventiva.

---

### 3.5 Módulo de Identidad y Autenticación con Google
- Implementación de **AndroidX Credential Manager** (`androidx.credentials:credentials:1.2.2`).
- Soporte para **Google ID Token Credential** con cifrado seguro de credenciales.
- Inclusión de **Modo Invitado / Offline** y **Modo de Prueba** para entornos de desarrollo y emuladores sin servicios de Google configurados.

---

## 4. Requerimientos No Funcionales y Calidad de Software

| Atributo de Calidad | Especificación | Mecanismo de Garantía |
| :--- | :--- | :--- |
| **Puntualidad de Alarmas** | Error máximo $\le 1$ segundo | `AlarmManager.setExactAndAllowWhileIdle` |
| **Disponibilidad Offline** | 100% operativo sin Internet | Base de datos local SQLite (Room) |
| **Consumo de Batería** | $< 1.5\%$ de drenaje diario en reposo | No uso de Foreground Services continuos; geovallas por hardware |
| **Tiempo de Respuesta UI** | Renderizado constante a 60/120 FPS | Compose Layouts optimizados con estados inmutables |
| **Seguridad de Datos** | Aislamiento de base de datos | Almacenamiento en sandbox privada (`MODE_PRIVATE`) |

---

## 5. Especificaciones de Compilación y Empaquetado

- **Build System:** Gradle 8.4 con Kotlin DSL (`build.gradle.kts`).
- **Procesador de Anotaciones:** Kotlin Symbol Processing (KSP) `1.9.23-1.0.20`.
- **Configuración de ProGuard/R8:** Reglas personalizadas en `app/proguard-rules.pro` para mantener anotaciones de Room y evitar ofuscación de modelos DTO de OpenFDA.
- **Comandos de Generación de Paquetes:**
  - Compilación Debug: `./gradlew assembleDebug`
  - Compilación Release optimizada: `./gradlew assembleRelease`
  - Bundle para Google Play Store: `./gradlew bundleRelease`
