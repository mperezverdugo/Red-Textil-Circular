package com.textilcircular.mercado;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.textilcircular.mercado.dto.CrearProductoRequest;
import com.textilcircular.mercado.dto.EditarProductoRequest;
import com.textilcircular.mercado.model.Producto;
import com.textilcircular.mercado.model.Rol;
import com.textilcircular.mercado.repository.ProductoRepository;
import com.textilcircular.mercado.security.TokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ProductoTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired ProductoRepository productos;
    @Autowired TokenService tokens;

    private String token(Long usuarioId, Rol rol) {
        return "Bearer " + tokens.generar(usuarioId, rol);
    }

    @Test
    void altaDeProductoPorEmprendedora() throws Exception {
        CrearProductoRequest solicitud = new CrearProductoRequest(
                "Bolso Upcycling",
                "Bolso resistente confeccionado a partir de jeans recuperados.",
                "Desarmamos jeans en desuso y reforzamos uniones."
        );

        mvc.perform(post("/productos")
                        .header("Authorization", token(2L, Rol.EMPRENDEDORA))
                        .contentType("application/json")
                        .content(json.writeValueAsString(solicitud)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.emprendedoraId").value(2))
                .andExpect(jsonPath("$.nombre").value("Bolso Upcycling"))
                .andExpect(jsonPath("$.descripcionCorta").value("Bolso resistente confeccionado a partir de jeans recuperados."))
                .andExpect(jsonPath("$.explicacionProceso").value("Desarmamos jeans en desuso y reforzamos uniones."));
    }

    @Test
    void edicionDeProductoPorDuena() throws Exception {
        Producto producto = productos.save(new Producto(2L, "Original", "Descripción válida de diez caracteres", null));

        EditarProductoRequest solicitud = new EditarProductoRequest(
                "Modificado",
                "Nueva descripción válida con longitud suficiente.",
                "Proceso artesanal detallado"
        );

        mvc.perform(put("/productos/{id}", producto.getId())
                        .header("Authorization", token(2L, Rol.EMPRENDEDORA))
                        .contentType("application/json")
                        .content(json.writeValueAsString(solicitud)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Modificado"))
                .andExpect(jsonPath("$.descripcionCorta").value("Nueva descripción válida con longitud suficiente."))
                .andExpect(jsonPath("$.explicacionProceso").value("Proceso artesanal detallado"));

        Producto guardado = productos.findById(producto.getId()).orElseThrow();
        assertThat(guardado.getNombre()).isEqualTo("Modificado");
    }

    @Test
    void bajaDeProductoPorDuena() throws Exception {
        Producto producto = productos.save(new Producto(2L, "Para Borrar", "Descripción válida de diez caracteres", null));

        mvc.perform(delete("/productos/{id}", producto.getId())
                        .header("Authorization", token(2L, Rol.EMPRENDEDORA)))
                .andExpect(status().isNoContent());

        assertThat(productos.findById(producto.getId())).isEmpty();
    }

    @Test
    void otroUsuarioNoPuedeEditarNiBorrar() throws Exception {
        Producto producto = productos.save(new Producto(2L, "Original", "Descripción válida de diez caracteres", null));

        EditarProductoRequest edicion = new EditarProductoRequest(
                "Intento", "Descripción válida de diez caracteres", null);

        // Otra emprendedora intenta editar
        mvc.perform(put("/productos/{id}", producto.getId())
                        .header("Authorization", token(3L, Rol.EMPRENDEDORA))
                        .contentType("application/json")
                        .content(json.writeValueAsString(edicion)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("ACCESO_DENEGADO"));

        // Otra emprendedora intenta borrar
        mvc.perform(delete("/productos/{id}", producto.getId())
                        .header("Authorization", token(3L, Rol.EMPRENDEDORA)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("ACCESO_DENEGADO"));
    }

    @Test
    void clienteNoPuedeCrearProducto() throws Exception {
        CrearProductoRequest solicitud = new CrearProductoRequest(
                "Bolso Upcycling", "Descripción válida de diez caracteres", null);

        mvc.perform(post("/productos")
                        .header("Authorization", token(14L, Rol.CLIENTE))
                        .contentType("application/json")
                        .content(json.writeValueAsString(solicitud)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("ACCESO_DENEGADO"));

        mvc.perform(post("/productos")
                        .header("Authorization", token(1L, Rol.ADMIN))
                        .contentType("application/json")
                        .content(json.writeValueAsString(solicitud)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("ACCESO_DENEGADO"));
    }

    @Test
    void rechazaDatosInvalidos() throws Exception {
        CrearProductoRequest nombreCorto = new CrearProductoRequest(
                "Ab", "Descripción válida de diez caracteres", null);

        mvc.perform(post("/productos")
                        .header("Authorization", token(2L, Rol.EMPRENDEDORA))
                        .contentType("application/json")
                        .content(json.writeValueAsString(nombreCorto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("DATOS_INVALIDOS"));

        CrearProductoRequest descCorta = new CrearProductoRequest(
                "Nombre Válido", "Corta", null);

        mvc.perform(post("/productos")
                        .header("Authorization", token(2L, Rol.EMPRENDEDORA))
                        .contentType("application/json")
                        .content(json.writeValueAsString(descCorta)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("DATOS_INVALIDOS"));
    }

    @Test
    void consultaPorEmprendedoraDevuelveSoloSusProductos() throws Exception {
        productos.save(new Producto(2L, "Prod 1 Emp 2", "Descripción válida de diez caracteres", null));
        productos.save(new Producto(2L, "Prod 2 Emp 2", "Descripción válida de diez caracteres", null));
        productos.save(new Producto(3L, "Prod 1 Emp 3", "Descripción válida de diez caracteres", null));

        mvc.perform(get("/productos")
                        .param("emprendedoraId", "2")
                        .header("Authorization", token(14L, Rol.CLIENTE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].nombre").value("Prod 1 Emp 2"))
                .andExpect(jsonPath("$[1].nombre").value("Prod 2 Emp 2"));
    }

    @Test
    void detalleYListarMios() throws Exception {
        Producto p1 = productos.save(new Producto(2L, "Mi Producto 1", "Descripción válida de diez caracteres", null));
        productos.save(new Producto(3L, "Ajeno", "Descripción válida de diez caracteres", null));

        // Detalle por id
        mvc.perform(get("/productos/{id}", p1.getId())
                        .header("Authorization", token(14L, Rol.CLIENTE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(p1.getId()))
                .andExpect(jsonPath("$.nombre").value("Mi Producto 1"));

        // Listar mios como emprendedora 2
        mvc.perform(get("/productos/mios")
                        .header("Authorization", token(2L, Rol.EMPRENDEDORA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nombre").value("Mi Producto 1"));

        // Listar mios como cliente da 403
        mvc.perform(get("/productos/mios")
                        .header("Authorization", token(14L, Rol.CLIENTE)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("ACCESO_DENEGADO"));
    }

    @Test
    void devuelve404ParaProductoInexistente() throws Exception {
        mvc.perform(get("/productos/99999")
                        .header("Authorization", token(2L, Rol.EMPRENDEDORA)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("PRODUCTO_NO_ENCONTRADO"));
    }

    @Test
    void emprendedoraCreaEditaYEliminaProductoPersistiendoEnH2() throws Exception {
        CrearProductoRequest nuevo = new CrearProductoRequest(
                "Cartera Jeans Reciclada",
                "Cartera artesanal confeccionada con retazos de mezclilla.",
                "Corte manual y costura reforzada."
        );

        String creadoJson = mvc.perform(post("/productos")
                        .header("Authorization", token(16L, Rol.EMPRENDEDORA))
                        .contentType("application/json")
                        .content(json.writeValueAsString(nuevo)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.emprendedoraId").value(16))
                .andExpect(jsonPath("$.nombre").value("Cartera Jeans Reciclada"))
                .andReturn().getResponse().getContentAsString();

        Long prodId = json.readTree(creadoJson).get("id").asLong();

        // Verifica que se consulta inmediatamente por emprendedoraId
        mvc.perform(get("/productos")
                        .param("emprendedoraId", "16")
                        .header("Authorization", token(14L, Rol.CLIENTE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(prodId));

        assertThat(productos.findById(prodId)).isPresent();
    }
}
