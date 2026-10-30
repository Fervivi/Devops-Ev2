package cl.duoc.devops.minibiblioteca.model;

public record Libro(
        Long id,
        String titulo,
        String autor,
        boolean disponible
) {
}
