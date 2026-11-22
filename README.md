
## Consumo de API Externa 

Para enriquecer la experiencia de la aplicación sin interferir con los datos locales de Room ni con los microservicios propios de Spring Boot, se integró la **API REST pública de Wikipedia en español (MediaWiki REST API)**.

### Endpoint Utilizado
* **Base URL:** `https://es.wikipedia.org/api/rest_v1/`
* **Ruta:** `GET page/summary/{title}`
* **Políticas de acceso:** Cumple las directrices de Wikimedia enviando la cabecera `User-Agent: TextilCircularApp/1.0 (contacto@textilcircular.cl; https://github.com/mperezverdugo/Red-Textil-Circular)`.

### Temas Educativos Vinculados
1. **Reciclaje textil** (`Reciclaje_textil`): Conceptos clave, reducción de residuos y materias primas recuperadas.
2. **Economía circular** (`Economía_circular`): Modelo de producción y consumo regenerativo.
3. **Suprarreciclaje / Upcycling** (`Suprarreciclaje`): Aprovechamiento de descartes para crear productos de mayor valor.
4. **Moda sostenible** (`Moda_sostenible`): Diseño ético y responsabilidad socioambiental.

### Arquitectura de la Integración (Capa Educativa)
* **`remote/`**: Cliente Retrofit aislado (`ClienteRetrofitWikipedia`) y servicio tipado (`WikipediaApiService`) con DTOs seguros (`WikipediaSummaryDto`).
* **`util/`**: `LimpiadorHtml` para sanitizar etiquetas HTML (`<p>`, `<b>`, `<span>`) y entidades acentuadas devueltas por Wikipedia.
* **`repository/`**: `EducacionRepository` que orquesta la descarga, limpieza y manejo de errores/reintentos ante fallas de red.
* **`ui/`**: `EducacionViewModel` con `StateFlow` (`Cargando`, `Exito`, `Error`) y composable `SeccionEducacionWikipedia` con previews y enlace directo al artículo original mediante Intent externo.

---

## Pruebas Unitarias

El módulo de API externa cuenta con pruebas unitarias en **JUnit 4** y **Kotlinx Coroutines Test**:
* **`LimpiadorHtmlTest`**: Valida la eliminación de etiquetas HTML y la decodificación de entidades.
* **`EducacionRepositoryTest`**: Valida la transformación de DTOs a modelos de dominio y el manejo de excepciones de red con servicios simulados.
* **`EducacionViewModelTest`**: Valida las transiciones de estados reactivos (`Cargando` -> `Exito` / `Error`) y la funcionalidad de reintento.

### Ejecución de Pruebas Unitarias
```bash
./gradlew testDebugUnitTest
```

---

## Pasos para Ejecutar la Aplicación

1. Clonar el repositorio:
   ```bash
   git clone https://github.com/mperezverdugo/Red-Textil-Circular.git
   ```
2. Abrir el proyecto en **Android Studio** (Koala / Ladybug o superior).
3. Asegurarse de tener configurado **JDK 17**.
4. Sincronizar el proyecto con archivos Gradle.
5. Ejecutar en un emulador Android (API 26+) o dispositivo físico con depuración USB activada.

---