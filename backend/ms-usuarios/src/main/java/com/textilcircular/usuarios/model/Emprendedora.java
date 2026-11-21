package com.textilcircular.usuarios.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "emprendedoras")
public class Emprendedora {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    @Column(nullable = false, unique = true)
    private String rut;

    @Column(name = "nombre_comercial", nullable = false)
    private String nombreComercial;

    @Column(nullable = false)
    private String region;

    @Column(nullable = false)
    private String comuna;

    @ElementCollection
    @CollectionTable(name = "emprendedora_rubros", joinColumns = @JoinColumn(name = "emprendedora_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "rubro", nullable = false)
    private List<Rubro> rubros = new ArrayList<>();

    private String instagram;

    @Column(nullable = false)
    private boolean activa = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_ficha", nullable = false)
    private EstadoFichaEmprendimiento estadoFicha = EstadoFichaEmprendimiento.POR_REVISAR;

    @Column(name = "motivo_revision", length = 2000)
    private String motivoRevision;

    @Column(name = "fecha_revision")
    private Instant fechaRevision;

    @ElementCollection
    @CollectionTable(name = "emprendedora_problemas", joinColumns = @JoinColumn(name = "emprendedora_id"))
    @Column(name = "problema")
    private List<String> problemasDetectados = new ArrayList<>();

    protected Emprendedora() {}

    public Emprendedora(Usuario usuario, String rut, String nombreComercial, String region,
                        String comuna, List<Rubro> rubros, String instagram) {
        this.usuario = usuario;
        this.rut = rut;
        this.nombreComercial = nombreComercial;
        this.region = region;
        this.comuna = comuna;
        this.rubros = new ArrayList<>(rubros);
        this.instagram = instagram;
    }

    public void revisar(EstadoFichaEmprendimiento estado, String motivo, Instant fecha) {
        this.estadoFicha = estado;
        this.motivoRevision = motivo;
        this.fechaRevision = fecha;
    }

    public void darDeBaja(Instant fecha) {
        activa = false;
        estadoFicha = EstadoFichaEmprendimiento.DADA_DE_BAJA;
        usuario.darDeBaja(fecha);
    }

    public void eliminar(Instant fecha) {
        darDeBaja(fecha);
        usuario.eliminar(fecha);
    }

    public Long getId() { return id; }
    public Usuario getUsuario() { return usuario; }
    public String getRut() { return rut; }
    public String getNombreComercial() { return nombreComercial; }
    public String getRegion() { return region; }
    public String getComuna() { return comuna; }
    public List<Rubro> getRubros() { return List.copyOf(rubros); }
    public String getInstagram() { return instagram; }
    public boolean isActiva() { return activa; }
    public EstadoFichaEmprendimiento getEstadoFicha() { return estadoFicha; }
    public String getMotivoRevision() { return motivoRevision; }
    public Instant getFechaRevision() { return fechaRevision; }
    public List<String> getProblemasDetectados() { return List.copyOf(problemasDetectados); }
}
