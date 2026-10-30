package cl.duoc.devops.minibiblioteca.repository;

import cl.duoc.devops.minibiblioteca.model.Libro;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class LibroRepositoryMemoria implements LibroRepository {

    private final List<Libro> libros = List.of(
            new Libro(1L, "Clean Code", "Robert C. Martin", true),
            new Libro(2L, "The DevOps Handbook", "Gene Kim", true),
            new Libro(3L, "Docker Deep Dive", "Nigel Poulton", false),
            new Libro(4L, "Continuous Delivery", "Jez Humble", true)
    );

    @Override
    public List<Libro> findAll() {
        return libros;
    }

    @Override
    public Optional<Libro> findById(Long id) {
        return libros.stream()
                .filter(libro -> libro.id().equals(id))
                .findFirst();
    }
}
