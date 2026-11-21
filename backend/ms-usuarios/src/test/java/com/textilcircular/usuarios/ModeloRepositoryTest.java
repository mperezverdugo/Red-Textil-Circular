package com.textilcircular.usuarios;

import com.textilcircular.usuarios.model.*;
import com.textilcircular.usuarios.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import java.time.Instant;
import java.util.List;
import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class ModeloRepositoryTest {
    @Autowired UsuarioRepository usuarios;
    @Autowired EmprendedoraRepository emprendedoras;
    @Autowired TestEntityManager entityManager;

    @ParameterizedTest
    @EnumSource(Rol.class)
    void conservaRolYConsentimiento(Rol rol) {
        Usuario usuario = usuarios.save(new Usuario("Ana", "ana@example.cl", "hash", rol, true));
        entityManager.flush();
        entityManager.clear();
        Usuario guardado = usuarios.findByEmail("ana@example.cl").orElseThrow();
        assertThat(guardado.getId()).isEqualTo(usuario.getId());
        assertThat(guardado.getRol()).isEqualTo(rol);
        assertThat(guardado.isAceptaLeyDatos()).isTrue();
    }

    @Test
    void conservaFichaRevisionYEliminacionLogica() {
        Usuario usuario = usuarios.save(new Usuario("Ana", "ana@example.cl", "hash", Rol.EMPRENDEDORA, true));
        Emprendedora ficha = emprendedoras.save(new Emprendedora(usuario, "123456785",
                "Taller Ana", "Metropolitana", "Santiago", List.of(Rubro.REPARACION), null));
        assertThat(ficha.getEstadoFicha()).isEqualTo(EstadoFichaEmprendimiento.POR_REVISAR);
        Instant fecha = Instant.parse("2026-11-21T12:00:00Z");
        ficha.revisar(EstadoFichaEmprendimiento.RECHAZADA, "Faltan antecedentes", fecha);
        entityManager.flush();
        entityManager.clear();
        Emprendedora guardada = emprendedoras.findByUsuarioId(usuario.getId()).orElseThrow();
        assertThat(guardada.getMotivoRevision()).isEqualTo("Faltan antecedentes");
        assertThat(guardada.getFechaRevision()).isEqualTo(fecha);
        assertThat(guardada.getEstadoFicha()).isEqualTo(EstadoFichaEmprendimiento.RECHAZADA);
        assertThat(guardada.getRubros()).containsExactly(Rubro.REPARACION);
        guardada.eliminar(fecha);
        entityManager.flush();
        entityManager.clear();
        Emprendedora eliminada = emprendedoras.findById(ficha.getId()).orElseThrow();
        assertThat(eliminada.isActiva()).isFalse();
        assertThat(eliminada.getEstadoFicha()).isEqualTo(EstadoFichaEmprendimiento.DADA_DE_BAJA);
        assertThat(usuarios.findById(usuario.getId()).orElseThrow().getFechaEliminacion()).isEqualTo(fecha);
    }

    @Test
    void rechazaCorreoDuplicado() {
        usuarios.save(new Usuario("Ana", "ana@example.cl", "hash", Rol.CLIENTE, true));
        entityManager.flush();
        assertThatThrownBy(() -> {
            usuarios.save(new Usuario("Otra", "ana@example.cl", "hash", Rol.EMPRENDEDORA, true));
            entityManager.flush();
        }).isInstanceOf(RuntimeException.class);
    }

    @Test
    void rechazaRutDuplicado() {
        Usuario ana = usuarios.save(new Usuario("Ana", "ana@example.cl", "hash", Rol.EMPRENDEDORA, true));
        Usuario eva = usuarios.save(new Usuario("Eva", "eva@example.cl", "hash", Rol.EMPRENDEDORA, true));
        emprendedoras.save(new Emprendedora(ana, "123456785", "Taller", "RM", "Santiago", List.of(), null));
        entityManager.flush();
        assertThatThrownBy(() -> {
            emprendedoras.save(new Emprendedora(eva, "123456785", "Otro", "RM", "Santiago", List.of(), null));
            entityManager.flush();
        }).isInstanceOf(RuntimeException.class);
    }
}
