package com.github.oczarnecki.messageevaluator.telegram;

/**
 * Checked {@link Exception} that might occur while importing data from an external source.
 */
public class ImportException extends Exception {

    private static final String MESSAGE_PREFIX = "Could not import data: ";

    /**
     * Creates an {@link ImportException} with the given message prepended with "Could not import data: "
     *
     * @param message error message
     */
    ImportException(String message) {
        super(MESSAGE_PREFIX + message);
    }

    /**
     * Creates an {@link ImportException} with the given cause and message, prepending the message with
     * "Could not import data: "
     *
     * @param message error message
     * @param cause cause of error
     */
    ImportException(String message, Throwable cause) {
        super(MESSAGE_PREFIX + message, cause);
    }
}
