package com.textilcircular.mercado.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "conversaciones", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"cliente_id", "emprendedora_id"})
})
public class Conversacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cliente_id", nullable = false)
    private Long clienteId;

    @Column(name = "emprendedora_id", nullable = false)
    private Long emprendedoraId;

    @Column(name = "producto_id")
    private Long productoId;

    @Column(name = "nombre_cliente", nullable = false)
    private String nombreCliente;

    @Column(name = "nombre_emprendimiento", nullable = false)
    private String nombreEmprendimiento;

    @Column(name = "ultimo_mensaje", length = 1000)
    private String ultimoMensaje;

    @Column(name = "fecha_hora", nullable = false)
    private Instant fechaHora;

    @Column(name = "no_leidos_cliente", nullable = false)
    private int noLeidosCliente = 0;

    @Column(name = "no_leidos_emprendedora", nullable = false)
    private int noLeidosEmprendedora = 0;

    protected Conversacion() {}

    public Conversacion(Long clienteId, Long emprendedoraId, String nombreCliente,
                        String nombreEmprendimiento, Long productoId, Instant fechaHora) {
        this.clienteId = clienteId;
        this.emprendedoraId = emprendedoraId;
        this.nombreCliente = nombreCliente;
        this.nombreEmprendimiento = nombreEmprendimiento;
        this.productoId = productoId;
        this.fechaHora = fechaHora;
        this.ultimoMensaje = "";
    }

    public void registrarMensaje(String mensaje, Instant fecha, boolean enviadoPorCliente) {
        this.ultimoMensaje = mensaje;
        this.fechaHora = fecha;
        if (enviadoPorCliente) {
            this.noLeidosEmprendedora++;
        } else {
            this.noLeidosCliente++;
        }
    }

    public void marcarLeidaPorCliente() {
        this.noLeidosCliente = 0;
    }

    public void marcarLeidaPorEmprendedora() {
        this.noLeidosEmprendedora = 0;
    }

    public Long getId() { return id; }
    public Long getClienteId() { return clienteId; }
    public Long getEmprendedoraId() { return emprendedoraId; }
    public Long getProductoId() { return productoId; }
    public String getNombreCliente() { return nombreCliente; }
    public String getNombreEmprendimiento() { return nombreEmprendimiento; }
    public String getUltimoMensaje() { return ultimoMensaje; }
    public Instant getFechaHora() { return fechaHora; }
    public int getNoLeidosCliente() { return noLeidosCliente; }
    public int getNoLeidosEmprendedora() { return noLeidosEmprendedora; }
}
