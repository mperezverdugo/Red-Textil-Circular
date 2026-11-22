package com.textilcircular.mercado.model;

import jakarta.persistence.*;

@Entity
@Table(name = "productos")
public class Producto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "emprendedora_id", nullable = false)
    private Long emprendedoraId;

    @Column(nullable = false, length = 80)
    private String nombre;

    @Column(name = "descripcion_corta", nullable = false, length = 250)
    private String descripcionCorta;

    @Column(name = "explicacion_proceso", length = 500)
    private String explicacionProceso;

    protected Producto() {}

    public Producto(Long emprendedoraId, String nombre, String descripcionCorta, String explicacionProceso) {
        this.emprendedoraId = emprendedoraId;
        this.nombre = nombre;
        this.descripcionCorta = descripcionCorta;
        this.explicacionProceso = explicacionProceso;
    }

    public void actualizar(String nombre, String descripcionCorta, String explicacionProceso) {
        this.nombre = nombre;
        this.descripcionCorta = descripcionCorta;
        this.explicacionProceso = explicacionProceso;
    }

    public Long getId() { return id; }
    public Long getEmprendedoraId() { return emprendedoraId; }
    public String getNombre() { return nombre; }
    public String getDescripcionCorta() { return descripcionCorta; }
    public String getExplicacionProceso() { return explicacionProceso; }
}
