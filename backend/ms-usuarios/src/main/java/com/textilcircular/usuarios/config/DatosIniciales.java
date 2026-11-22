package com.textilcircular.usuarios.config;

import com.textilcircular.usuarios.model.*;
import com.textilcircular.usuarios.repository.EmprendedoraRepository;
import com.textilcircular.usuarios.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;

@Component
public class DatosIniciales implements CommandLineRunner {

    // Contraseñas demo definidas en un solo lugar y fáciles de encontrar
    public static final String PASSWORD_ADMIN = "admin123";
    public static final String PASSWORD_EMPRENDEDORA = "emprendedora123";
    public static final String PASSWORD_CLIENTE = "cliente123";

    private final UsuarioRepository usuarios;
    private final EmprendedoraRepository emprendedoras;
    private final PasswordEncoder passwordEncoder;
    private final boolean habilitada;

    public DatosIniciales(UsuarioRepository usuarios,
                          EmprendedoraRepository emprendedoras,
                          PasswordEncoder passwordEncoder,
                          @Value("${usuarios.semilla.habilitada:true}") boolean habilitada) {
        this.usuarios = usuarios;
        this.emprendedoras = emprendedoras;
        this.passwordEncoder = passwordEncoder;
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
        if (usuarios.count() > 0 || emprendedoras.count() > 0) {
            return;
        }

        String hashAdmin = passwordEncoder.encode(PASSWORD_ADMIN);
        String hashEmp = passwordEncoder.encode(PASSWORD_EMPRENDEDORA);
        String hashCli = passwordEncoder.encode(PASSWORD_CLIENTE);

        // =========================================================================
        // ORDEN FIJO DE CREACIÓN DE USUARIOS (los IDs deben alinearse con ms-mercado):
        // ID 1: Administradora
        // ID 2 a 9: Emprendedoras ACTIVAS (EmprendedorasActivasDemo de la app)
        // ID 10: Emprendedora OCULTA (EmprendedoraOcultaDemo de la app)
        // ID 11: Emprendedora DADA_DE_BAJA (EmprendedoraBajaDemo de la app)
        // ID 12: Emprendedora POR_REVISAR (nueva postulante pendiente de moderación)
        // ID 13: Emprendedora RECHAZADA (postulante rechazada con motivo)
        // ID 14: Cliente Francisca Tapia
        // ID 15: Cliente Rodrigo San Martín
        // =========================================================================

        // 1. Administradora
        usuarios.save(new Usuario("Administradora Fundación", "admin@textilcircular.cl",
                hashAdmin, Rol.ADMIN, true));

        // 2. Emprendedora Activa 1: Camila Morales (Valparaíso)
        crearEmprendedora(
                "Camila Andrea Morales Soto", "camila.morales@textilcircular.cl", hashEmp,
                "154203125", "Taller Hilos del Sur", "Valparaíso", "Valparaíso",
                List.of(Rubro.SUPRARRECICLAJE, Rubro.REPARACION), "tallerhilosdelsur",
                EstadoFichaEmprendimiento.ACTIVA, null, true, null);

        // 3. Emprendedora Activa 2: María Elena Sepúlveda (Providencia)
        crearEmprendedora(
                "María Elena Sepúlveda", "maria.renace@correo.cl", hashEmp,
                "178924412", "Renace Ropa Vintage", "Metropolitana de Santiago", "Providencia",
                List.of(Rubro.SEGUNDA_MANO), "renace_vintage",
                EstadoFichaEmprendimiento.ACTIVA, null, true, null);

        // 4. Emprendedora Activa 3: Daniela Paz Rojas (Arica)
        crearEmprendedora(
                "Daniela Paz Rojas Castillo", "daniela.sol@correo.cl", hashEmp,
                "164258813", "Sol Textil Arica", "Arica y Parinacota", "Arica",
                List.of(Rubro.SUPRARRECICLAJE), "soltextilarica",
                EstadoFichaEmprendimiento.ACTIVA, null, true, null);

        // 5. Emprendedora Activa 4: Claudia Jimena Silva (Antofagasta)
        crearEmprendedora(
                "Claudia Jimena Silva Ortiz", "claudia.recrea@correo.cl", hashEmp,
                "183021194", "Recrea Telas Antofagasta", "Antofagasta", "Antofagasta",
                List.of(Rubro.SUPRARRECICLAJE, Rubro.TALLERES), "recreatelas",
                EstadoFichaEmprendimiento.ACTIVA, null, true, null);

        // 6. Emprendedora Activa 5: Valentina Alejandra Herrera (La Serena)
        crearEmprendedora(
                "Valentina Alejandra Herrera Díaz", "valentina.coquimbo@correo.cl", hashEmp,
                "191234567", "Arriendo Consciente Coquimbo", "Coquimbo", "La Serena",
                List.of(Rubro.ARRIENDO, Rubro.SEGUNDA_MANO), "arriendoconsciente",
                EstadoFichaEmprendimiento.ACTIVA, null, true, null);

        // 7. Emprendedora Activa 6: Loreto Paz Carrasco (Talca)
        crearEmprendedora(
                "Loreto Paz Carrasco Morales", "loreto.maule@correo.cl", hashEmp,
                "145678901", "Puntada Limpia Maule", "Maule", "Talca",
                List.of(Rubro.REPARACION), "puntadalimpia",
                EstadoFichaEmprendimiento.ACTIVA, null, true, null);

        // 8. Emprendedora Activa 7: Carmen Gloria Benítez (Temuco)
        crearEmprendedora(
                "Carmen Gloria Benítez Soto", "carmen.araucania@correo.cl", hashEmp,
                "132345678", "Sur Circular Araucanía", "La Araucanía", "Temuco",
                List.of(Rubro.SUPRARRECICLAJE, Rubro.SEGUNDA_MANO), "surcircular",
                EstadoFichaEmprendimiento.ACTIVA, null, true, null);

        // 9. Emprendedora Activa 8: Lucía Ignacia Navarro (Punta Arenas)
        crearEmprendedora(
                "Lucía Ignacia Navarro Pérez", "lucia.patagonia@correo.cl", hashEmp,
                "123456789", "Lanas Magallánicas", "Magallanes y de la Antártica Chilena", "Punta Arenas",
                List.of(Rubro.TALLERES, Rubro.REPARACION), "lanasmagallanicas",
                EstadoFichaEmprendimiento.ACTIVA, null, true, null);

        // 10. Emprendedora OCULTA: Rosa Inés Gómez (Concepción)
        crearEmprendedora(
                "Rosa Inés Gómez", "rosa.gomez@correo.cl", hashEmp,
                "14331092K", "Costuras y Arreglos Rosa", "Biobío", "Concepción",
                List.of(Rubro.REPARACION, Rubro.TALLERES), null,
                EstadoFichaEmprendimiento.OCULTA, "Ficha ocultada manualmente por la administradora",
                true, null);

        // 11. Emprendedora DADA_DE_BAJA: Beatriz Fuentes (Castro)
        crearEmprendedora(
                "Beatriz Fuentes", "beatriz@chiloe.cl", hashEmp,
                "161108745", "EcoTejidos Chiloé", "Los Lagos", "Castro",
                List.of(Rubro.SUPRARRECICLAJE), "ecotejidos_chiloe",
                EstadoFichaEmprendimiento.DADA_DE_BAJA, "Baja voluntaria de la emprendedora",
                false, Instant.now());

        // 12. Emprendedora POR_REVISAR: Patricia Andrea Valenzuela (Santiago)
        crearEmprendedora(
                "Patricia Andrea Valenzuela Lagos", "patricia.valenzuela@correo.cl", hashEmp,
                "159876543", "Taller Costuras Urbanas", "Metropolitana de Santiago", "Santiago",
                List.of(Rubro.REPARACION, Rubro.SUPRARRECICLAJE), "costurasurbanas",
                EstadoFichaEmprendimiento.POR_REVISAR, null,
                true, null);

        // 13. Emprendedora RECHAZADA: Mónica Beatriz Cáceres (Quilpué)
        crearEmprendedora(
                "Mónica Beatriz Cáceres Morales", "monica.caceres@correo.cl", hashEmp,
                "167894561", "Confecciones Ropa Express", "Valparaíso", "Quilpué",
                List.of(Rubro.SEGUNDA_MANO), "ropaexpress",
                EstadoFichaEmprendimiento.RECHAZADA, "No cumple con criterios de reciclaje o reutilización textil circular",
                true, null);

        // 14. Cliente 1: Francisca Tapia
        usuarios.save(new Usuario("Francisca Tapia", "francisca.tapia@correo.cl",
                hashCli, Rol.CLIENTE, true));

        // 15. Cliente 2: Rodrigo San Martín
        usuarios.save(new Usuario("Rodrigo San Martín", "rodrigo.sanmartin@correo.cl",
                hashCli, Rol.CLIENTE, true));
    }

    private void crearEmprendedora(String nombre, String email, String passwordHash,
                                   String rut, String nombreComercial, String region, String comuna,
                                   List<Rubro> rubros, String instagram,
                                   EstadoFichaEmprendimiento estado, String motivo,
                                   boolean activa, Instant fechaBaja) {
        Usuario usuario = new Usuario(nombre, email, passwordHash, Rol.EMPRENDEDORA, true);
        if (fechaBaja != null) {
            usuario.darDeBaja(fechaBaja);
        }
        usuario = usuarios.save(usuario);

        Emprendedora ficha = new Emprendedora(usuario, rut, nombreComercial, region, comuna, rubros, instagram);
        ficha.revisar(estado, motivo, Instant.now());
        if (!activa) {
            ficha.darDeBaja(fechaBaja != null ? fechaBaja : Instant.now());
        }
        emprendedoras.save(ficha);
    }
}
