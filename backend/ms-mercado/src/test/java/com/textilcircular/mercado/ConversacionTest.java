package com.textilcircular.mercado;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.textilcircular.mercado.dto.CrearConversacionRequest;
import com.textilcircular.mercado.dto.EnviarMensajeRequest;
import com.textilcircular.mercado.model.Conversacion;
import com.textilcircular.mercado.model.Mensaje;
import com.textilcircular.mercado.model.Rol;
import com.textilcircular.mercado.repository.ConversacionRepository;
import com.textilcircular.mercado.repository.MensajeRepository;
import com.textilcircular.mercado.security.TokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ConversacionTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired ConversacionRepository conversaciones;
    @Autowired MensajeRepository mensajes;
    @Autowired TokenService tokens;

    private String token(Long usuarioId, Rol rol) {
        return "Bearer " + tokens.generar(usuarioId, rol);
    }

    @Test
    void crearNuevaConversacionYReutilizarExistente() throws Exception {
        CrearConversacionRequest solicitud = new CrearConversacionRequest(
                2L, "Francisca Tapia", "Taller Hilos del Sur", 101L);

        // 1. Crear nueva
        mvc.perform(post("/conversaciones")
                        .header("Authorization", token(14L, Rol.CLIENTE))
                        .contentType("application/json")
                        .content(json.writeValueAsString(solicitud)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.clienteId").value(14))
                .andExpect(jsonPath("$.emprendedoraId").value(2))
                .andExpect(jsonPath("$.nombreCliente").value("Francisca Tapia"))
                .andExpect(jsonPath("$.nombreEmprendimiento").value("Taller Hilos del Sur"));

        assertThat(conversaciones.count()).isEqualTo(1);

        // 2. Reutilizar existente
        mvc.perform(post("/conversaciones")
                        .header("Authorization", token(14L, Rol.CLIENTE))
                        .contentType("application/json")
                        .content(json.writeValueAsString(solicitud)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clienteId").value(14))
                .andExpect(jsonPath("$.emprendedoraId").value(2));

        assertThat(conversaciones.count()).isEqualTo(1);
    }

    @Test
    void emprendedoraNoPuedeCrearConversacion() throws Exception {
        CrearConversacionRequest solicitud = new CrearConversacionRequest(
                2L, "Cliente", "Emprendimiento", null);

        mvc.perform(post("/conversaciones")
                        .header("Authorization", token(2L, Rol.EMPRENDEDORA))
                        .contentType("application/json")
                        .content(json.writeValueAsString(solicitud)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("ACCESO_DENEGADO"));
    }

    @Test
    void listarSoloLasPropias() throws Exception {
        conversaciones.save(new Conversacion(14L, 2L, "Francisca", "Taller 2", null, Instant.now()));
        conversaciones.save(new Conversacion(15L, 3L, "Rodrigo", "Taller 3", null, Instant.now()));

        // Francisca (14) solo ve la suya
        mvc.perform(get("/conversaciones")
                        .header("Authorization", token(14L, Rol.CLIENTE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nombreCliente").value("Francisca"));

        // Emprendedora 2 solo ve la suya
        mvc.perform(get("/conversaciones")
                        .header("Authorization", token(2L, Rol.EMPRENDEDORA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nombreEmprendimiento").value("Taller 2"));
    }

    @Test
    void envioDeMensajesYContadoresDeNoLeidos() throws Exception {
        Conversacion c = conversaciones.save(new Conversacion(
                14L, 2L, "Francisca", "Taller", null, Instant.now()));

        // Cliente envía mensaje
        mvc.perform(post("/conversaciones/{id}/mensajes", c.getId())
                        .header("Authorization", token(14L, Rol.CLIENTE))
                        .contentType("application/json")
                        .content(json.writeValueAsString(new EnviarMensajeRequest("Hola, consulta", null, null))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.emisor").value("CLIENTE"))
                .andExpect(jsonPath("$.contenido").value("Hola, consulta"));

        Conversacion actualizada = conversaciones.findById(c.getId()).orElseThrow();
        assertThat(actualizada.getUltimoMensaje()).isEqualTo("Hola, consulta");
        assertThat(actualizada.getNoLeidosEmprendedora()).isEqualTo(1);
        assertThat(actualizada.getNoLeidosCliente()).isEqualTo(0);

        // Emprendedora responde
        mvc.perform(post("/conversaciones/{id}/mensajes", c.getId())
                        .header("Authorization", token(2L, Rol.EMPRENDEDORA))
                        .contentType("application/json")
                        .content(json.writeValueAsString(new EnviarMensajeRequest("Hola Francisca, dime", null, null))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.emisor").value("EMPRENDEDORA"));

        actualizada = conversaciones.findById(c.getId()).orElseThrow();
        assertThat(actualizada.getUltimoMensaje()).isEqualTo("Hola Francisca, dime");
        assertThat(actualizada.getNoLeidosEmprendedora()).isEqualTo(1);
        assertThat(actualizada.getNoLeidosCliente()).isEqualTo(1);
    }

    @Test
    void lecturaPoneContadorEnCeroYMarcaMensajes() throws Exception {
        Conversacion c = conversaciones.save(new Conversacion(
                14L, 2L, "Francisca", "Taller", null, Instant.now()));

        // Enviar mensaje del cliente
        mvc.perform(post("/conversaciones/{id}/mensajes", c.getId())
                .header("Authorization", token(14L, Rol.CLIENTE))
                .contentType("application/json")
                .content("{\"contenido\":\"Mensaje para la emprendedora\"}"));

        // Emprendedora marca lectura
        mvc.perform(patch("/conversaciones/{id}/lectura", c.getId())
                        .header("Authorization", token(2L, Rol.EMPRENDEDORA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.noLeidosEmprendedora").value(0));

        Conversacion actualizada = conversaciones.findById(c.getId()).orElseThrow();
        assertThat(actualizada.getNoLeidosEmprendedora()).isEqualTo(0);

        List<Mensaje> msgs = mensajes.findByConversacionIdOrderByIdAsc(c.getId());
        assertThat(msgs.get(0).isEsLeido()).isTrue();
    }

    @Test
    void despuesDeIdFiltraMensajesNuevos() throws Exception {
        Conversacion c = conversaciones.save(new Conversacion(
                14L, 2L, "Francisca", "Taller", null, Instant.now()));

        mvc.perform(post("/conversaciones/{id}/mensajes", c.getId())
                .header("Authorization", token(14L, Rol.CLIENTE))
                .contentType("application/json").content("{\"contenido\":\"Msg 1\"}"));

        mvc.perform(post("/conversaciones/{id}/mensajes", c.getId())
                .header("Authorization", token(14L, Rol.CLIENTE))
                .contentType("application/json").content("{\"contenido\":\"Msg 2\"}"));

        List<Mensaje> todos = mensajes.findByConversacionIdOrderByIdAsc(c.getId());
        Long primerId = todos.get(0).getId();

        mvc.perform(get("/conversaciones/{id}/mensajes", c.getId())
                        .param("despuesDeId", primerId.toString())
                        .header("Authorization", token(2L, Rol.EMPRENDEDORA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].contenido").value("Msg 2"));
    }

    @Test
    void terceroAjenoRecibe403() throws Exception {
        Conversacion c = conversaciones.save(new Conversacion(
                14L, 2L, "Francisca", "Taller", null, Instant.now()));

        // Usuario 15 no es participante
        mvc.perform(post("/conversaciones/{id}/mensajes", c.getId())
                        .header("Authorization", token(15L, Rol.CLIENTE))
                        .contentType("application/json").content("{\"contenido\":\"Espía\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("ACCESO_DENEGADO"));

        mvc.perform(get("/conversaciones/{id}/mensajes", c.getId())
                        .header("Authorization", token(15L, Rol.CLIENTE)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("ACCESO_DENEGADO"));

        mvc.perform(patch("/conversaciones/{id}/lectura", c.getId())
                        .header("Authorization", token(15L, Rol.CLIENTE)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("ACCESO_DENEGADO"));
    }

    @Test
    void validacionesDeMensajeYConversacionInexistente() throws Exception {
        mvc.perform(post("/conversaciones/99999/mensajes")
                        .header("Authorization", token(14L, Rol.CLIENTE))
                        .contentType("application/json").content("{\"contenido\":\"Hola\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("CONVERSACION_NO_ENCONTRADA"));

        Conversacion c = conversaciones.save(new Conversacion(
                14L, 2L, "Francisca", "Taller", null, Instant.now()));

        mvc.perform(post("/conversaciones/{id}/mensajes", c.getId())
                        .header("Authorization", token(14L, Rol.CLIENTE))
                        .contentType("application/json").content("{\"contenido\":\"   \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("DATOS_INVALIDOS"));

        mvc.perform(post("/conversaciones/{id}/mensajes", c.getId())
                        .header("Authorization", token(14L, Rol.CLIENTE))
                        .contentType("application/json").content("{\"contenido\":\"" + "A".repeat(1001) + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("DATOS_INVALIDOS"));
    }

    @Test
    void rechazaCrearConversacionConDatosInvalidos() throws Exception {
        mvc.perform(post("/conversaciones")
                        .header("Authorization", token(14L, Rol.CLIENTE))
                        .contentType("application/json").content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("DATOS_INVALIDOS"));
    }

    @Test
    void flujoClienteDemoCrearConversacionEnviarYLeerMensajes() throws Exception {
        // Cliente demo (usuarioId = 14) inicia conversación con emprendedora demo (usuarioId = 2)
        CrearConversacionRequest solicitud = new CrearConversacionRequest(
                2L, "Francisca Tapia", "Taller Hilos del Sur", 101L);

        String respConv = mvc.perform(post("/conversaciones")
                        .header("Authorization", token(14L, Rol.CLIENTE))
                        .contentType("application/json")
                        .content(json.writeValueAsString(solicitud)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clienteId").value(14))
                .andExpect(jsonPath("$.emprendedoraId").value(2))
                .andReturn().getResponse().getContentAsString();

        Long convId = json.readTree(respConv).get("id").asLong();

        // Cliente demo envía un mensaje y se persiste inmediatamente con 201
        mvc.perform(post("/conversaciones/{id}/mensajes", convId)
                        .header("Authorization", token(14L, Rol.CLIENTE))
                        .contentType("application/json")
                        .content(json.writeValueAsString(new EnviarMensajeRequest("Hola, consulta desde cliente demo", 101L, "Bolso Upcycling Denim"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.emisor").value("CLIENTE"))
                .andExpect(jsonPath("$.contenido").value("Hola, consulta desde cliente demo"));

        // Al listar conversaciones y mensajes del cliente demo aparece inmediatamente
        mvc.perform(get("/conversaciones")
                        .header("Authorization", token(14L, Rol.CLIENTE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(convId))
                .andExpect(jsonPath("$[0].ultimoMensaje").value("Hola, consulta desde cliente demo"));

        mvc.perform(get("/conversaciones/{id}/mensajes", convId)
                        .header("Authorization", token(14L, Rol.CLIENTE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].contenido").value("Hola, consulta desde cliente demo"));
    }
}
