package com.textilcircular.mercado.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "mensajes", indexes = {
        @Index(name = "idx_mensajes_conversacion", columnList = "conversacion_id")
})
public class Mensaje {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "conversacion_id", nullable = false)
    private Long conversacionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoEmisor emisor;

    @Column(nullable = false, length = 1000)
    private String contenido;

    @Column(name = "fecha_hora", nullable = false)
    private Instant fechaHora;

    @Column(name = "es_leido", nullable = false)
    private boolean esLeido = false;

    @Column(name = "producto_id")
    private Long productoId;

    @Column(name = "nombre_producto")
    private String nombreProducto;

    protected Mensaje() {}

    public Mensaje(Long conversacionId, TipoEmisor emisor, String contenido,
                   Instant fechaHora, boolean esLeido, Long productoId, String nombreProducto) {
        this.conversacionId = conversacionId;
        this.emisor = emisor;
        this.contenido = contenido;
        this.fechaHora = fechaHora;
        this.esLeido = esLeido;
        this.productoId = productoId;
        this.nombreProducto = nombreProducto;
    }

    public void marcarComoLeido() {
        this.esLeido = true;
    }

    public Long getId() { return id; }
    public Long getConversacionId() { return conversacionId; }
    public TipoEmisor getEmisor() { return emisor; }
    public String getContenido() { return contenido; }
    public Instant getFechaHora() { return fechaHora; }
    public boolean isEsLeido() { return esLeido; }
    public Long getProductoId() { return productoId; }
    public String getNombreProducto() { return nombreProducto; }
}
