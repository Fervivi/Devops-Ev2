package cl.duoc.devops.minibiblioteca.controller;

import cl.duoc.devops.minibiblioteca.model.Libro;
import cl.duoc.devops.minibiblioteca.service.LibroService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class LibroController {

    private final LibroService libroService;

    public LibroController(LibroService libroService) {
        this.libroService = libroService;
    }

    @GetMapping("/libros")
    public List<Libro> listar() {
        return libroService.listar();
    }

    @GetMapping("/libros/{id}")
    public Libro buscar(@PathVariable Long id) {
        return libroService.buscarPorId(id);
    }

    @GetMapping("/resumen")
    public Map<String, Long> resumen() {
        return Map.of(
                "total", (long) libroService.listar().size(),
                "disponibles", libroService.contarDisponibles()
        );
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP");
    }
}
