package com.alura.name.Alura.repository;

import com.alura.name.Alura.model.Libro;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface LibroRepository extends JpaRepository<Libro, Long> {
    Optional<Libro> findByTituloIgnoreCase(String titulo);

    
    List<Libro> findByIdioma(String idioma);
}