package com.dreamcatcher.intermediate.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dreamcatcher.intermediate.model.Dream;

public interface DreamRepository extends JpaRepository<Dream, Long> {
}
