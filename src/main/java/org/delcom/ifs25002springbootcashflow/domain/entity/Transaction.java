package org.delcom.ifs25002springbootcashflow.domain.entity;

import java.time.LocalDateTime;

public class Transaction {

    private Long id;
    private Long userId;
    private String title;
    private String description;
    private TransactionType type;
    private Double amount;
    private String attachmentPath;
    private LocalDateTime createdAt;

    public Transaction(Long id, Long userId, String title, String description,
                       TransactionType type, Double amount, String attachmentPath, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.type = type;
        this.amount = amount;
        this.attachmentPath = attachmentPath;
        this.createdAt = createdAt;
    }

    public Transaction(Long userId, String title, String description,
                       TransactionType type, Double amount, String attachmentPath) {
        this(null, userId, title, description, type, amount, attachmentPath, LocalDateTime.now());
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return this.userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TransactionType getType() {
        return this.type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public Double getAmount() {
        return this.amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getAttachmentPath() {
        return this.attachmentPath;
    }

    public void setAttachmentPath(String attachmentPath) {
        this.attachmentPath = attachmentPath;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}