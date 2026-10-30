package cl.duoc.devops.minibiblioteca.service;

import cl.duoc.devops.minibiblioteca.exception.LibroNoEncontradoException;
import cl.duoc.devops.minibiblioteca.model.Libro;
import cl.duoc.devops.minibiblioteca.repository.LibroRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LibroService {

    private final LibroRepository libroRepository;

    public LibroService(LibroRepository libroRepository) {
        this.libroRepository = libroRepository;
    }

    public List<Libro> listar() {
        return libroRepository.findAll();
    }

    public Libro buscarPorId(Long id) {
        return libroRepository.findById(id)
                .orElseThrow(() -> new LibroNoEncontradoException(id));
    }

    public long contarDisponibles() {
        return libroRepository.findAll().stream()
                .filter(Libro::disponible)
                .count();
    }
}
