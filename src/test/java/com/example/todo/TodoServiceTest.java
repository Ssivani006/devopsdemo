package com.example.todo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Plain unit tests for the business logic (no Spring context). */
class TodoServiceTest {

    private TodoService service;

    @BeforeEach
    void setUp() {
        service = new TodoService();
    }

    @Test
    void createAddsTodoWithIncrementingId() {
        Todo first = service.create("Buy milk");
        Todo second = service.create("Write report");

        assertThat(first.getId()).isEqualTo(1);
        assertThat(second.getId()).isEqualTo(2);
        assertThat(service.findAll()).hasSize(2);
    }

    @Test
    void createTrimsTitleAndStartsNotDone() {
        Todo todo = service.create("  Study DevOps  ");

        assertThat(todo.getTitle()).isEqualTo("Study DevOps");
        assertThat(todo.isDone()).isFalse();
    }

    @Test
    void createRejectsBlankTitle() {
        assertThatThrownBy(() -> service.create("   "))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.create(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void toggleFlipsDoneFlag() {
        Todo todo = service.create("Push code");

        service.toggle(todo.getId());
        assertThat(service.findById(todo.getId()).get().isDone()).isTrue();

        service.toggle(todo.getId());
        assertThat(service.findById(todo.getId()).get().isDone()).isFalse();
    }

    @Test
    void toggleUnknownIdReturnsEmpty() {
        assertThat(service.toggle(99)).isEmpty();
    }

    @Test
    void deleteRemovesTodo() {
        Todo todo = service.create("Temp");

        assertThat(service.delete(todo.getId())).isTrue();
        assertThat(service.findAll()).isEmpty();
        assertThat(service.delete(todo.getId())).isFalse();
    }

    @Test
    void countPendingIgnoresDoneItems() {
        Todo a = service.create("A");
        service.create("B");
        service.toggle(a.getId());

        assertThat(service.countPending()).isEqualTo(1);
    }
}
