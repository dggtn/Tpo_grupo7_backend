package com.example.g7_back_mobile.controllers.dtos;

public class NotificationEvent {
    private String type; // REMINDER, RESCHEDULE, CANCEL
    private String message;
    private String classId;
    private long scheduledAt; // epoch millis de la clase (si aplica)
    private long createdAt = System.currentTimeMillis();

    public NotificationEvent() {}

    public NotificationEvent(String type, String message, String classId, long scheduledAt) {
        this.type = type;
        this.message = message;
        this.classId = classId;
        this.scheduledAt = scheduledAt;
        this.createdAt = System.currentTimeMillis();
    }

    // getters y setters
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getClassId() { return classId; }
    public void setClassId(String classId) { this.classId = classId; }
    public long getScheduledAt() { return scheduledAt; }
    public void setScheduledAt(long scheduledAt) { this.scheduledAt = scheduledAt; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
