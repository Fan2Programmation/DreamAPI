package com.dreamcatcher.intermediate.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.dreamcatcher.intermediate.model.Dream;
import com.dreamcatcher.intermediate.model.DreamWithoutImage;

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

    @Query("SELECT d.id as id, d.content as content, d.createdAt as createdAt, d.user as user " +
           "FROM Dream d " +
           "WHERE UPPER(d.content) LIKE UPPER(CONCAT('%', :content, '%'))")
    List<DreamWithoutImage> findByPartialContent(@Param("content") String content);
}
