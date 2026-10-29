# 🤝 Guía de Contribución a MediRemind

¡Gracias por tu interés en contribuir a **MediRemind**! Este proyecto es de código abierto y agradecemos sugerencias, corrección de errores, mejoras de documentación y nuevas funcionalidades.

---

## 📜 Código de Conducta
Al participar en este proyecto, te comprometes a fomentar un entorno abierto, respetuoso e inclusivo para todos los colaboradores, independientemente de su nivel de experiencia, género, orientación sexual, discapacidad o etnia.

---

## 🛠️ Cómo Empezar a Contribuir

1. **Haz un Fork del repositorio:**
   - Haz clic en el botón **Fork** en la esquina superior derecha de GitHub.
2. **Clona tu copia local:**
   ```bash
   git clone https://github.com/tu-usuario/App-Medicina.git
   cd App-Medicina
   ```
3. **Crea una nueva rama de trabajo (Branch):**
   Utilizamos la convención de ramas descriptivas:
   - `feature/nombre-de-la-funcionalidad` para nuevas características.
   - `fix/descripcion-del-bug` para corrección de errores.
   - `docs/mejora-en-documentacion` para ajustes de documentos técnicos.
   ```bash
   git checkout -b feature/nuevo-recordatorio-inteligente
   ```

---

## 📐 Convenciones de Código y Estilo

- **Lenguaje:** Kotlin estándar con directrices oficiales de Android (`kotlin.code.style=official`).
- **Arquitectura:** Todo cambio debe respetar el patrón **Clean Architecture + MVVM** con Jetpack Compose.
- **Nombres:**
  - Clases y Objetos: `PascalCase` (ej. `MedicationRepository`).
  - Funciones y Variables: `camelCase` (ej. `recordCustomDose`).
  - Constantes: `UPPER_SNAKE_CASE` (ej. `CHANNEL_REMINDERS_ID`).
  - Composables: `PascalCase` (ej. `RecordDoseDialog`).

---

## 💬 Convención de Mensajes de Commit (Conventional Commits)

Utilizamos el estándar de *Conventional Commits*:
- `feat: agrega selector de retraso en diálogo de toma`
- `fix: corrige desfase de milisegundos en cálculo de alarma Doze Mode`
- `docs: añade diagrama de secuencia para registro extemporáneo`
- `refactor: optimiza consulta INNER JOIN en ReminderDao`
- `test: añade pruebas unitarias para cálculo de distancias Haversine`

---

## 🚀 Proceso de Envío de Pull Request (PR)

1. Asegúrate de que el proyecto compile limpiamente sin errores:
   ```bash
   ./gradlew assembleDebug
   ```
2. Ejecuta los linters y pruebas:
   ```bash
   ./gradlew test
   ```
3. Haz push de tu rama a GitHub:
   ```bash
   git push origin feature/tu-funcionalidad
   ```
4. Abre un **Pull Request** detallando:
   - ¿Qué problema resuelve el PR?
   - ¿Qué cambios técnicos específicos se realizaron?
   - Capturas de pantalla o videos breves si modificaste elementos visuales de Compose.
