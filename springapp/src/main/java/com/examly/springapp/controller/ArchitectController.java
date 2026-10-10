package com.examly.springapp.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.examly.springapp.model.Architect;
import com.examly.springapp.service.ArchitectService;

@RestController
@RequestMapping("/api/architects")
public class ArchitectController {
    @Autowired
    private ArchitectService architectService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Architect addArchitect(@RequestBody Architect architect) {
        return architectService.addArchitect(architect);
    }

    @GetMapping("/top-rated")
    public List<Architect> getTopRated() {
        return architectService.getTopRated();
    }

    @GetMapping("/locations")
    public List<String> getAllLocations() {
        return architectService.getAllLocations();
    }

    @GetMapping("/{architectId}")
    public Architect getArchitectById(@PathVariable Long architectId) {
        return architectService.getArchitectById(architectId);
    }

    @GetMapping
    public List<Architect> getAllArchitects() {
        return architectService.getAllArchitects();
    }

    @PutMapping("/{architectId}")
    public Architect updateArchitect(@PathVariable Long architectId,
                                     @RequestBody Architect architect) {
        return architectService.updateArchitect(architectId, architect);
    }

    @DeleteMapping("/{architectId}")
    public void deleteArchitect(@PathVariable Long architectId) {
        architectService.deleteArchitect(architectId);
    }
}
