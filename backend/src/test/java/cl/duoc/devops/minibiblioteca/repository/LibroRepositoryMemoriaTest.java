package cl.duoc.devops.minibiblioteca.repository;

import cl.duoc.devops.minibiblioteca.model.Libro;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LibroRepositoryMemoriaTest {

    private final LibroRepositoryMemoria repository = new LibroRepositoryMemoria();

    @Test
    void findAll_deberiaRetornarCatalogoInicial() {
        List<Libro> libros = repository.findAll();

        assertEquals(4, libros.size());
        assertTrue(libros.stream().anyMatch(libro -> !libro.disponible()));
    }

    @Test
    void findById_deberiaEncontrarLibroExistente() {
        Optional<Libro> libro = repository.findById(1L);

        assertTrue(libro.isPresent());
        assertEquals("Clean Code", libro.get().titulo());
    }

    @Test
    void findById_deberiaRetornarVacioCuandoNoExiste() {
        assertFalse(repository.findById(99L).isPresent());
    }
}
