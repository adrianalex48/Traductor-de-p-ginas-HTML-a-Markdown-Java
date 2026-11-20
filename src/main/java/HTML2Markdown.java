import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.util.Locale;

public class HTML2Markdown {

    public static void main(String[] args) {
        if (args.length < 1 || args.length > 2) {
            mostrarUso();
            System.exit(1);
        }

        String urlTexto = args[0].trim();
        String rutaSalidaArg = (args.length == 2) ? args[1].trim() : null;

        try {
            URL url = validarYConstruirUrl(urlTexto);
            Path destino = MarkdownWriter.resolverRutaSalida(url, rutaSalidaArg);

            WebClient cliente = new WebClient();
            HtmlParser parser = new HtmlParser();
            MarkdownConverter conversor = new MarkdownConverter();
            MarkdownWriter escritor = new MarkdownWriter();

            System.out.println("HTML2Markdown");
            System.out.println("--------------------------------");
            System.out.println("URL: " + url);
            System.out.println("Conectando con el servidor...");

            WebClient.RespuestaHttp respuesta = cliente.descargarConMetadatos(url);
            System.out.println("Respuesta HTTP: " + respuesta.codigoEstado());

            System.out.println("Analizando HTML...");
            HtmlParser.ElementNode raiz = parser.analizar(respuesta.html());

            System.out.println("Convirtiendo a Markdown...");
            String markdown = conversor.convertir(raiz, respuesta.urlFinal());

            escritor.guardar(destino, markdown);
            long tamanoBytes = Files.size(destino);
            String tamanoFormateado = NumberFormat.getIntegerInstance(Locale.US).format(tamanoBytes);

            System.out.println("Archivo generado: " + destino);
            System.out.println("Tamaño: " + tamanoFormateado + " bytes");
            if (markdown.isEmpty()) {
                System.out.println("Aviso: El documento HTML no contenía elementos convertibles.");
            }
            System.out.println("Conversión finalizada.");

        } catch (IllegalArgumentException | MalformedURLException e) {
            System.err.println("Error de validación: " + e.getMessage());
            System.exit(2);
        } catch (UnknownHostException e) {
            System.err.println("Error de red: No se pudo resolver el servidor (" + e.getMessage() + ").");
            System.exit(3);
        } catch (SocketTimeoutException e) {
            System.err.println("Error de red: Tiempo de espera agotado al conectar o leer del servidor.");
            System.exit(3);
        } catch (IOException e) {
            System.err.println("Error de E/S o HTTP: " + e.getMessage());
            System.exit(4);
        }
    }

    private static URL validarYConstruirUrl(String urlTexto) throws MalformedURLException {
        if (urlTexto == null || urlTexto.isEmpty()) {
            throw new IllegalArgumentException("La URL no puede estar vacía.");
        }

        URI uri;
        try {
            uri = new URI(urlTexto);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Sintaxis de URL inválida: " + e.getMessage(), e);
        }

        String esquema = uri.getScheme();
        if (esquema == null || (!esquema.equalsIgnoreCase("http") && !esquema.equalsIgnoreCase("https"))) {
            throw new IllegalArgumentException(
                    "Protocolo no admitido. Solo se permiten URL con esquema http:// o https://.");
        }

        if (uri.getHost() == null || uri.getHost().isBlank()) {
            throw new IllegalArgumentException("La URL debe especificar un nombre de host válido.");
        }

        return uri.toURL();
    }

    private static void mostrarUso() {
        System.err.println("Uso: java HTML2Markdown <URL> [ruta_salida.md]");
        System.err.println("Ejemplos:");
        System.err.println("  java HTML2Markdown https://ejemplo.org/articulo.html");
        System.err.println("  java HTML2Markdown https://ejemplo.org/articulo.html output/salida.md");
    }
}