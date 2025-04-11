package com.example.backend.entity;
import jakarta.persistence.*;
@Entity
@Table(name = "sizes")
public class Size {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String value;

    // Getters and Setters

    public Size(Long id, String value) {
        this.id = id;
        this.value = value;
    }

    public Size() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}