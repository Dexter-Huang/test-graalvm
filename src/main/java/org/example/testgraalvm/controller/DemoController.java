package org.example.testgraalvm.controller;

import org.example.testgraalvm.entity.Demo;
import org.example.testgraalvm.repository.DemoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * REST Controller for Demo entity CRUD operations.
 */
@RestController
@RequestMapping("/api/demo")
public class DemoController {

    private final DemoRepository demoRepository;

    public DemoController(DemoRepository demoRepository) {
        this.demoRepository = demoRepository;
    }

    /**
     * Create a new demo record.
     * POST /api/demo
     */
    @PostMapping
    public ResponseEntity<Demo> create(@RequestBody Demo demo) {
        Demo saved = demoRepository.save(demo);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    /**
     * Get all demo records.
     * GET /api/demo
     */
    @GetMapping
    public ResponseEntity<List<Demo>> findAll() {
        List<Demo> demos = demoRepository.findAll();
        return ResponseEntity.ok(demos);
    }

    /**
     * Get a demo record by ID.
     * GET /api/demo/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Demo> findById(@PathVariable Long id) {
        Optional<Demo> demo = demoRepository.findById(id);
        return demo.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Search demos by name.
     * GET /api/demo/search?name=xxx
     */
    @GetMapping("/search")
    public ResponseEntity<List<Demo>> searchByName(@RequestParam String name) {
        List<Demo> demos = demoRepository.findByNameContainingIgnoreCase(name);
        return ResponseEntity.ok(demos);
    }

    /**
     * Update a demo record.
     * PUT /api/demo/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<Demo> update(@PathVariable Long id, @RequestBody Demo demo) {
        if (!demoRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        demo.setId(id);
        Demo updated = demoRepository.save(demo);
        return ResponseEntity.ok(updated);
    }

    /**
     * Delete a demo record by ID.
     * DELETE /api/demo/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!demoRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        demoRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Delete all demo records.
     * DELETE /api/demo
     */
    @DeleteMapping
    public ResponseEntity<Void> deleteAll() {
        demoRepository.deleteAll();
        return ResponseEntity.noContent().build();
    }
}
