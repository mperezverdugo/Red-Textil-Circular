package com.textilcircular.usuarios.security;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.textilcircular.usuarios.model.Rol;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

@Service
public class TokenService {
    private final JwtEncoder encoder;
    private final NimbusJwtDecoder decoder;
    private final Duration duracion;
    private final Clock reloj;

    @Autowired
    public TokenService(@Value("${usuarios.token.secreto}") String secreto,
                        @Value("${usuarios.token.duracion}") Duration duracion) {
        this(secreto, duracion, Clock.systemUTC());
    }

    public TokenService(String secreto, Duration duracion, Clock reloj) {
        byte[] bytes = secreto.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32 || duracion.isZero() || duracion.isNegative()) {
            throw new IllegalArgumentException("El secreto debe tener al menos 32 bytes y la duración debe ser positiva");
        }
        var clave = new SecretKeySpec(bytes, "HmacSHA256");
        encoder = new NimbusJwtEncoder(new ImmutableSecret<>(clave));
        decoder = NimbusJwtDecoder.withSecretKey(clave).macAlgorithm(MacAlgorithm.HS256).build();
        var validadorFecha = new JwtTimestampValidator(Duration.ZERO);
        validadorFecha.setClock(reloj);
        decoder.setJwtValidator(validadorFecha);
        this.duracion = duracion;
        this.reloj = reloj;
    }

    public String generar(Long usuarioId, Rol rol) {
        if (usuarioId == null || usuarioId <= 0 || rol == null) {
            throw new IllegalArgumentException("El usuario y el rol son obligatorios");
        }
        Instant ahora = reloj.instant();
        var claims = JwtClaimsSet.builder()
                .subject(usuarioId.toString())
                .claim("rol", rol.name())
                .issuedAt(ahora)
                .expiresAt(ahora.plus(duracion))
                .build();
        var encabezado = JwsHeader.with(MacAlgorithm.HS256).build();
        return encoder.encode(JwtEncoderParameters.from(encabezado, claims)).getTokenValue();
    }

    public DatosToken validar(String token, Rol rolEsperado) {
        try {
            Jwt jwt = decoder.decode(token);
            Long usuarioId = Long.valueOf(jwt.getSubject());
            Rol rol = Rol.valueOf(jwt.getClaimAsString("rol"));
            if (usuarioId <= 0 || rol != rolEsperado || jwt.getExpiresAt() == null
                    || !jwt.getExpiresAt().isAfter(reloj.instant())) {
                throw new IllegalArgumentException("Token inválido");
            }
            return new DatosToken(usuarioId, rol);
        } catch (JwtException | IllegalArgumentException error) {
            throw new IllegalArgumentException("Token inválido o vencido");
        }
    }

    public record DatosToken(Long usuarioId, Rol rol) {}
}
