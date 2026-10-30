package cl.duoc.devops.minibiblioteca;

import cl.duoc.devops.minibiblioteca.controller.LibroController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

// Prueba de humo: el contexto de Spring arranca y conecta Controller -> Service -> Repository.
@SpringBootTest
class MiniBibliotecaApplicationTest {

    @Autowired
    private LibroController libroController;

    @Test
    void contextoDeberiaCargar() {
        assertNotNull(libroController);
        assertNotNull(libroController.health());
    }
}
