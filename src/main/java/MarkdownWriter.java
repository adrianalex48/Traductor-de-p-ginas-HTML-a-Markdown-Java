import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class MarkdownWriter {

    public void guardar(Path destino, String contenido) throws IOException {
        if (destino == null) {
            throw new IllegalArgumentException("La ruta de destino no puede ser nula.");
        }

        Path directorioPadre = destino.toAbsolutePath().getParent();
        if (directorioPadre != null && !Files.exists(directorioPadre)) {
            Files.createDirectories(directorioPadre);
        }

        String textoFinal = (contenido == null) ? "" : contenido;
        Files.writeString(destino, textoFinal, StandardCharsets.UTF_8);
    }

    public static Path resolverRutaSalida(URL url, String rutaOpcional) {
        if (rutaOpcional != null && !rutaOpcional.isBlank()) {
            return Path.of(rutaOpcional);
        }

        String pathUrl = url.getPath();
        if (pathUrl == null || pathUrl.isBlank() || pathUrl.endsWith("/")) {
            return Path.of("index.md");
        }

        int ultimaBarra = pathUrl.lastIndexOf('/');
        String nombreArchivo = (ultimaBarra >= 0) ? pathUrl.substring(ultimaBarra + 1) : pathUrl;

        if (nombreArchivo.isBlank()) {
            return Path.of("index.md");
        }

        int puntoExtension = nombreArchivo.lastIndexOf('.');
        String nombreBase = (puntoExtension > 0)
                ? nombreArchivo.substring(0, puntoExtension)
                : nombreArchivo;

        return Path.of(nombreBase + ".md");
    }
}