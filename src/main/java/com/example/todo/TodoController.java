package com.example.todo;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** REST API used by the web page (static/index.html). */
@RestController
@RequestMapping("/api")
public class TodoController {

    private final TodoService service;
    private final String appVersion;

    public TodoController(TodoService service, @Value("${app.version}") String appVersion) {
        this.service = service;
        this.appVersion = appVersion;
    }

    /** Shows which build is running - used to prove a redeployment reached the cluster. */
    @GetMapping("/info")
    public Map<String, String> info() {
        return Map.of(
                "app", "To-Do List",
                "version", appVersion,
                "hostname", System.getenv().getOrDefault("HOSTNAME", "local"));
    }

    @GetMapping("/todos")
    public List<Todo> list() {
        return service.findAll();
    }

    @PostMapping("/todos")
    public ResponseEntity<?> create(@RequestBody Map<String, String> body) {
        try {
            Todo todo = service.create(body.get("title"));
            return ResponseEntity.status(HttpStatus.CREATED).body(todo);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/todos/{id}/toggle")
    public ResponseEntity<Todo> toggle(@PathVariable long id) {
        return service.toggle(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/todos/{id}")
    public ResponseEntity<Void> delete(@PathVariable long id) {
        return service.delete(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}
