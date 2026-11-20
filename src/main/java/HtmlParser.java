import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;

public class HtmlParser {

    private static final Set<String> ETIQUETAS_IGNORADAS =
            Set.of("script", "style", "head", "noscript", "iframe", "object", "embed");

    private static final Set<String> ETIQUETAS_VACIAS =
            Set.of("br", "img", "hr", "meta", "link", "input");

    private static final Pattern PATRON_ENTIDAD_NUMERICA =
            Pattern.compile("&#(x?[0-9a-fA-F]+);");

    public sealed interface HtmlNode permits ElementNode, TextNode {}

    public static final class ElementNode implements HtmlNode {
        private final String tagName;
        private final Map<String, String> attributes;
        private final List<HtmlNode> children;

        public ElementNode(String tagName, Map<String, String> attributes) {
            this.tagName = tagName;
            this.attributes = Collections.unmodifiableMap(new LinkedHashMap<>(attributes));
            this.children = new ArrayList<>();
        }

        public String getTagName() {
            return tagName;
        }

        public String getAttribute(String name) {
            return attributes.get(name.toLowerCase(Locale.ROOT));
        }

        public List<HtmlNode> getChildren() {
            return Collections.unmodifiableList(children);
        }

        void addChild(HtmlNode child) {
            children.add(child);
        }
    }

    public static final class TextNode implements HtmlNode {
        private final String text;

        public TextNode(String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }
    }

    public ElementNode analizar(String html) throws IOException {
        ElementNode raiz = new ElementNode("root", Map.of());
        if (html == null || html.isBlank()) {
            return raiz;
        }

        ConstructorArbolCallback callback = new ConstructorArbolCallback(raiz);
        ParserDelegator delegator = new ParserDelegator();
        delegator.parse(new StringReader(html), callback, true);

        return raiz;
    }

    private static class ConstructorArbolCallback extends HTMLEditorKit.ParserCallback {
        private final Deque<ElementNode> pila = new ArrayDeque<>();
        private int profundidadIgnorada = 0;

        ConstructorArbolCallback(ElementNode raiz) {
            pila.push(raiz);
        }

        @Override
        public void handleStartTag(HTML.Tag t, MutableAttributeSet a, int pos) {
            String tag = t.toString().toLowerCase(Locale.ROOT);

            if (ETIQUETAS_IGNORADAS.contains(tag)) {
                profundidadIgnorada++;
                return;
            }
            if (profundidadIgnorada > 0) {
                return;
            }

            if (tag.equals("li")) {
                cerrarSiAbiertoEnNivelActual("li");
            }

            Map<String, String> attrs = extraerAtributos(a);
            ElementNode nodo = new ElementNode(tag, attrs);
            pila.peek().addChild(nodo);

            if (!ETIQUETAS_VACIAS.contains(tag)) {
                pila.push(nodo);
            }
        }

        @Override
        public void handleEndTag(HTML.Tag t, int pos) {
            String tag = t.toString().toLowerCase(Locale.ROOT);

            if (ETIQUETAS_IGNORADAS.contains(tag)) {
                if (profundidadIgnorada > 0) {
                    profundidadIgnorada--;
                }
                return;
            }
            if (profundidadIgnorada > 0) {
                return;
            }

            desapilarHasta(tag);
        }

        @Override
        public void handleSimpleTag(HTML.Tag t, MutableAttributeSet a, int pos) {
            if (profundidadIgnorada > 0) {
                return;
            }

            if (a != null && a.getAttribute(HTML.Attribute.ENDTAG) != null) {
                return;
            }

            String tag = t.toString().toLowerCase(Locale.ROOT);
            if (ETIQUETAS_VACIAS.contains(tag)) {
                Map<String, String> attrs = extraerAtributos(a);
                pila.peek().addChild(new ElementNode(tag, attrs));
            }
        }

        @Override
        public void handleText(char[] data, int pos) {
            if (profundidadIgnorada > 0 || data == null || data.length == 0) {
                return;
            }
            String texto = decodificarEntidadesRestantes(new String(data));
            pila.peek().addChild(new TextNode(texto));
        }

        private void cerrarSiAbiertoEnNivelActual(String tagObjetivo) {
            if (!pila.isEmpty() && pila.peek().getTagName().equals(tagObjetivo)) {
                pila.pop();
            }
        }

        private void desapilarHasta(String tagObjetivo) {
            boolean encontrado = false;
            for (ElementNode nodo : pila) {
                if (nodo.getTagName().equals(tagObjetivo)) {
                    encontrado = true;
                    break;
                }
            }
            if (!encontrado) {
                return;
            }
            while (pila.size() > 1) {
                ElementNode actual = pila.pop();
                if (actual.getTagName().equals(tagObjetivo)) {
                    break;
                }
            }
        }

        private Map<String, String> extraerAtributos(MutableAttributeSet a) {
            if (a == null) {
                return Map.of();
            }
            Map<String, String> mapa = new LinkedHashMap<>();
            Enumeration<?> nombres = a.getAttributeNames();
            while (nombres.hasMoreElements()) {
                Object clave = nombres.nextElement();
                Object valor = a.getAttribute(clave);
                if (clave != null && valor != null) {
                    mapa.put(clave.toString().toLowerCase(Locale.ROOT), valor.toString());
                }
            }
            return mapa;
        }

        private String decodificarEntidadesRestantes(String texto) {
            String resultado = texto
                    .replace("&nbsp;", " ")
                    .replace("&mdash;", "—")
                    .replace("&ndash;", "–")
                    .replace("&laquo;", "«")
                    .replace("&raquo;", "»")
                    .replace("&copy;", "©")
                    .replace("&reg;", "®");

            Matcher matcher = PATRON_ENTIDAD_NUMERICA.matcher(resultado);
            StringBuilder sb = new StringBuilder();
            while (matcher.find()) {
                String codigo = matcher.group(1);
                try {
                    int codePoint = codigo.startsWith("x") || codigo.startsWith("X")
                            ? Integer.parseInt(codigo.substring(1), 16)
                            : Integer.parseInt(codigo, 10);
                    if (Character.isValidCodePoint(codePoint)) {
                        matcher.appendReplacement(sb, Matcher.quoteReplacement(new String(Character.toChars(codePoint))));
                    }
                } catch (NumberFormatException ignored) {
                }
            }
            matcher.appendTail(sb);
            return sb.toString();
        }
    }



}