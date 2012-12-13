package net.sourceforge.marathon.api;

import java.io.Serializable;
import java.util.Date;

public class LogRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String message ;
    private final String description;
    private final String module ;
    private final Date date ;
    private final int type;
    
    public LogRecord(int type, String message, String description, String module, Date date) {
        this.type = type;
        this.message = message;
        this.description = description;
        this.module = module;
        this.date = date;
    }

    public String getMessage() {
        return message;
    }

    public String getModule() {
        return module;
    }

    public Date getDate() {
        return date;
    }

    public String getDescription() {
        return description;
    }

    public int getType() {
        return type;
    }
}