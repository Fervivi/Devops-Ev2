package cl.duoc.devops.minibiblioteca.controller;

import cl.duoc.devops.minibiblioteca.exception.LibroNoEncontradoException;
import cl.duoc.devops.minibiblioteca.model.Libro;
import cl.duoc.devops.minibiblioteca.service.LibroService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class LibroControllerTest {

    // El servicio se simula: probamos solo la capa HTTP (rutas, códigos y JSON).
    @Mock
    private LibroService libroService;

    @InjectMocks
    private LibroController libroController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        // MockMvc standalone: no levanta Spring completo, solo el controller y el manejador de errores.
        mockMvc = MockMvcBuilders.standaloneSetup(libroController)
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
    }

    @Test
    void listar_deberiaResponder200ConLibros() throws Exception {
        when(libroService.listar()).thenReturn(List.of(
                new Libro(1L, "Clean Code", "Robert C. Martin", true)
        ));

        mockMvc.perform(get("/api/libros"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].titulo").value("Clean Code"))
                .andExpect(jsonPath("$[0].disponible").value(true));
    }

    @Test
    void buscar_deberiaResponder200CuandoExiste() throws Exception {
        when(libroService.buscarPorId(2L))
                .thenReturn(new Libro(2L, "Docker Deep Dive", "Nigel Poulton", false));

        mockMvc.perform(get("/api/libros/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.autor").value("Nigel Poulton"));

        verify(libroService).buscarPorId(2L);
    }

    @Test
    void buscar_deberiaResponder404CuandoNoExiste() throws Exception {
        when(libroService.buscarPorId(99L)).thenThrow(new LibroNoEncontradoException(99L));

        mockMvc.perform(get("/api/libros/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("No existe un libro con id 99"));
    }

    @Test
    void resumen_deberiaRetornarTotalYDisponibles() throws Exception {
        when(libroService.listar()).thenReturn(List.of(
                new Libro(1L, "A", "Autor A", true),
                new Libro(2L, "B", "Autor B", false)
        ));
        when(libroService.contarDisponibles()).thenReturn(1L);

        mockMvc.perform(get("/api/resumen"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(2))
                .andExpect(jsonPath("$.disponibles").value(1));
    }

    @Test
    void health_deberiaResponderUp() throws Exception {
        // Este endpoint lo usan el healthcheck de Compose, las probes de Kubernetes y el deploy.
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }
}
