package com.examly.springapp.service;

import java.util.List;

import com.examly.springapp.model.Architect;

public interface ArchitectService {
    Architect addArchitect(Architect architect);

    Architect updateArchitect(Long architectId, Architect updatedArchitect);

    Architect getArchitectById(Long architectId);

    List<Architect> getAllArchitects();

    void deleteArchitect(Long architectId);

    List<Architect> getTopRated();

    List<String> getAllLocations();
}
