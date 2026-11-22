package com.textilcircular.mercado.config;

import com.textilcircular.mercado.model.Conversacion;
import com.textilcircular.mercado.model.Mensaje;
import com.textilcircular.mercado.model.Producto;
import com.textilcircular.mercado.model.TipoEmisor;
import com.textilcircular.mercado.repository.ConversacionRepository;
import com.textilcircular.mercado.repository.MensajeRepository;
import com.textilcircular.mercado.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;

@Component
public class DatosIniciales implements CommandLineRunner {

    // =========================================================================
    // MAPEO DE IDS DE USUARIO (provienen de ms-usuarios, donde cada usuario
    // tiene un ID correlativo fijo: ADMIN 1, Emprendedoras 2 a 13, Clientes 14 y 15).
    // Los productos demo de la app original usaban el id de ficha (1 a 8),
    // que aquí corresponde al id de usuario sumando 1 (fichaId + 1).
    // =========================================================================
    public static final Long EMPRENDEDORA_CAMILA = 2L;     // Ficha 1 en la app
    public static final Long EMPRENDEDORA_MARIA = 3L;      // Ficha 2 en la app
    public static final Long EMPRENDEDORA_DANIELA = 4L;    // Ficha 3 en la app
    public static final Long EMPRENDEDORA_CLAUDIA = 5L;    // Ficha 4 en la app
    public static final Long EMPRENDEDORA_VALENTINA = 6L;  // Ficha 5 en la app
    public static final Long EMPRENDEDORA_LORETO = 7L;     // Ficha 6 en la app
    public static final Long EMPRENDEDORA_CARMEN = 8L;     // Ficha 7 en la app
    public static final Long EMPRENDEDORA_LUCIA = 9L;      // Ficha 8 en la app

    public static final Long CLIENTE_FRANCISCA = 14L;
    public static final Long CLIENTE_RODRIGO = 15L;

    private final ProductoRepository productos;
    private final ConversacionRepository conversaciones;
    private final MensajeRepository mensajes;
    private final boolean habilitada;

    public DatosIniciales(ProductoRepository productos,
                          ConversacionRepository conversaciones,
                          MensajeRepository mensajes,
                          @Value("${mercado.semilla.habilitada:true}") boolean habilitada) {
        this.productos = productos;
        this.conversaciones = conversaciones;
        this.mensajes = mensajes;
        this.habilitada = habilitada;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (habilitada) {
            sembrarSiEstaVacia();
        }
    }

    @Transactional
    public void sembrarSiEstaVacia() {
        if (productos.count() > 0 || conversaciones.count() > 0) {
            return;
        }

        Instant ahora = Instant.now();

        // 1. Catálogo de creaciones textiles para emprendedoras activas
        Producto p1 = productos.save(new Producto(
                EMPRENDEDORA_CAMILA,
                "Bolso Upcycling Denim",
                "Bolso resistente confeccionado a partir de 3 pantalones de jeans recuperados.",
                "Desarmamos jeans en desuso, lavamos con bajo consumo y reforzamos uniones."
        ));
        Producto p2 = productos.save(new Producto(
                EMPRENDEDORA_CAMILA,
                "Cojín Textil Parche",
                "Cojín decorativo relleno con retazos de algodón y lino recuperado.",
                "Aprovechamiento integral de descartes de sastrerías locales."
        ));
        productos.save(new Producto(
                EMPRENDEDORA_CAMILA,
                "Chaqueta Intervenida Bordada",
                "Chaqueta vintage de segunda mano con bordados a mano inspirados en flora local.",
                "Restauración de botones y bordado con hilo de lana natural."
        ));

        productos.save(new Producto(
                EMPRENDEDORA_MARIA,
                "Vestido Vintage Años 90",
                "Vestido estampado floral en excelente estado de conservación.",
                null
        ));
        productos.save(new Producto(
                EMPRENDEDORA_MARIA,
                "Camisa de Seda Rescatada",
                "Camisa clásica de seda natural con botones originales recuperados.",
                null
        ));

        productos.save(new Producto(
                EMPRENDEDORA_DANIELA,
                "Mochila Retazos del Desierto",
                "Mochila urbana fabricada con telas recuperadas y correas de seguridad reutilizadas.",
                null
        ));
        productos.save(new Producto(
                EMPRENDEDORA_DANIELA,
                "Estuche Mezclilla Arica",
                "Estuche multiuso reforzado con cierre metálico reciclado.",
                null
        ));

        productos.save(new Producto(
                EMPRENDEDORA_CLAUDIA,
                "Funda Cojín Minero",
                "Funda tejida con excedentes textiles industriales de la zona norte.",
                null
        ));
        productos.save(new Producto(
                EMPRENDEDORA_CLAUDIA,
                "Taller Costura Inicial",
                "Sesión práctica de reparación básica de prendas de vestir.",
                null
        ));

        productos.save(new Producto(
                EMPRENDEDORA_VALENTINA,
                "Vestido Gala para Arriendo",
                "Prenda de fiesta disponible para arriendo por fin de semana.",
                null
        ));
        productos.save(new Producto(
                EMPRENDEDORA_VALENTINA,
                "Tapado Fiesta Reutilizado",
                "Tapado elegante de segunda mano higienizado y listo para usar.",
                null
        ));

        productos.save(new Producto(
                EMPRENDEDORA_LORETO,
                "Arreglo y Ajuste de Pantalones",
                "Servicio de basta, entalle y cambio de cierres para prolongar vida útil.",
                null
        ));

        productos.save(new Producto(
                EMPRENDEDORA_CARMEN,
                "Manta de Parches Temuco",
                "Manta abrigadora armada con retazos de polar y lana recuperada.",
                null
        ));
        productos.save(new Producto(
                EMPRENDEDORA_CARMEN,
                "Chaleco Lana Segunda Mano",
                "Chaleco artesanal tejido en lana chilota restaurado a mano.",
                null
        ));

        productos.save(new Producto(
                EMPRENDEDORA_LUCIA,
                "Gorro Lana Merino Magallanes",
                "Gorro térmico confeccionado con sobrantes de hilados patagónicos.",
                null
        ));
        productos.save(new Producto(
                EMPRENDEDORA_LUCIA,
                "Taller Hilado Circular",
                "Clase grupal para transformar retazos delgados en hilo utilizable.",
                null
        ));

        // 2. Conversaciones demo
        // Conversación 1: Francisca Tapia con Camila Morales (Taller Hilos del Sur)
        Conversacion c1 = new Conversacion(
                CLIENTE_FRANCISCA,
                EMPRENDEDORA_CAMILA,
                "Francisca Tapia",
                "Taller Hilos del Sur",
                p1.getId(),
                ahora.minusSeconds(900)
        );
        c1 = conversaciones.save(c1);

        mensajes.save(new Mensaje(
                c1.getId(),
                TipoEmisor.CLIENTE,
                "Quiero saber más sobre este producto: Bolso Upcycling Denim",
                ahora.minusSeconds(900),
                true,
                p1.getId(),
                p1.getNombre()
        ));
        mensajes.save(new Mensaje(
                c1.getId(),
                TipoEmisor.EMPRENDEDORA,
                "¡Hola Francisca! Claro que sí, es de mezclilla gruesa reforzada y tiene bolsillo interior.",
                ahora.minusSeconds(600),
                true,
                null,
                null
        ));
        mensajes.save(new Mensaje(
                c1.getId(),
                TipoEmisor.CLIENTE,
                "¡Hola! ¿Hacen envíos a Viña del Mar o entregas presenciales?",
                ahora.minusSeconds(300),
                false,
                null,
                null
        ));
        c1.registrarMensaje("¡Hola! ¿Hacen envíos a Viña del Mar o entregas presenciales?", ahora.minusSeconds(300), true);

        // Conversación 2: Rodrigo San Martín con Camila Morales
        Conversacion c2 = new Conversacion(
                CLIENTE_RODRIGO,
                EMPRENDEDORA_CAMILA,
                "Rodrigo San Martín",
                "Taller Hilos del Sur",
                p2.getId(),
                ahora.minusSeconds(86400)
        );
        c2 = conversaciones.save(c2);

        mensajes.save(new Mensaje(
                c2.getId(),
                TipoEmisor.CLIENTE,
                "Muchas gracias Camila, me encantó el bolso.",
                ahora.minusSeconds(86400),
                true,
                p2.getId(),
                p2.getNombre()
        ));
        c2.registrarMensaje("Muchas gracias Camila, me encantó el bolso.", ahora.minusSeconds(86400), true);
        c2.marcarLeidaPorEmprendedora();
    }
}
