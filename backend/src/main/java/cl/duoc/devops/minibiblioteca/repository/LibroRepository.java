package cl.duoc.devops.minibiblioteca.repository;

import cl.duoc.devops.minibiblioteca.model.Libro;

import java.util.List;
import java.util.Optional;

public interface LibroRepository {

    List<Libro> findAll();

    Optional<Libro> findById(Long id);
}
