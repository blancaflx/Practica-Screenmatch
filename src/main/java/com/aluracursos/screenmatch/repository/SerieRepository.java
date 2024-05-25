package com.aluracursos.screenmatch.repository;

import com.aluracursos.screenmatch.model.Categoria;
import com.aluracursos.screenmatch.model.Serie;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SerieRepository extends JpaRepository<Serie, Long> {
    Optional findByTituloContainsIgnoreCase(String nombreSerie); //optional utilizado porque puede ser que retorno o no algo

    List<Serie> findTop5ByOrderByEvaluacionDesc();
    List<Serie> findByGenero(Categoria categoria);

}
