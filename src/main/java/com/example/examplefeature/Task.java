package com.example.examplefeature;

import jakarta.persistence.*;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "task")
public class Task {

    public static final int DESCRIPTION_MAX_LENGTH = 300;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "task_id")
    private Long id;

    @Column(name = "description", nullable = false, length = DESCRIPTION_MAX_LENGTH)
    private String description = "";

    @Column(name = "creation_date", nullable = false)
    private Instant creationDate;

    @Column(name = "due_date")
    @Nullable
    private LocalDate dueDate;

    @Column(name = "color")
    private String color;

    protected Task() { // To keep Hibernate happy
    }

    public Task(String description, Instant creationDate) {
        setDescription(description);
        this.creationDate = creationDate;
        this.color = com.example.ColorService.generateRandomColor();
    }

    public @Nullable Long getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        if (description.length() > DESCRIPTION_MAX_LENGTH) {
            throw new IllegalArgumentException("Description length exceeds " + DESCRIPTION_MAX_LENGTH);
        }
        this.description = description;
    }

    public Instant getCreationDate() {
        return creationDate;
    }

    public @Nullable LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(@Nullable LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public enum Priority {
        HIGH("HIGH", "danger"),
        MEDIUM("MEDIUM", "warning"),
        LOW("LOW", "success");

        private final String displayName;
        private final String themeVariant;

        Priority(String displayName, String themeVariant) {
            this.displayName = displayName;
            this.themeVariant = themeVariant;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getThemeVariant() {
            return themeVariant;
        }
    }

    public Priority getPriority() {
        if (dueDate == null) {
            return Priority.LOW; // Sem data de conclusão = baixa prioridade
        }

        long daysUntilDue = ChronoUnit.DAYS.between(LocalDate.now(), dueDate);

        if (daysUntilDue <= 2) {
            return Priority.HIGH;
        } else if (daysUntilDue <= 5) {
            return Priority.MEDIUM;
        } else {
            return Priority.LOW;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !getClass().isAssignableFrom(obj.getClass())) {
            return false;
        }
        if (obj == this) {
            return true;
        }

        Task other = (Task) obj;
        return getId() != null && getId().equals(other.getId());
    }

    @Override
    public int hashCode() {
        // Hashcode should never change during the lifetime of an object. Because of
        // this we can't use getId() to calculate the hashcode. Unless you have sets
        // with lots of entities in them, returning the same hashcode should not be a
        // problem.
        return getClass().hashCode();
    }
}
