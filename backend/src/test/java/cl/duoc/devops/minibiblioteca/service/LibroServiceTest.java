package cl.duoc.devops.minibiblioteca.service;

import cl.duoc.devops.minibiblioteca.exception.LibroNoEncontradoException;
import cl.duoc.devops.minibiblioteca.model.Libro;
import cl.duoc.devops.minibiblioteca.repository.LibroRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LibroServiceTest {

    @Mock
    private LibroRepository libroRepository;

    @InjectMocks
    private LibroService libroService;

    @Test
    void listar_deberiaRetornarLibrosDelRepositorio() {
        // ARRANGE: definimos lo que responderá el mock.
        List<Libro> esperados = List.of(
                new Libro(1L, "Clean Code", "Robert C. Martin", true),
                new Libro(2L, "Docker Deep Dive", "Nigel Poulton", false)
        );
        when(libroRepository.findAll()).thenReturn(esperados);

        // ACT: ejecutamos el método real del servicio.
        List<Libro> resultado = libroService.listar();

        // ASSERT: validamos el resultado y la interacción.
        assertEquals(2, resultado.size());
        assertEquals("Clean Code", resultado.getFirst().titulo());
        verify(libroRepository).findAll();
    }

    @Test
    void buscarPorId_deberiaRetornarLibroCuandoExiste() {
        Libro libro = new Libro(1L, "Clean Code", "Robert C. Martin", true);
        when(libroRepository.findById(1L)).thenReturn(Optional.of(libro));

        Libro resultado = libroService.buscarPorId(1L);

        assertEquals(1L, resultado.id());
        assertEquals("Clean Code", resultado.titulo());
        verify(libroRepository).findById(1L);
    }

    @Test
    void buscarPorId_deberiaLanzarExcepcionCuandoNoExiste() {
        when(libroRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(
                LibroNoEncontradoException.class,
                () -> libroService.buscarPorId(99L)
        );
    }

    @Test
    void contarDisponibles_deberiaContarSoloDisponibles() {
        when(libroRepository.findAll()).thenReturn(List.of(
                new Libro(1L, "A", "Autor A", true),
                new Libro(2L, "B", "Autor B", false),
                new Libro(3L, "C", "Autor C", true)
        ));

        long disponibles = libroService.contarDisponibles();

        assertEquals(2L, disponibles);
        assertTrue(disponibles > 0);
    }
}
