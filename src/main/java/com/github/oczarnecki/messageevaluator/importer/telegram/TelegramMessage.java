package com.github.oczarnecki.messageevaluator.importer.telegram;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Immutable object representing a single telegram text message
 */
public final class TelegramMessage {
    private final String sender;
    private final String content;
    private final LocalDateTime timestamp;

    TelegramMessage(LocalDateTime timestamp, String sender, String content) {
        this.sender = sender;
        this.content = content;
        this.timestamp = timestamp;
    }

    /**
     * @return the name of the sender of the message
     */
    public String getSender() {
        return sender;
    }

    /**
     * @return the text content of the message
     */
    public String getContent() {
        return content;
    }

    /**
     * @return the timestamp at which the message was sent
     */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TelegramMessage that = (TelegramMessage) o;
        return Objects.equals(sender, that.sender) &&
                Objects.equals(content, that.content) &&
                Objects.equals(timestamp, that.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sender, content, timestamp);
    }

    @Override
    public String toString() {
        return "TelegramMessage{" +
                "sender='" + sender + '\'' +
                ", content='" + content + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
