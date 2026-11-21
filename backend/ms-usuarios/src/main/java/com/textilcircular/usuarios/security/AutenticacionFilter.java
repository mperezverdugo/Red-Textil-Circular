package com.textilcircular.usuarios.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.textilcircular.usuarios.exception.ApiException;
import com.textilcircular.usuarios.exception.ManejadorErrores.ErrorResponse;
import com.textilcircular.usuarios.model.Rol;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class AutenticacionFilter extends OncePerRequestFilter {
    public static final String USUARIO_AUTENTICADO = "usuarioAutenticado";

    private final AutenticacionService autenticacion;
    private final ObjectMapper json;
    private final boolean consolaPublica;
    private final String rutaConsola;

    public AutenticacionFilter(AutenticacionService autenticacion, ObjectMapper json,
                              Environment entorno,
                              @Value("${spring.h2.console.enabled:false}") boolean consolaHabilitada,
                              @Value("${spring.h2.console.path:/h2-console}") String rutaConsola) {
        this.autenticacion = autenticacion;
        this.json = json;
        this.consolaPublica = consolaHabilitada && entorno.acceptsProfiles(Profiles.of("dev"));
        this.rutaConsola = rutaConsola;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String ruta = request.getServletPath();
        if (ruta.isEmpty()) {
            ruta = request.getRequestURI().substring(request.getContextPath().length());
        }
        if (("POST".equals(request.getMethod()) && perteneceA(ruta, "/auth"))
                || (consolaPublica && perteneceA(ruta, rutaConsola))) {
            chain.doFilter(request, response);
            return;
        }

        try {
            String encabezado = request.getHeader("Authorization");
            if (encabezado == null || encabezado.isBlank()) {
                throw new ApiException(HttpStatus.UNAUTHORIZED, "TOKEN_REQUERIDO", "Debes enviar un token");
            }
            if (!encabezado.regionMatches(true, 0, "Bearer ", 0, 7) || encabezado.substring(7).isBlank()) {
                throw new ApiException(HttpStatus.UNAUTHORIZED, "TOKEN_INVALIDO", "Usa un token Bearer válido");
            }
            TokenService.DatosToken datos = autenticacion.autenticar(encabezado.substring(7));
            if (perteneceA(ruta, "/admin") && datos.rol() != Rol.ADMIN) {
                throw new ApiException(HttpStatus.FORBIDDEN, "ACCESO_DENEGADO", "Se requiere el rol ADMIN");
            }
            request.setAttribute(USUARIO_AUTENTICADO, datos);
        } catch (ApiException error) {
            response.setStatus(error.getEstado().value());
            response.setContentType("application/json");
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            json.writeValue(response.getWriter(), new ErrorResponse(error.getCodigo(), error.getMessage()));
            return;
        }
        chain.doFilter(request, response);
    }

    private boolean perteneceA(String ruta, String prefijo) {
        return ruta.equals(prefijo) || ruta.startsWith(prefijo + "/");
    }
}
