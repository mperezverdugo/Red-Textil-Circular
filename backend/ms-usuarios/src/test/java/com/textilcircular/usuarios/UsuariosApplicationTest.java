package com.textilcircular.usuarios;

import java.sql.Connection;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class UsuariosApplicationTest {
    @Autowired
    private DataSource dataSource;

    @Test
    void conectaConLaBaseDePruebas() throws Exception {
        try (Connection conexion = dataSource.getConnection()) {
            assertTrue(conexion.isValid(2));
            assertTrue(conexion.getMetaData().getURL().startsWith("jdbc:h2:mem:"));
        }
    }
}
