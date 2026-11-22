package com.textilcircular.usuarios;

import com.textilcircular.usuarios.model.*;
import com.textilcircular.usuarios.repository.*;
import com.textilcircular.usuarios.security.TokenService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ListadoEmprendedorasTest {
    @Autowired MockMvc mvc;
    @Autowired UsuarioRepository usuarios;
    @Autowired EmprendedoraRepository emprendedoras;
    @Autowired TokenService tokens;
    @Autowired EntityManager entityManager;
    private String autorizacion;
    private Long tallerId;

    @BeforeEach
    void prepararDatos() {
        Usuario cliente = usuarios.save(new Usuario("Cliente", "cliente@example.cl", "hash", Rol.CLIENTE, true));
        autorizacion = "Bearer " + tokens.generar(cliente.getId(), Rol.CLIENTE);
        tallerId = crearFicha("1", "Taller Circular", "Metropolitana", "Santiago",
                List.of(Rubro.REPARACION, Rubro.TALLERES), EstadoFichaEmprendimiento.ACTIVA).getId();
        crearFicha("2", "Ropa Circular", "Metropolitana", "Providencia",
                List.of(Rubro.SEGUNDA_MANO), EstadoFichaEmprendimiento.ACTIVA);
        crearFicha("3", "Costuras del Puerto", "Valparaíso", "Valparaíso",
                List.of(Rubro.REPARACION), EstadoFichaEmprendimiento.ACTIVA);
        entityManager.flush();
        entityManager.clear();
    }

    private Emprendedora crearFicha(String numero, String nombre, String region, String comuna,
                                    List<Rubro> rubros, EstadoFichaEmprendimiento estado) {
        Usuario usuario = usuarios.save(new Usuario("Ana", numero + "@example.cl", "hash", Rol.EMPRENDEDORA, true));
        Emprendedora ficha = new Emprendedora(usuario, numero, nombre, region, comuna, rubros, "taller");
        ficha.revisar(estado, "Revisión", Instant.now());
        return emprendedoras.save(ficha);
    }

    @Test
    void listaActivasSinDatosPrivadosNiDuplicadosPorRubro() throws Exception {
        mvc.perform(get("/emprendedoras").header("Authorization", autorizacion))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].nombreComercial").value("Taller Circular"))
                .andExpect(jsonPath("$[0].rubros.length()").value(2))
                .andExpect(jsonPath("$[0].rut").doesNotExist())
                .andExpect(jsonPath("$[0].email").doesNotExist())
                .andExpect(jsonPath("$[0].usuario").doesNotExist())
                .andExpect(jsonPath("$[0].motivoRevision").doesNotExist());
    }

    @Test
    void filtraTextoParcialSinDistinguirMayusculas() throws Exception {
        mvc.perform(get("/emprendedoras").param("texto", "cIRcuLAR").header("Authorization", autorizacion))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void filtraRegion() throws Exception {
        mvc.perform(get("/emprendedoras").param("region", "Valparaíso").header("Authorization", autorizacion))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nombreComercial").value("Costuras del Puerto"));
    }

    @Test
    void filtraComuna() throws Exception {
        mvc.perform(get("/emprendedoras").param("comuna", "Providencia").header("Authorization", autorizacion))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nombreComercial").value("Ropa Circular"));
    }

    @Test
    void filtraRubro() throws Exception {
        mvc.perform(get("/emprendedoras").param("rubro", "REPARACION").header("Authorization", autorizacion))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void combinaTodosLosFiltros() throws Exception {
        mvc.perform(get("/emprendedoras").param("texto", "CIRCULAR").param("region", "Metropolitana")
                .param("comuna", "Santiago").param("rubro", "TALLERES").header("Authorization", autorizacion))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(tallerId));
    }

    @Test
    void devuelveListaVaciaCuandoNoCoincidenLosFiltros() throws Exception {
        mvc.perform(get("/emprendedoras").param("region", "Valparaíso").param("comuna", "Santiago")
                .header("Authorization", autorizacion))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void ignoraFiltrosVacios() throws Exception {
        mvc.perform(get("/emprendedoras").param("texto", " ").param("region", "").param("comuna", "")
                .param("rubro", "").header("Authorization", autorizacion))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(3));
    }

    @ParameterizedTest
    @EnumSource(value = EstadoFichaEmprendimiento.class, names = {"POR_REVISAR", "OCULTA", "RECHAZADA", "DADA_DE_BAJA"})
    void noExponeFichaNoActiva(EstadoFichaEmprendimiento estado) throws Exception {
        Emprendedora ficha = crearFicha("4", "Ficha privada", "RM", "Santiago", List.of(Rubro.TALLERES), estado);
        mvc.perform(get("/emprendedoras").header("Authorization", autorizacion))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(3));
        mvc.perform(get("/emprendedoras/{id}", ficha.getId()).header("Authorization", autorizacion))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("EMPRENDEDORA_NO_ENCONTRADA"));
    }

    @Test
    void obtieneDetalleActivoSinDatosPrivados() throws Exception {
        mvc.perform(get("/emprendedoras/{id}", tallerId).header("Authorization", autorizacion))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(tallerId))
                .andExpect(jsonPath("$.rubros.length()").value(2))
                .andExpect(jsonPath("$.instagram").value("taller"))
                .andExpect(jsonPath("$.rut").doesNotExist()).andExpect(jsonPath("$.email").doesNotExist());
    }

    @Test
    void devuelve404ParaIdInexistente() throws Exception {
        mvc.perform(get("/emprendedoras/{id}", Long.MAX_VALUE).header("Authorization", autorizacion))
                .andExpect(status().isNotFound());
    }

    @Test
    void rechazaRubroInvalido() throws Exception {
        mvc.perform(get("/emprendedoras").param("rubro", "DESCONOCIDO").header("Authorization", autorizacion))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.codigo").value("DATOS_INVALIDOS"));
    }

    @Test
    void mantieneAutenticacionEnListadoYDetalle() throws Exception {
        mvc.perform(get("/emprendedoras")).andExpect(status().isUnauthorized());
        mvc.perform(get("/emprendedoras/{id}", tallerId)).andExpect(status().isUnauthorized());
    }

    @Test
    void noExponeFichaSiSuCuentaFueDadaDeBaja() throws Exception {
        Emprendedora ficha = emprendedoras.findById(tallerId).orElseThrow();
        ficha.getUsuario().darDeBaja(Instant.now());
        mvc.perform(get("/emprendedoras").header("Authorization", autorizacion))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(2));
        mvc.perform(get("/emprendedoras/{id}", tallerId).header("Authorization", autorizacion))
                .andExpect(status().isNotFound());
    }
}
