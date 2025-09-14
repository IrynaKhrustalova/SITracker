package org.example.sitracker.domain;

import java.time.LocalDateTime;
import java.util.Objects;

public class Issue {
    private String id;
    private String description;
    private String parentId;
    private Status status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Issue() {
    }

    public Issue (String id, String description, String parentId, Status status, LocalDateTime createdAt) {
        this.id = id;
        this.description  = description;
        this.parentId = parentId;
        this.status = status;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public String getParentId() {
        return parentId;
    }

    public Status getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Issue issue)) return false;
        return Objects.equals(id, issue.id) && Objects.equals(description, issue.description) && Objects.equals(parentId, issue.parentId) && status == issue.status && Objects.equals(createdAt, issue.createdAt) && Objects.equals(updatedAt, issue.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, description, parentId, status, createdAt, updatedAt);
    }

    @Override
    public String toString() {
        return "Issue{" +
                "id='" + id + '\'' +
                ", description='" + description + '\'' +
                ", parentId='" + parentId + '\'' +
                ", status=" + status +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
