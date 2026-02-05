package org.example.testgraalvm.entity;

import jakarta.persistence.*;

/**
 * Demo entity for SQLite database demonstration.
 * This entity is compatible with GraalVM Native Image.
 */
@Entity
@Table(name = "demo")
public class Demo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    // Default constructor required by JPA
    public Demo() {
    }

    public Demo(String name) {
        this.name = name;
    }

    public Demo(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Demo{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
