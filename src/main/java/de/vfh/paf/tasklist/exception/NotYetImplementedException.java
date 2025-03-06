package de.vfh.paf.tasklist.exception;

/**
 * Exception for methods that are not yet implemented.
 * Useful during incremental development.
 */
public class NotYetImplementedException extends RuntimeException {
    
    public NotYetImplementedException() {
        super("This functionality is not yet implemented");
    }
    
    public NotYetImplementedException(String message) {
        super(message);
    }
}