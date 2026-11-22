package com.textilcircular.usuarios;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.textilcircular.usuarios.dto.EditarFichaAdminRequest;
import com.textilcircular.usuarios.dto.RegistroEmprendedoraRequest;
import com.textilcircular.usuarios.model.*;
import com.textilcircular.usuarios.repository.EmprendedoraRepository;
import com.textilcircular.usuarios.repository.UsuarioRepository;
import com.textilcircular.usuarios.security.TokenService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AdminEmprendedoraTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired UsuarioRepository usuarios;
    @Autowired EmprendedoraRepository emprendedoras;
    @Autowired TokenService tokens;
    @Autowired PasswordEncoder encoder;
    @Autowired EntityManager entityManager;

    private int contador = 0;

    private String tokenAdmin() {
        Usuario admin = usuarios.save(new Usuario("Admin", "admin" + (++contador) + "@textil.cl", "hash", Rol.ADMIN, true));
        return "Bearer " + tokens.generar(admin.getId(), Rol.ADMIN);
    }

    private String tokenCliente() {
        Usuario cliente = usuarios.save(new Usuario("Cliente", "cliente" + (++contador) + "@textil.cl", "hash", Rol.CLIENTE, true));
        return "Bearer " + tokens.generar(cliente.getId(), Rol.CLIENTE);
    }

    private Emprendedora crearFicha(String email, String rut, String nombreComercial,
                                    EstadoFichaEmprendimiento estado, String motivo) {
        Usuario usuario = usuarios.save(new Usuario("Emprendedora", email, encoder.encode("password123"),
                Rol.EMPRENDEDORA, true));
        Emprendedora ficha = new Emprendedora(usuario, rut, nombreComercial, "Metropolitana",
                "Santiago", List.of(Rubro.REPARACION), "@taller");
        ficha.revisar(estado, motivo, null);
        return emprendedoras.save(ficha);
    }

    @Test
    void listaTodasLasFichasYFiltraPorEstado() throws Exception {
        crearFicha("emp1@textil.cl", "111111111", "Taller Uno", EstadoFichaEmprendimiento.ACTIVA, null);
        crearFicha("emp2@textil.cl", "222222222", "Taller Dos", EstadoFichaEmprendimiento.POR_REVISAR, null);
        crearFicha("emp3@textil.cl", "333333333", "Taller Tres", EstadoFichaEmprendimiento.RECHAZADA, "Falta documentación");

        mvc.perform(get("/admin/emprendedoras").header("Authorization", tokenAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].rut").isString())
                .andExpect(jsonPath("$[0].email").isString())
                .andExpect(jsonPath("$[0].estadoFicha").isString());

        mvc.perform(get("/admin/emprendedoras?estado=RECHAZADA").header("Authorization", tokenAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nombreComercial").value("Taller Tres"))
                .andExpect(jsonPath("$[0].motivoRevision").value("Falta documentación"));
    }

    @Test
    void obtieneFichaPorIdConDatosCompletos() throws Exception {
        Emprendedora ficha = crearFicha("detalle@textil.cl", "444444444", "Taller Detalle",
                EstadoFichaEmprendimiento.POR_REVISAR, null);

        mvc.perform(get("/admin/emprendedoras/{id}", ficha.getId()).header("Authorization", tokenAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ficha.getId()))
                .andExpect(jsonPath("$.rut").value("444444444"))
                .andExpect(jsonPath("$.email").value("detalle@textil.cl"))
                .andExpect(jsonPath("$.nombreComercial").value("Taller Detalle"))
                .andExpect(jsonPath("$.estadoFicha").value("POR_REVISAR"));
    }

    @Test
    void creaEmprendedoraDesdeAdminDirectamenteActiva() throws Exception {
        RegistroEmprendedoraRequest solicitud = new RegistroEmprendedoraRequest(
                "Emprendedora Admin",
                "creada.admin@textil.cl",
                "secreto123",
                true,
                "12.345.678-5",
                "Taller Admin Creado",
                "Valparaíso",
                "Viña del Mar",
                List.of(Rubro.SUPRARRECICLAJE),
                "@admincreado"
        );

        mvc.perform(post("/admin/emprendedoras")
                        .header("Authorization", tokenAdmin())
                        .contentType("application/json")
                        .content(json.writeValueAsString(solicitud)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.estadoFicha").value("ACTIVA"))
                .andExpect(jsonPath("$.activa").value(true))
                .andExpect(jsonPath("$.rut").value("123456785"))
                .andExpect(jsonPath("$.email").value("creada.admin@textil.cl"));

        // Comprueba que puede iniciar sesión inmediatamente porque ya está ACTIVA
        mvc.perform(post("/auth/login")
                        .contentType("application/json")
                        .content("{\"email\":\"creada.admin@textil.cl\",\"password\":\"secreto123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString());
    }

    @Test
    void validaDatosAlCrearDesdeAdmin() throws Exception {
        RegistroEmprendedoraRequest rutInvalido = new RegistroEmprendedoraRequest(
                "Emprendedora Admin",
                "creada2@textil.cl",
                "secreto123",
                true,
                "11.111.111-2",
                "Taller Invalido",
                "Región",
                "Comuna",
                List.of(Rubro.REPARACION),
                null
        );

        mvc.perform(post("/admin/emprendedoras")
                        .header("Authorization", tokenAdmin())
                        .contentType("application/json")
                        .content(json.writeValueAsString(rutInvalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("DATOS_INVALIDOS"));
    }

    @Test
    void editaDatosDeFicha() throws Exception {
        Emprendedora ficha = crearFicha("editar@textil.cl", "555555555", "Nombre Original",
                EstadoFichaEmprendimiento.ACTIVA, null);

        EditarFichaAdminRequest edicion = new EditarFichaAdminRequest(
                "Nombre Modificado",
                "Biobío",
                "Concepción",
                List.of(Rubro.SUPRARRECICLAJE),
                "@nuevointa"
        );

        mvc.perform(put("/admin/emprendedoras/{id}", ficha.getId())
                        .header("Authorization", tokenAdmin())
                        .contentType("application/json")
                        .content(json.writeValueAsString(edicion)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombreComercial").value("Nombre Modificado"))
                .andExpect(jsonPath("$.region").value("Biobío"))
                .andExpect(jsonPath("$.comuna").value("Concepción"))
                .andExpect(jsonPath("$.instagram").value("@nuevointa"));

        entityManager.flush();
        entityManager.clear();

        Emprendedora guardada = emprendedoras.findById(ficha.getId()).orElseThrow();
        assertThat(guardada.getNombreComercial()).isEqualTo("Nombre Modificado");
        assertThat(guardada.getRegion()).isEqualTo("Biobío");
    }

    @Test
    void eliminacionLogicaDeFicha() throws Exception {
        Emprendedora ficha = crearFicha("eliminar@textil.cl", "666666666", "Para Eliminar",
                EstadoFichaEmprendimiento.ACTIVA, null);

        mvc.perform(delete("/admin/emprendedoras/{id}", ficha.getId())
                        .header("Authorization", tokenAdmin()))
                .andExpect(status().isNoContent());

        entityManager.flush();
        entityManager.clear();

        Emprendedora guardada = emprendedoras.findById(ficha.getId()).orElseThrow();
        assertThat(guardada.isActiva()).isFalse();
        assertThat(guardada.getEstadoFicha()).isEqualTo(EstadoFichaEmprendimiento.DADA_DE_BAJA);
        assertThat(guardada.getUsuario().getFechaEliminacion()).isNotNull();

        // En la vista de administración ya no se lista porque fue eliminada
        mvc.perform(get("/admin/emprendedoras/{id}", ficha.getId()).header("Authorization", tokenAdmin()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("EMPRENDEDORA_NO_ENCONTRADA"));
    }

    @Test
    void exigeRolAdminParaTodasLasOperaciones() throws Exception {
        String tokenCli = tokenCliente();

        mvc.perform(get("/admin/emprendedoras").header("Authorization", tokenCli))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.codigo").value("ACCESO_DENEGADO"));

        mvc.perform(get("/admin/emprendedoras/1").header("Authorization", tokenCli))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.codigo").value("ACCESO_DENEGADO"));

        mvc.perform(post("/admin/emprendedoras").header("Authorization", tokenCli)
                        .contentType("application/json").content("{}"))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.codigo").value("ACCESO_DENEGADO"));

        mvc.perform(put("/admin/emprendedoras/1").header("Authorization", tokenCli)
                        .contentType("application/json").content("{}"))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.codigo").value("ACCESO_DENEGADO"));

        mvc.perform(delete("/admin/emprendedoras/1").header("Authorization", tokenCli))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.codigo").value("ACCESO_DENEGADO"));
    }

    @Test
    void exigeTokenParaTodasLasOperaciones() throws Exception {
        mvc.perform(get("/admin/emprendedoras"))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.codigo").value("TOKEN_REQUERIDO"));

        mvc.perform(get("/admin/emprendedoras/1"))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.codigo").value("TOKEN_REQUERIDO"));

        mvc.perform(post("/admin/emprendedoras").contentType("application/json").content("{}"))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.codigo").value("TOKEN_REQUERIDO"));

        mvc.perform(put("/admin/emprendedoras/1").contentType("application/json").content("{}"))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.codigo").value("TOKEN_REQUERIDO"));

        mvc.perform(delete("/admin/emprendedoras/1"))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.codigo").value("TOKEN_REQUERIDO"));
    }
}
