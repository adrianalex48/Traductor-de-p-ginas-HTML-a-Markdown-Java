import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WebClient {

    private static final int TIMEOUT_CONEXION_MS = 10_000;
    private static final int TIMEOUT_LECTURA_MS = 15_000;
    private static final int LIMITE_BYTES_HTML = 5 * 1024 * 1024;
    private static final int MAX_REDIRECCIONES = 5;

    private static final Pattern PATRON_CHARSET_HEADER =
            Pattern.compile("charset\\s*=\\s*\"?([a-zA-Z0-9._-]+)\"?", Pattern.CASE_INSENSITIVE);
    private static final Pattern PATRON_CHARSET_META =
            Pattern.compile("<meta[^>]+charset\\s*=\\s*[\"']?([a-zA-Z0-9._-]+)[\"']?", Pattern.CASE_INSENSITIVE);

    public record RespuestaHttp(String html, int codigoEstado, URL urlFinal) {}

    public String descargarHTML(URL url) throws IOException {
        return descargarConMetadatos(url).html();
    }

    public RespuestaHttp descargarConMetadatos(URL urlInicial) throws IOException {
        URL urlActual = urlInicial;
        int redirecciones = 0;

        while (true) {
            validarProtocoloHttp(urlActual);
            URLConnection conexionBase = urlActual.openConnection();

            if (!(conexionBase instanceof HttpURLConnection conexion)) {
                throw new IOException("La conexión no corresponde al protocolo HTTP/HTTPS.");
            }

            try {
                conexion.setRequestMethod("GET");
                conexion.setConnectTimeout(TIMEOUT_CONEXION_MS);
                conexion.setReadTimeout(TIMEOUT_LECTURA_MS);
                conexion.setInstanceFollowRedirects(false);
                conexion.setRequestProperty("User-Agent", "HTML2Markdown-JavaClient/1.0");
                conexion.setRequestProperty("Accept", "text/html,application/xhtml+xml;q=0.9,*/*;q=0.8");
                conexion.setRequestProperty("Accept-Charset", "UTF-8");

                conexion.connect();
                int codigo = conexion.getResponseCode();

                if (esRedireccion(codigo)) {
                    if (redirecciones >= MAX_REDIRECCIONES) {
                        throw new IOException("Se excedió el límite de redirecciones (" + MAX_REDIRECCIONES + ").");
                    }
                    String ubicacion = conexion.getHeaderField("Location");
                    if (ubicacion == null || ubicacion.isBlank()) {
                        throw new IOException("Redirección (" + codigo + ") sin cabecera Location.");
                    }
                    urlActual = resolverRedireccion(urlActual, ubicacion);
                    redirecciones++;
                    continue;
                }

                if (codigo < 200 || codigo >= 300) {
                    throw new IOException("Respuesta HTTP no exitosa: " + codigo + " " + conexion.getResponseMessage());
                }

                String contentType = conexion.getContentType();
                validarTipoContenidoHtml(contentType);

                byte[] datosCrudos = leerFlujoConLimite(conexion.getInputStream());
                Charset charset = detectarCharset(contentType, datosCrudos);
                String html = new String(datosCrudos, charset);

                return new RespuestaHttp(html, codigo, urlActual);
            } finally {
                conexion.disconnect();
            }
        }
    }

    private void validarProtocoloHttp(URL url) throws IOException {
        String protocolo = url.getProtocol();
        if (protocolo == null || (!protocolo.equalsIgnoreCase("http") && !protocolo.equalsIgnoreCase("https"))) {
            throw new IOException("Protocolo no permitido: " + protocolo);
        }
    }

    private boolean esRedireccion(int codigo) {
        return codigo == HttpURLConnection.HTTP_MOVED_PERM
                || codigo == HttpURLConnection.HTTP_MOVED_TEMP
                || codigo == HttpURLConnection.HTTP_SEE_OTHER
                || codigo == 307
                || codigo == 308;
    }

    private URL resolverRedireccion(URL base, String ubicacion) throws IOException {
        try {
            URI destino = base.toURI().resolve(ubicacion.trim());
            URL nuevaUrl = destino.toURL();
            validarProtocoloHttp(nuevaUrl);
            return nuevaUrl;
        } catch (Exception e) {
            throw new IOException("URL de redirección inválida: " + ubicacion, e);
        }
    }

    private void validarTipoContenidoHtml(String contentType) throws IOException {
        if (contentType == null || contentType.isBlank()) {
            return;
        }
        String tipoBase = contentType.split(";")[0].trim().toLowerCase(Locale.ROOT);
        if (!tipoBase.equals("text/html") && !tipoBase.equals("application/xhtml+xml")) {
            throw new IOException("El recurso no es HTML (Content-Type: " + contentType + ").");
        }
    }

    private byte[] leerFlujoConLimite(InputStream flujoEntrada) throws IOException {
        try (BufferedInputStream entrada = new BufferedInputStream(flujoEntrada);
             ByteArrayOutputStream salida = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[8192];
            int bytesLeidos;
            int totalBytes = 0;

            while ((bytesLeidos = entrada.read(buffer)) != -1) {
                totalBytes += bytesLeidos;
                if (totalBytes > LIMITE_BYTES_HTML) {
                    throw new IOException("El documento excede el tamaño máximo permitido (" + LIMITE_BYTES_HTML + " bytes).");
                }
                salida.write(buffer, 0, bytesLeidos);
            }
            return salida.toByteArray();
        }
    }

    private Charset detectarCharset(String contentType, byte[] datos) {
        if (contentType != null) {
            Matcher matcherHeader = PATRON_CHARSET_HEADER.matcher(contentType);
            if (matcherHeader.find()) {
                Charset cs = parsearCharsetSeguro(matcherHeader.group(1));
                if (cs != null) {
                    return cs;
                }
            }
        }

        int longitudInspeccion = Math.min(datos.length, 2048);
        String encabezadoHtml = new String(datos, 0, longitudInspeccion, StandardCharsets.ISO_8859_1);
        Matcher matcherMeta = PATRON_CHARSET_META.matcher(encabezadoHtml);
        if (matcherMeta.find()) {
            Charset cs = parsearCharsetSeguro(matcherMeta.group(1));
            if (cs != null) {
                return cs;
            }
        }

        return StandardCharsets.UTF_8;
    }

    private Charset parsearCharsetSeguro(String nombre) {
        try {
            return Charset.forName(nombre.trim());
        } catch (IllegalCharsetNameException | UnsupportedCharsetException e) {
            return null;
        }
    }

}