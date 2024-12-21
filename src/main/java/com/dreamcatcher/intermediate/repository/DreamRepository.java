package com.dreamcatcher.intermediate.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dreamcatcher.intermediate.model.Dream;

@Repository
public interface DreamRepository extends JpaRepository<Dream, Long> {
    /**
     * Cette méthode est overridée de JpaRepository est la méthode de base
     * permet juste de faire la requête sql correspondante à la recherche
     * d'un motif (content) dans la base de données, pratique !!
     * @param content
     * @return
     */
    List<Dream> findByContentContainingIgnoreCase(String content);
}
