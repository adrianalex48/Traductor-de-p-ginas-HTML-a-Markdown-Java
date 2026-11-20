import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Set;

public class MarkdownConverter {

    private static final Set<String> ETIQUETAS_LISTA = Set.of("ul", "ol");

    private record Contexto(URL urlBase, boolean enPre, boolean enCodigo, int profundidadLista) {
        Contexto conPre(boolean valor) {
            return new Contexto(urlBase, valor, enCodigo, profundidadLista);
        }

        Contexto conCodigo(boolean valor) {
            return new Contexto(urlBase, enPre, valor, profundidadLista);
        }

        Contexto incrementarLista() {
            return new Contexto(urlBase, enPre, enCodigo, profundidadLista + 1);
        }
    }

    public String convertir(HtmlParser.ElementNode raiz, URL urlBase) {
        if (raiz == null) {
            return "";
        }
        Contexto contextoInicial = new Contexto(urlBase, false, false, 0);
        String bruto = convertirHijos(raiz.getChildren(), contextoInicial);
        return normalizarSalida(bruto);
    }

    private String convertirHijos(List<HtmlParser.HtmlNode> hijos, Contexto ctx) {
        StringBuilder sb = new StringBuilder();
        for (HtmlParser.HtmlNode hijo : hijos) {
            sb.append(convertirNodo(hijo, ctx));
        }
        return sb.toString();
    }

    private String convertirNodo(HtmlParser.HtmlNode nodo, Contexto ctx) {
        if (nodo instanceof HtmlParser.TextNode textoNode) {
            return procesarTexto(textoNode.getText(), ctx);
        }

        HtmlParser.ElementNode elem = (HtmlParser.ElementNode) nodo;
        String tag = elem.getTagName();

        return switch (tag) {
            case "h1", "h2", "h3", "h4", "h5", "h6" -> convertirEncabezado(elem, ctx);
            case "p" -> convertirParrafo(elem, ctx);
            case "strong", "b" -> convertirEnfasis(elem, ctx, "**");
            case "em", "i" -> convertirEnfasis(elem, ctx, "*");
            case "a" -> convertirEnlace(elem, ctx);
            case "img" -> convertirImagen(elem, ctx);
            case "ul" -> convertirLista(elem, ctx, false);
            case "ol" -> convertirLista(elem, ctx, true);
            case "br" -> "  \n";
            case "blockquote" -> convertirCita(elem, ctx);
            case "code" -> convertirCodigoEnLinea(elem, ctx);
            case "pre" -> convertirBloqueCodigo(elem, ctx);
            case "hr" -> "\n---\n\n";
            default -> convertirHijos(elem.getChildren(), ctx);
        };
    }

    private String convertirEncabezado(HtmlParser.ElementNode elem, Contexto ctx) {
        int nivel = Character.getNumericValue(elem.getTagName().charAt(1));
        String texto = convertirHijos(elem.getChildren(), ctx)
                .replaceAll("\\s+", " ")
                .trim();
        if (texto.isEmpty()) {
            return "";
        }
        return "#".repeat(nivel) + " " + texto + "\n\n";
    }

    private String convertirParrafo(HtmlParser.ElementNode elem, Contexto ctx) {
        String contenido = limpiarLineasInline(convertirHijos(elem.getChildren(), ctx));
        if (contenido.isEmpty()) {
            return "";
        }
        return contenido + "\n\n";
    }

    private String convertirEnfasis(HtmlParser.ElementNode elem, Contexto ctx, String delimitador) {
        String contenido = convertirHijos(elem.getChildren(), ctx);
        if (contenido.isBlank()) {
            return contenido;
        }
        String prefijoEspacio = contenido.startsWith(" ") ? " " : "";
        String sufijoEspacio = contenido.endsWith(" ") ? " " : "";
        String centro = contenido.trim();
        return prefijoEspacio + delimitador + centro + delimitador + sufijoEspacio;
    }

    private String convertirEnlace(HtmlParser.ElementNode elem, Contexto ctx) {
        String href = elem.getAttribute("href");
        String texto = limpiarLineasInline(convertirHijos(elem.getChildren(), ctx));

        if (href == null || href.isBlank()) {
            return texto;
        }

        String urlResuelta = resolverReferencia(ctx.urlBase(), href.trim());
        if (texto.isEmpty()) {
            texto = urlResuelta;
        }
        return "[" + texto + "](" + urlResuelta + ")";
    }

    private String convertirImagen(HtmlParser.ElementNode elem, Contexto ctx) {
        String src = elem.getAttribute("src");
        if (src == null || src.isBlank()) {
            return "";
        }
        String alt = elem.getAttribute("alt");
        String altLimpio = (alt == null) ? "" : alt.replace("]", "\\]").trim();
        String urlResuelta = resolverReferencia(ctx.urlBase(), src.trim());
        return "![" + altLimpio + "](" + urlResuelta + ")";
    }

    private String convertirLista(HtmlParser.ElementNode listaElem, Contexto ctx, boolean ordenada) {
        StringBuilder sb = new StringBuilder();
        String indentacion = "  ".repeat(ctx.profundidadLista());
        int indice = 1;

        for (HtmlParser.HtmlNode hijo : listaElem.getChildren()) {
            if (hijo instanceof HtmlParser.ElementNode item && item.getTagName().equals("li")) {
                String vineta = ordenada ? (indice + ". ") : "- ";
                String contenidoItem = convertirElementoLista(item, ctx);
                if (!contenidoItem.isEmpty()) {
                    sb.append(indentacion).append(vineta).append(contenidoItem).append("\n");
                    indice++;
                }
            }
        }

        if (ctx.profundidadLista() == 0 && !sb.isEmpty()) {
            sb.append("\n");
        }
        return sb.toString();
    }

    private String convertirElementoLista(HtmlParser.ElementNode liElem, Contexto ctx) {
        StringBuilder lineaPrincipal = new StringBuilder();
        StringBuilder sublistas = new StringBuilder();

        for (HtmlParser.HtmlNode hijo : liElem.getChildren()) {
            if (hijo instanceof HtmlParser.ElementNode subElem
                    && ETIQUETAS_LISTA.contains(subElem.getTagName())) {
                String sublistaTexto = convertirNodo(subElem, ctx.incrementarLista());
                if (!sublistaTexto.isEmpty()) {
                    sublistas.append("\n").append(sublistaTexto.stripTrailing());
                }
            } else {
                lineaPrincipal.append(convertirNodo(hijo, ctx));
            }
        }

        String textoPrincipal = limpiarLineasInline(lineaPrincipal.toString());
        return textoPrincipal + sublistas;
    }

    private String convertirCita(HtmlParser.ElementNode elem, Contexto ctx) {
        String contenido = convertirHijos(elem.getChildren(), ctx).trim();
        if (contenido.isEmpty()) {
            return "";
        }

        String[] lineas = contenido.split("\\R");
        StringBuilder sb = new StringBuilder();
        for (String linea : lineas) {
            String l = linea.trim();
            if (l.isEmpty()) {
                sb.append(">\n");
            } else {
                sb.append("> ").append(l).append("\n");
            }
        }
        sb.append("\n");
        return sb.toString();
    }

    private String convertirCodigoEnLinea(HtmlParser.ElementNode elem, Contexto ctx) {
        if (ctx.enPre()) {
            return convertirHijos(elem.getChildren(), ctx.conCodigo(true));
        }

        String codigo = convertirHijos(elem.getChildren(), ctx.conCodigo(true))
                .replaceAll("\\R+", " ")
                .trim();
        if (codigo.isEmpty()) {
            return "";
        }

        String delimitador = codigo.contains("`") ? "``" : "`";
        return delimitador + codigo + delimitador;
    }

    private String convertirBloqueCodigo(HtmlParser.ElementNode elem, Contexto ctx) {
        String codigo = convertirHijos(elem.getChildren(), ctx.conPre(true));
        codigo = codigo.replaceFirst("^\\R+", "").replaceFirst("\\R+$", "");
        if (codigo.isEmpty()) {
            return "";
        }
        return "\n```\n" + codigo + "\n```\n\n";
    }

    private String procesarTexto(String texto, Contexto ctx) {
        if (texto == null || texto.isEmpty()) {
            return "";
        }
        if (ctx.enPre() || ctx.enCodigo()) {
            return texto;
        }
        String colapsado = texto.replaceAll("[\\s\\u00A0]+", " ");
        return escaparMarkdown(colapsado);
    }

    private String escaparMarkdown(String texto) {
        StringBuilder sb = new StringBuilder(texto.length());
        for (int i = 0; i < texto.length(); i++) {
            char c = texto.charAt(i);
            switch (c) {
                case '\\', '`', '*', '_', '[', ']' -> sb.append('\\').append(c);
                default -> sb.append(c);
            }
        }
        return sb.toString();
    }

    private String resolverReferencia(URL urlBase, String referencia) {
        if (referencia.startsWith("#")
                || referencia.startsWith("mailto:")
                || referencia.startsWith("tel:")) {
            return referencia;
        }
        if (urlBase == null) {
            return referencia;
        }
        try {
            URI uriRef = new URI(referencia);
            if (uriRef.isAbsolute()) {
                return uriRef.toString();
            }
            return urlBase.toURI().resolve(uriRef).toString();
        } catch (URISyntaxException e) {
            try {
                return new URL(urlBase, referencia).toString();
            } catch (MalformedURLException ex) {
                return referencia;
            }
        }
    }

    private String limpiarLineasInline(String texto) {
        return texto
                .replaceAll(" *\\n *", "\n")
                .replaceAll(" {2,}", " ")
                .trim();
    }

    private String normalizarSalida(String texto) {
        String limpio = texto
                .replace("\r\n", "\n")
                .replaceAll("[ \\t]+\\n", "\n")
                .replaceAll("\\n{3,}", "\n\n")
                .trim();
        return limpio.isEmpty() ? "" : limpio + "\n";
    }

}