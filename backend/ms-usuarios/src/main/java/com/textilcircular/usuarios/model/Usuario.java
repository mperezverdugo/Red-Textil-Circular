package com.textilcircular.usuarios.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "usuarios")
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Rol rol;

    @Column(name = "acepta_ley_datos", nullable = false)
    private boolean aceptaLeyDatos;

    @Column(name = "fecha_baja")
    private Instant fechaBaja;

    @Column(name = "fecha_eliminacion")
    private Instant fechaEliminacion;

    protected Usuario() {}

    public Usuario(String nombre, String email, String passwordHash, Rol rol,
                   boolean aceptaLeyDatos) {
        this.nombre = nombre;
        this.email = email;
        this.passwordHash = passwordHash;
        this.rol = rol;
        this.aceptaLeyDatos = aceptaLeyDatos;
    }

    public void darDeBaja(Instant fecha) { this.fechaBaja = fecha; }
    public void eliminar(Instant fecha) { this.fechaEliminacion = fecha; }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public Rol getRol() { return rol; }
    public boolean isAceptaLeyDatos() { return aceptaLeyDatos; }
    public Instant getFechaBaja() { return fechaBaja; }
    public Instant getFechaEliminacion() { return fechaEliminacion; }
}
