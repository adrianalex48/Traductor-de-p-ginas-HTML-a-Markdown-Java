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
HTML2Markdown/
├── README.md
├── .gitignore
├── src/
│   
│       
│           
│           
│           
│           
│           
└── main/
└── java/
├── HTML2Markdown.java
├── WebClient.java
├── HtmlParser.java
├── MarkdownConverter.java
└── MarkdownWriter.java
├── tests/
│   
├── encabezados.html
│   
│   
├── listas.html
├── enlaces.html
│   
└── documento_completo.html
└── output/
└── articulo.md
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
P01 Sin argumentos Mensaje de uso
P02 URL inválida Error controlado
P03 Página inexistente Error HTTP
P04 Encabezados h1–h6 Conversión correcta
P05 Negritas y cursivas Formato preservado
P06 Enlaces absolutos y relativos Enlaces Markdown válidos
P07 Imágenes con texto alternativo Sintaxis ![alt](URL)
P08 Listas ordenadas y no ordenadas Estructura conservada
P09 Elementos anidados Jerarquía y contenido conservados
P10 Etiquetas script y style Contenido excluido
P11 Acentos y entidades HTML Caracteres preservados
P12 Página HTML vacía Archivo vacío o mensaje informativo
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
2. ¿Qué responsabilidades corresponden al servidor y cuáles al cliente?
3. ¿Qué diferencia existe entre recuperar HTML e interpretar su estructura?
4. ¿Por qué no basta reemplazar etiquetas HTML con expresiones regulares?
5. ¿Qué dificultades presentan los elementos anidados?
6. ¿Qué información puede perderse al convertir HTML a Markdown?
7. ¿Por qué deben resolverse las URL relativas?
8. ¿Qué ventajas ofrece separar descarga, análisis y conversión en clases?
9. ¿Qué limitaciones presenta ParserDelegator con HTML moderno?
10. ¿Qué modificaciones serían necesarias para convertir tablas HTML a Markdown?
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