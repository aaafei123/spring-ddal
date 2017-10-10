package io.isharing.springddal.route.exception;

public class ConfigurationException extends RuntimeException {
    private static final long serialVersionUID = -180146385688342818L;

    public ConfigurationException() {
        super();
    }

    public ConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConfigurationException(String message) {
        super(message);
    }

    public ConfigurationException(Throwable cause) {
        super(cause);
    }

}