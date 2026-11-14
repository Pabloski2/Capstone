package com.financialbeacon.security;

import java.net.URI;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Decide qué páginas web (orígenes) pueden llamar a la API desde un navegador (CORS).
 * Siempre se permiten los orígenes listados en la configuración y, mientras se desarrolla,
 * localhost y las redes privadas (para abrir la app desde el celular por WiFi).
 */
public final class OrigenesPermitidos {

    private static final Pattern IPV4 = Pattern.compile("^(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})$");

    private final Set<String> exactos;
    private final boolean permitirRedLocal;

    /** exactos: orígenes completos separados por coma, por ejemplo https://app.midominio.cl */
    public OrigenesPermitidos(String exactos, boolean permitirRedLocal) {
        this.exactos = exactos == null ? Set.of() : Arrays.stream(exactos.split(","))
                .map(s -> s.trim().toLowerCase(Locale.ROOT))
                .map(s -> s.endsWith("/") ? s.substring(0, s.length() - 1) : s)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toUnmodifiableSet());
        this.permitirRedLocal = permitirRedLocal;
    }

    public boolean permite(String origen) {
        if (origen == null || origen.isBlank()) {
            return false;
        }
        String normal = origen.trim().toLowerCase(Locale.ROOT);
        if (exactos.contains(normal)) {
            return true;
        }
        if (!permitirRedLocal) {
            return false;
        }
        try {
            URI uri = URI.create(normal);
            String esquema = uri.getScheme();
            String host = uri.getHost();
            boolean sinRuta = uri.getRawPath() == null || uri.getRawPath().isEmpty();
            if (host == null || !sinRuta || uri.getRawQuery() != null || uri.getRawUserInfo() != null
                    || !("http".equals(esquema) || "https".equals(esquema))) {
                return false;
            }
            return "localhost".equals(host) || esIpv4Privada(host);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    static boolean esIpv4Privada(String host) {
        Matcher m = IPV4.matcher(host);
        if (!m.matches()) {
            return false;
        }
        int[] o = new int[4];
        for (int i = 0; i < 4; i++) {
            o[i] = Integer.parseInt(m.group(i + 1));
            if (o[i] > 255) {
                return false;
            }
        }
        return o[0] == 127
                || o[0] == 10
                || (o[0] == 172 && o[1] >= 16 && o[1] <= 31)
                || (o[0] == 192 && o[1] == 168);
    }
}
