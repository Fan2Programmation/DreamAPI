package com.dreamcatcher.intermediate.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "dream")
public class Dream {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 5000)
    private String content;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Lob
    @Column
    private byte[] imageData;

    public Long getId() { return id; }
    public void setId(Long i) { id = i; }
    public String getContent() { return content; }
    public void setContent(String c) { content = c; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime t) { createdAt = t; }
    public User getUser() { return user; }
    public void setUser(User u) { user = u; }
    public byte[] getImageData() { return imageData; }
    public void setImageData(byte[] d) { imageData = d; }
}
