package com.github.oczarnecki.messageevaluator.telegram;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * An immutable object representing a telegram chat
 */
public final class TelegramChat {
    private final String name;
    private final List<TelegramMessage> messages;

    /**
     * Creates an immutable {@link TelegramChat}. The list of messages is copied into an immutable one.
     *
     * @param name name of the chat
     * @param messages list of messages contained in the chat
     */
    TelegramChat(String name, List<TelegramMessage> messages) {
        this.name = name;
        this.messages = Collections.unmodifiableList(new ArrayList<>(messages));
    }

    /**
     * @return the name of the chat
     */
    public String getName() {
        return name;
    }

    /**
     * @return an immutable list of all messages contained in the chat
     */
    public List<TelegramMessage> getMessages() {
        return messages;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TelegramChat that = (TelegramChat) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(messages, that.messages);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, messages);
    }

    @Override
    public String toString() {
        return "TelegramChat{" +
                "name='" + name + '\'' +
                ", messages=" + messages +
                '}';
    }
}