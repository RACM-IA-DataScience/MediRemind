# 📋 FICHA TÉCNICA DE SOFTWARE: MEDIREMIND

---

## 1. Información General del Producto

| Parámetro | Detalle |
| :--- | :--- |
| **Nombre del Software** | **MediRemind** |
| **Versión Actual** | `1.0.0` (Compilación Release) |
| **Tipo de Aplicación** | Aplicación Móvil Nativa Android |
| **Categoría** | Salud y Bienestar / Medicina / Asistencia al Paciente |
| **Lenguaje de Programación** | Kotlin (v1.9.23) |
| **Framework de Interfaz de Usuario** | Jetpack Compose con Material Design 3 |
| **Patrón de Arquitectura** | Clean Architecture + MVVM (Model-View-ViewModel) + Flujo Unidireccional de Datos (UDF) |
| **Licencia** | MIT License |
| **Repositorio** | Repositorio Oficial en GitHub |

---

## 2. Requerimientos de Plataforma y Hardware

### 2.1 Requerimientos de Software (Sistema Operativo)
- **Versión Mínima de Android (minSdk):** Android 8.0 (API Nivel 26 - Oreo)
- **Versión Destino (targetSdk):** Android 14 (API Nivel 34 - Upside Down Cake)
- **Versión de Compilación (compileSdk):** Android API 34
- **Entorno de Ejecución:** Android Runtime (ART) con compatibilidad Java 17

### 2.2 Requerimientos de Hardware del Dispositivo Móvil
| Componente | Requerimiento Mínimo | Requerimiento Recomendado |
| :--- | :--- | :--- |
| **Procesador (CPU)** | ARMv7 / ARM64 Quad-Core 1.5 GHz | ARM64 Octa-Core 2.0 GHz o superior |
| **Memoria RAM** | 2.0 GB RAM libre | 4.0 GB RAM o superior |
| **Almacenamiento Interno** | 100 MB de espacio disponible | 250 MB (incluyendo base de datos local y caché) |
| **Módulo GPS / Ubicación** | Antena GPS con soporte A-GPS | GNSS multifrecuencia (GPS, GLONASS, Galileo) |
| **Conectividad de Red** | Wi-Fi 802.11 b/g/n o 3G/4G | Wi-Fi 802.11 ac/ax o 4G/5G LTE |
| **Sensores y Periféricos** | Motor de vibración, altavoz multimedia | Acelerómetro, vibración háptica avanzada |

---

## 3. Stack Tecnológico y Dependencias

### 3.1 Núcleo del Sistema y UI
- **AndroidX Core KTX:** `1.12.0` — Extensiones idiomáticas de Kotlin para APIs nativas.
- **AndroidX Lifecycle & ViewModel Compose:** `2.7.0` — Manejo del ciclo de vida y persistencia de estado de interfaz ante cambios de configuración.
- **Jetpack Compose BOM:** `2024.04.01` — Gestión coordinada de versiones de Compose.
- **Material Design 3:** `androidx.compose.material3:material3` — Componentes visuales accesibles y modernos.
- **Navigation Compose:** `2.7.7` — Navegación declarativa por grafos y argumentos tipificados.

### 3.2 Persistencia y Base de Datos Local
- **Room Persistence Library:** `2.6.1` (con procesador KSP `1.9.23-1.0.20`)
  - Motor relacional SQLite embebido con mapeo seguro en tiempo de compilación.
  - Soporte reactivo con `kotlinx.coroutines.flow.Flow`.
  - Migraciones estructuradas y precarga de catálogo clínico (`PreloadedMedications`).

### 3.3 Red, Consumo de APIs y Serialización
- **Retrofit:** `2.11.0` — Cliente HTTP tipificado para comunicación con endpoints REST.
- **OkHttp:** `4.12.0` — Capa de transporte con `HttpLoggingInterceptor` y políticas de timeout de 15 segundos.
- **Gson:** `2.10.1` — Parseo de estructuras JSON complejas devueltas por la FDA.

### 3.4 Servicios de Google Play e Identidad
- **Google Play Services Location:** `21.2.0` — `FusedLocationProviderClient` y `GeofencingClient`.
- **Google Play Services Auth:** `21.1.0` — Soporte para flujos de autenticación legados y modernos.
- **AndroidX Credentials:** `1.2.2` — Credential Manager API unificada para autenticación biométrica y Google ID.
- **Google ID Identity:** `1.1.0` — Autenticación segura mediante `GetGoogleIdOption`.

### 3.5 Asincronía y Concurrencia
- **Kotlinx Coroutines Android:** `1.8.0` — Despacho estructurado en hilos (`Dispatchers.IO`, `Dispatchers.Main`).
- **Kotlinx Coroutines Play Services:** `1.8.0` — Integración de `Task<T>` con `await()`.

---

## 4. Matriz de Permisos del Sistema Operativo

| Permiso | Nivel de Protección | Justificación Técnica y Operativa |
| :--- | :--- | :--- |
| `android.permission.INTERNET` | Normal | Consulta de catálogo clínico en vivo vía API de OpenFDA y validación OAuth de Google. |
| `android.permission.ACCESS_NETWORK_STATE` | Normal | Detección previa del estado de conectividad para alternar entre catálogo local y búsqueda en línea. |
| `android.permission.POST_NOTIFICATIONS` | Peligroso (Runtime, API 33+) | Emisión de avisos visuales y audibles cuando se cumple la hora de una dosis programada. |
| `android.permission.SCHEDULE_EXACT_ALARM` | Normal / Especial | Programación de disparos de milisegundos exactos mediante `AlarmManager.setExactAndAllowWhileIdle`. |
| `android.permission.USE_EXACT_ALARM` | Normal (API 33+) | Garantía de puntualidad de alarmas médicas críticas sin descarte por optimizadores del fabricante. |
| `android.permission.VIBRATE` | Normal | Notificación táctil para usuarios con discapacidad auditiva o dispositivo en modo silencio. |
| `android.permission.WAKE_LOCK` | Normal | Activación temporal de la CPU para procesar `AlarmReceiver` mientras el dispositivo está en Doze Mode. |
| `android.permission.RECEIVE_BOOT_COMPLETED` | Normal | Reconstrucción automática de todas las alarmas en el `AlarmManager` al reiniciar el equipo. |
| `android.permission.ACCESS_FINE_LOCATION` | Peligroso (Runtime) | Posicionamiento GPS de alta precisión para el radar de farmacias y delimitación de geovallas de 150m. |
| `android.permission.ACCESS_COARSE_LOCATION` | Peligroso (Runtime) | Posicionamiento por antenas celulares/Wi-Fi como fallback de bajo consumo energético. |
| `android.permission.ACCESS_BACKGROUND_LOCATION`| Peligroso (Runtime) | Disparo pasivo de geovallas cuando el usuario arriba a su domicilio o lugar de trabajo con la app cerrada. |

---

## 5. Especificaciones de Consumo de Batería y Doze Mode

1. **Gestión de Alarmas Médicas:**
   - No se emplean servicios en primer plano continuos ni bucles de sondeo (*polling*).
   - Se utiliza `AlarmManager.setExactAndAllowWhileIdle()`, lo que permite que el dispositivo permanezca en descanso profundo (*Deep Sleep / Doze Mode*) hasta el segundo exacto de la alarma.
2. **Geolocalización Eficiente:**
   - Las coordenadas GPS se consultan bajo demanda (*on-demand*) al ingresar a la pantalla del radar.
   - Las geovallas delegan el monitoreo al hardware del sistema a través del chip de bajo consumo de Google Play Services, evitando el drenaje continuo de batería.

---

## 6. Seguridad, Privacidad y Cumplimiento

- **Almacenamiento Local Seguro:** Todos los datos médicos, tratamientos, stock e historiales permanecen exclusivamente en la base de datos SQLite local del dispositivo (`mediremind.db`), encapsulada en la sandbox privada de la app (`/data/data/com.mediremind.app/databases/`).
- **Comunicaciones Seguras:** Todas las peticiones HTTP externas (OpenFDA y Google Identity) se realizan mediante transporte cifrado **TLS 1.3 / HTTPS**.
- **Reglas de Copia de Seguridad:** Configurado mediante `data_extraction_rules.xml` y `backup_rules.xml` para proteger la información médica ante respaldos no autorizados.
- **Acceso Sin Registro Obligatorio:** Se incluye un "Modo Invitado / Offline" que respeta la privacidad de pacientes que no deseen asociar su tratamiento a cuentas de correo electrónico.
