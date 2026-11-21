Desarrollo de un traductor de páginas HTML a
Markdown utilizando Java
Asignatura: Desarrollo de Sistemas III
Unidad: Programación cliente-servidor y procesamiento de documentos
Modalidad: Individual
Lenguaje: Java 17 o superior
Entorno: IntelliJ IDEA
Control de versiones: Git y GitHub
Restricción: Exclusivamente clases de la API estándar de Java.
1. Propósito
   Desarrollar una aplicación Java que reciba como argumento la URL de una página web, recupere su
   contenido HTML mediante una conexión HTTP, interprete los elementos principales del documento y genere
   una representación equivalente en formato Markdown, almacenándola en un archivo local.
   La tarea integra arquitectura cliente-servidor, comunicación HTTP, procesamiento de texto, expresiones
   regulares, estructuras de datos y manejo de archivos.
2. Resultados de aprendizaje
   Al finalizar, el estudiante será capaz de:
1. Implementar un cliente HTTP mediante URL , URLConnection y HttpURLConnection .
2. Recuperar y validar contenido HTML remoto.
3. Analizar estructuras HTML utilizando herramientas estándar de Java.
4. Transformar elementos HTML a sintaxis Markdown.
5. Gestionar elementos anidados, enlaces, listas y caracteres especiales.
6. Generar archivos mediante java.nio.file .
7. Diseñar una aplicación modular, verificable y documentada.
3. Planteamiento del problema
   Muchos documentos publicados en Internet utilizan HTML. Para documentación técnica, apuntes o
   repositorios GitHub puede resultar conveniente disponer de su contenido en Markdown.
   Se requiere desarrollar una aplicación llamada HTML2Markdown que convierta el contenido principal de una
   página web en un archivo Markdown local.
   Ejecución esperada
   java HTML2Markdown https://ejemplo.org/articulo.html
   Salida predeterminada:
   articulo.md
   También deberá admitir una ruta de salida opcional:
   java HTML2Markdown https://ejemplo.org/articulo.html salida.md
   Ejemplo de conversión
   HTML de entrada:
<h1>Introducción a Java</h1>
<p>Java es un lenguaje <strong>orientado a objetos</strong>.</p>
<h2>Características</h2>
<ul>
<li>Portabilidad</li>
<li>Seguridad</li>
</ul>
<p>Visita la <a href="https://dev.java/">documentación</a>.</p>
Markdown esperado:
# Introducción a Java
Java es un lenguaje **orientado a objetos**.
## Características- Portabilidad- Seguridad
Visita la [documentación](https://dev.java/).
La salida deberá preservar, en la medida de lo posible, la estructura semántica del documento original.
ID Requisito
RF01 Recibir una URL HTTP o HTTPS como argumento obligatorio.
RF02 Recibir opcionalmente la ruta del archivo Markdown de salida.
RF03 Establecer una conexión HTTP y recuperar el HTML.
RF04 Verificar el código HTTP y el tipo de contenido.
RF05 Interpretar los elementos HTML incluidos en el alcance.
RF06 Convertir encabezados, párrafos y formato de texto.
RF07 Convertir enlaces, imágenes, listas y bloques de código.
RF08 Resolver referencias relativas a enlaces e imágenes.
RF09 Excluir contenido no pertinente, como scripts y estilos.
RF10 Guardar un archivo .md codificado en UTF-8.
RF11 Manejar errores de conexión, lectura, conversión y escritura.
RF12 Mostrar un resumen de la operación.
Elemento HTML Conversión Markdown
<h1> a <h6> # a ######
<p> Párrafo separado por una línea en blanco
<strong> , <b> **texto**
<em> , <i> *texto*
<a href="..."> [texto](URL)
<img src="..." alt="..."> ![alt](URL)
<ul> y <li> Lista con 
<ol> y <li> Lista numerada
<br> Salto de línea Markdown
4. Requisitos funcionales
5. Reglas de conversión
Elemento HTML
<blockquote>
<code>
<pre>
<hr>
<script> , <style>
Conversión Markdown
> texto
Código en línea entre acentos graves
Bloque de código delimitado por tres acentos graves--
Omitir su contenido
Se trabajará con un subconjunto definido de HTML. No se exige reproducir todas las características de un
navegador ni convertir perfectamente cualquier documento HTML de Internet.
6. Arquitectura propuesta
Divida la aplicación en componentes con responsabilidades específicas:
HTML2Markdown: 
argumentos y coordinación
WebClient: URL y 
URLConnection
HtmlParser: análisis de 
HTML
MarkdownConverter: 
reglas de transformación
MarkdownWriter: Files, 
Path y UTF-8
Estructura del proyecto
# HTML2Markdown

Aplicación de consola desarrollada en **Java 17+** utilizando **exclusivamente clases de la API estándar de Java** (`java.net`, `javax.swing.text.html`, `java.nio.file`), sin bibliotecas externas. Descarga una página web mediante HTTP/HTTPS, interpreta su estructura HTML y genera un archivo local equivalente en formato Markdown codificado en UTF-8.

## Arquitectura y estructura del proyecto

```text
HTML2Markdown/
├── README.md
├── .gitignore
├── src/
│   └── main/
│       └── java/
│           ├── HTML2Markdown.java      # Validación de argumentos y coordinación del flujo
│           ├── WebClient.java          # Cliente HTTP/HTTPS (URL, URLConnection, HttpURLConnection)
│           ├── HtmlParser.java         # Análisis estructural con ParserDelegator y ParserCallback
│           ├── MarkdownConverter.java  # Reglas de conversión, elementos anidados y URLs relativas
│           └── MarkdownWriter.java     # Escritura de archivos en UTF-8 con java.nio.file
├── tests/
│   ├── encabezados.html
│   ├── listas.html
│   ├── enlaces.html
│   ├── documento_completo.html
│   └── vacia.html
└── output/
    └── ejemplo_completo.md
Excluya output/ del control de versiones, salvo ejemplos de salida expresamente documentados.
7. Actividades de desarrollo
Actividad 1. Recuperación del documento HTML
Implemente WebClient para establecer una conexión HTTP mediante URL , URLConnection y, cuando
corresponda, HttpURLConnection .
La clase deberá:
Configurar tiempos máximos de conexión y lectura.
Verificar una respuesta HTTP exitosa.
Comprobar que el contenido sea HTML.
Identificar la codificación de caracteres; utilizar UTF-8 cuando corresponda.
Recuperar el documento completo dentro de un límite de tamaño.
Cerrar flujos y conexiones correctamente.
Método sugerido:
public String descargarHTML(URL url) throws IOException
Restricción: no se permite HttpClient ni bibliotecas HTTP externas.
Actividad 2. Análisis de la estructura HTML
Implemente un componente que identifique los elementos HTML pertinentes. Investigue la clase estándar:
javax.swing.text.html.parser.ParserDelegator
y el mecanismo de eventos:
javax.swing.text.html.HTMLEditorKit.ParserCallback
Utilice sus eventos para identificar inicio y fin de etiquetas, etiquetas vacías, texto y atributos.
Limitación técnica: ParserDelegator es un analizador heredado, basado principalmente en HTML 3.2; no
garantiza interpretar correctamente todos los documentos HTML5 modernos. Las expresiones regulares
pueden complementar el procesamiento de texto, pero no deben ser el único mecanismo para interpretar
estructuras HTML anidadas.
Actividad 3. Implementación del conversor
Implemente MarkdownConverter para aplicar las reglas definidas.
Considere especialmente:
Estructuras anidadas: preservar el orden del texto y de elementos como énfasis, enlaces y código.
Listas: administrar numeración, viñetas e indentación de listas anidadas.
Espacios en blanco: evitar separaciones innecesarias.
Caracteres especiales: escapar caracteres Markdown cuando corresponda sin modificar los
delimitadores creados por el conversor.
Referencias relativas: convertir enlaces e imágenes a referencias válidas respecto a la URL base.
Actividad 4. Generación del archivo Markdown
Implemente MarkdownWriter utilizando:
java.nio.file.Files
java.nio.file.Path
java.nio.charset.StandardCharsets
Método sugerido:
public void guardar(Path destino, String contenido) throws IOException
Genere un nombre predeterminado si el usuario no indica uno y cree los directorios de destino necesarios.
Actividad 5. Integración y manejo de errores
Integre los componentes en HTML2Markdown . La ejecución deberá informar la URL, el estado de conexión, la
conversión y la ubicación y tamaño del archivo generado.
Ejemplo ilustrativo:
HTML2Markdown--------------------------------
URL: https://ejemplo.org/articulo.html
Conectando con el servidor...
Respuesta HTTP: 200
Analizando HTML...
Convirtiendo a Markdown...
Archivo generado: articulo.md
Tamaño: 4,582 bytes
Conversión finalizada.
Controle URL inválida, servidor inaccesible, respuesta HTTP no exitosa, contenido no HTML y errores de
escritura.
Construya documentos HTML de prueba y compruebe cada caso.
Prueba Condición Resultado esperado
P01 Sin argumentos Mensaje de uso Muestra mensaje de uso y termina con código 1
P02 URL inválida Error controlado Error controlado de validación (Error de validación)
P03 Página inexistente Error HTTP Error HTTP controlado (Respuesta HTTP no exitosa: 404)
P04 Encabezados h1–h6 Conversión correcta Convertidos a # hasta ######
P05 Negritas y cursivas Formato preservado Preservados como **texto** y *texto*
P06 Enlaces absolutos y relativos Enlaces Markdown válidos Resueltos a URL absolutas en formato [texto](URL)
P07 Imágenes con texto alternativo Sintaxis ![alt](URL) Convertidas a ![alt](URL) con ruta resuelta
P08 Listas ordenadas y no ordenadas Estructura conservada Viñetas - y numeración 1. conservadas
P09 Elementos anidados Jerarquía y contenido conservados Indentación y jerarquía preservadas
P10 Etiquetas script y style Contenido excluido Contenido omitido por completo
P11 Acentos y entidades HTML Caracteres preservados Caracteres UTF-8 preservados correctamente
P12 Página HTML vacía Archivo vacío o mensaje informativo Genera archivo vacío e imprime aviso informativo
Abra el Markdown generado en el visor de IntelliJ IDEA o GitHub. Compare las salidas con archivos
Markdown esperados creados manualmente para las pruebas.
8. Pruebas de funcionamiento
9. Restricciones técnicas
1. Utilizar exclusivamente clases de la API estándar de Java.
2. Realizar conexiones HTTP mediante URL , URLConnection y HttpURLConnection .
3. No utilizar Jsoup, flexmark, commonmark, Apache HttpClient ni bibliotecas externas de conversión.
4. No invocar programas externos para convertir HTML.
5. No ejecutar JavaScript.
6. No descargar imágenes ni recursos adicionales: convertir solamente sus referencias.
7. No recorrer enlaces hacia otras páginas.
8. Limitar el tamaño del HTML y establecer tiempos máximos de espera.
9. Utilizar páginas autorizadas y preferentemente destinos controlados; admitir URL arbitrarias requiere
controles adicionales de redirecciones y destinos internos.
10. Preguntas de reflexión
Responda en README.md :
1. ¿Qué papel desempeña la aplicación dentro de una arquitectura cliente-servidor?
La aplicación actúa como un cliente HTTP especializado (agente de usuario). Inicia la comunicación enviando peticiones GET hacia un servidor web remoto, recibe la representación HTML del recurso solicitado, la interpreta localmente sin interfaz gráfica y transforma su contenido en un archivo Markdown almacenado en el sistema local.

2. ¿Qué responsabilidades corresponden al servidor y cuáles al cliente?
Servidor: Escuchar peticiones entrantes, localizar el recurso asociado a la ruta solicitada, emitir el código de estado HTTP correspondiente (200, 301, 404, etc.), enviar las cabeceras descriptivas (Content-Type, Content-Length, Location) y transmitir el flujo de bytes del documento.

Cliente (HTML2Markdown): Validar la URL y el protocolo (http/https), configurar tiempos de espera (timeouts) y límites de lectura, verificar el estado y tipo MIME de la respuesta, detectar la codificación (Charset), construir el árbol HTML, resolver rutas relativas y escribir el archivo .md en disco.

3. ¿Qué diferencia existe entre recuperar HTML e interpretar su estructura?
Recuperar HTML consiste en descargar una secuencia plana de bytes o caracteres mediante el protocolo HTTP. Interpretar su estructura implica realizar un análisis sintáctico (parsing) para reconocer etiquetas de apertura y cierre, atributos, entidades y relaciones jerárquicas padre-hijo, separando el contenido textual útil del marcado estructural y del código embebido (<script>, <style>).

4. ¿Por qué no basta reemplazar etiquetas HTML con expresiones regulares?
El HTML es un lenguaje libre de contexto que permite anidamiento recursivo arbitrario, atributos en distinto orden, comentarios y cierres implícitos. Las expresiones regulares carecen de memoria de pila (stack) para emparejar etiquetas anidadas del mismo tipo (como sublistas dentro de listas o énfasis combinado), por lo que fallan o corrompen el texto ante estructuras reales.

5. ¿Qué dificultades presentan los elementos anidados?
Orden del flujo textual: Preservar la secuencia exacta entre texto plano y etiquetas en línea (<p>Texto <strong>negrita <em>mixta</em></strong> fin</p>).

Estado contextual acumulado: Mantener niveles de indentación en listas anidadas (<ul>/<ol>), prefijos en citas (<blockquote>) y desactivar el escape de caracteres Markdown dentro de <pre><code>.

Cierres implícitos: Manejar documentos donde etiquetas como <li> o <p> se cierran implícitamente al abrir otra etiqueta hermana.

6. ¿Qué información puede perderse al convertir HTML a Markdown?
Markdown está diseñado para representar estructura documental básica, por lo que se pierden estilos visuales CSS (colores, tipografías, márgenes, cuadrículas), lógica interactiva JavaScript, atributos semánticos (class, id, data-*, aria-*), formularios y elementos multimedia incrustados (<video>, <audio>, <iframe>).

7. ¿Por qué deben resolverse las URL relativas?
En una página web, enlaces e imágenes suelen definirse respecto a la ruta del documento en el servidor (href="listas.html" o src="../img/logo.png"). Al guardar el archivo Markdown en una carpeta local fuera del servidor original, esas rutas relativas quedarían rotas si no se convierten a URL absolutas tomando como base la dirección descargada.

8. ¿Qué ventajas ofrece separar descarga, análisis y conversión en clases?
Aplica el Principio de Responsabilidad Única (SRP):

Mantenibilidad: Modificar una regla de formato en MarkdownConverter no afecta la lógica de red en WebClient.

Testabilidad: Permite probar el análisis y la conversión con cadenas HTML en memoria sin depender de red externa.

Reutilización: Componentes como WebClient o MarkdownWriter pueden reutilizarse en otros proyectos sin cambios.

9. ¿Qué limitaciones presenta ParserDelegator con HTML moderno?
ParserDelegator está basado en la especificación HTML 3.2. No reconoce de forma nativa etiquetas semánticas modernas de HTML5 (<article>, <section>, <nav>, <main>, <header>, <footer>), tratándolas como etiquetas desconocidas (HTML.UnknownTag), y requiere pasar ignoreCharSet = true para evitar que lance ChangedCharSetException al leer <meta charset="...">.

10. ¿Qué modificaciones serían necesarias para convertir tablas HTML a Markdown?
Registrar en HtmlParser las etiquetas <table>, <thead>, <tbody>, <tr>, <th> y <td> para conservar filas y celdas en el árbol.

En MarkdownConverter, agrupar las celdas de cada fila (<tr>) en una matriz de cadenas (List<List<String>>).

Renderizar la primera fila como encabezado (| Col 1 | Col 2 |), insertar inmediatamente la fila separadora obligatoria de Markdown (| --- | --- |) y formatear las filas restantes escapando cualquier carácter tubería (\|) dentro del texto de las celdas.
11. Entregables
Entregue el enlace a un repositorio GitHub que incluya:
Código fuente completo.
Documentos HTML de prueba.
Ejemplos representativos de salida Markdown.
Instrucciones de compilación y ejecución.
Archivo README.md con respuestas a las preguntas de reflexión.
Evidencias del funcionamiento.
Historial de commits que muestre el desarrollo incremental.
12. Rúbrica de evaluación
Criterio
Validación de argumentos y comunicación HTTP
Análisis de la estructura HTML
Implementación de reglas de conversión
Elementos anidados y referencias relativas
Generación del archivo Markdown
Manejo de errores y calidad del código
Pruebas funcionales
Documentación y uso de Git/GitHub
Total
13. Desafíos opcionales
Ponderación
15 %
20 %
25 %
10 %
10 %
10 %
5 %
5 %
100 %
Convertir tablas HTML a tablas Markdown.
Reconocer bloques de código e indicar el lenguaje.
Generar un índice a partir de encabezados.
Procesar varias URL desde un archivo CSV.
Generar un reporte de elementos HTML no admitidos.