package com.textilcircular.mercado;

import com.textilcircular.mercado.config.DatosIniciales;
import com.textilcircular.mercado.model.Conversacion;
import com.textilcircular.mercado.model.Producto;
import com.textilcircular.mercado.repository.ConversacionRepository;
import com.textilcircular.mercado.repository.MensajeRepository;
import com.textilcircular.mercado.repository.ProductoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DatosInicialesTest {

    @Autowired ProductoRepository productos;
    @Autowired ConversacionRepository conversaciones;
    @Autowired MensajeRepository mensajes;
    @Autowired DatosIniciales datosIniciales;

    @Test
    void cargaSemillaCorrectamente() {
        datosIniciales.sembrarSiEstaVacia();

        assertThat(productos.count()).isEqualTo(16);
        assertThat(conversaciones.count()).isEqualTo(2);
        assertThat(mensajes.count()).isEqualTo(4);

        // Productos de Camila (ID 2)
        List<Producto> productosCamila = productos.findByEmprendedoraIdOrderByIdAsc(DatosIniciales.EMPRENDEDORA_CAMILA);
        assertThat(productosCamila).hasSize(3);
        assertThat(productosCamila.get(0).getNombre()).isEqualTo("Bolso Upcycling Denim");

        // Conversación de Francisca con Camila
        Conversacion c1 = conversaciones.findByClienteIdAndEmprendedoraId(
                DatosIniciales.CLIENTE_FRANCISCA, DatosIniciales.EMPRENDEDORA_CAMILA).orElseThrow();
        assertThat(c1.getNombreCliente()).isEqualTo("Francisca Tapia");
        assertThat(c1.getNoLeidosEmprendedora()).isGreaterThan(0);
    }

    @Test
    void noDuplicaDatosAlEjecutarOtraVez() {
        datosIniciales.sembrarSiEstaVacia();
        long totalProds = productos.count();
        long totalConvs = conversaciones.count();
        long totalMsgs = mensajes.count();

        datosIniciales.sembrarSiEstaVacia();

        assertThat(productos.count()).isEqualTo(totalProds);
        assertThat(conversaciones.count()).isEqualTo(totalConvs);
        assertThat(mensajes.count()).isEqualTo(totalMsgs);
    }
}
