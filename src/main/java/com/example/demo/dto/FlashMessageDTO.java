package com.example.demo.dto;

public class FlashMessageDTO {
    private String message;
    private FlashMessageType type;
    
    public enum FlashMessageType {
        SUCCESS("success"),
        ERROR("error"),
        WARNING("warning"),
        INFO("info");
        
        private final String cssClass;
        
        FlashMessageType(String cssClass) {
            this.cssClass = cssClass;
        }
        
        public String getCssClass() {
            return cssClass;
        }
    }

    // Constructors
    public FlashMessageDTO() {}
    
    public FlashMessageDTO(String message, FlashMessageType type) {
        this.message = message;
        this.type = type;
    }
    
    // Getters and Setters
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public FlashMessageType getType() {
        return type;
    }
    
    public void setType(FlashMessageType type) {
        this.type = type;
    }
    
    // Helper methods
    public String getCssClass() {
        return type.getCssClass();
    }
    
    public static FlashMessageDTO success(String message) {
        return new FlashMessageDTO(message, FlashMessageType.SUCCESS);
    }
    
    public static FlashMessageDTO error(String message) {
        return new FlashMessageDTO(message, FlashMessageType.ERROR);
    }
    
    public static FlashMessageDTO warning(String message) {
        return new FlashMessageDTO(message, FlashMessageType.WARNING);
    }
    
    public static FlashMessageDTO info(String message) {
        return new FlashMessageDTO(message, FlashMessageType.INFO);
    }
}