package com.example.todo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Service;

/**
 * Business logic for to-do items. Data is kept in memory (thread-safe map);
 * this keeps the demo self-contained - no database is needed.
 */
@Service
public class TodoService {

    private final Map<Long, Todo> todos = new ConcurrentHashMap<>();
    private final AtomicLong nextId = new AtomicLong(1);

    public List<Todo> findAll() {
        List<Todo> list = new ArrayList<>(todos.values());
        list.sort((a, b) -> Long.compare(a.getId(), b.getId()));
        return list;
    }

    public Optional<Todo> findById(long id) {
        return Optional.ofNullable(todos.get(id));
    }

    public Todo create(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title must not be empty");
        }
        long id = nextId.getAndIncrement();
        Todo todo = new Todo(id, title.trim(), false);
        todos.put(id, todo);
        return todo;
    }

    /** Flips the done flag. Returns empty if the item does not exist. */
    public Optional<Todo> toggle(long id) {
        Todo todo = todos.get(id);
        if (todo == null) {
            return Optional.empty();
        }
        todo.setDone(!todo.isDone());
        return Optional.of(todo);
    }

    public boolean delete(long id) {
        return todos.remove(id) != null;
    }

    public long countPending() {
        return todos.values().stream().filter(t -> !t.isDone()).count();
    }
}
