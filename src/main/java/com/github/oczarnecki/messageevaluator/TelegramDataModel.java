package com.github.oczarnecki.messageevaluator;

import com.github.oczarnecki.messageevaluator.importer.telegram.ImportException;
import com.github.oczarnecki.messageevaluator.importer.telegram.TelegramChat;
import com.github.oczarnecki.messageevaluator.importer.telegram.TelegramImporter;
import com.github.oczarnecki.messageevaluator.importer.telegram.TelegramMessage;

import java.io.File;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The model represents a collection of {@link TelegramChat}s. Listeners can be added to it, which get notified when the
 * chats are changed.
 */
public final class TelegramDataModel {
    private Collection<TelegramChat> chats;
    private Collection<Consumer<TelegramDataModel>> changeListeners;
    private LocalDateTime earliestTimestamp;
    private LocalDateTime latestTimestamp;

    /**
     * Constructor
     */
    public TelegramDataModel() {
        chats = Collections.emptySet();
        changeListeners = new ArrayList<>();
    }

    /**
     * @return an unmodifiable view of this model's collection of chats
     */
    public Collection<TelegramChat> getChats() {
        return Collections.unmodifiableCollection(chats);
    }

    /**
     * Set the chats and notify all listeners.
     *
     * @param chats new {@link TelegramChat}s. Null is not allowed, use an empty collection instead.
     */
    private void setChats(Collection<TelegramChat> chats) {
        Objects.requireNonNull(chats);
        this.chats = chats;
        recalculateBounds();

        changeListeners.forEach(listener -> listener.accept(this));
    }

    /**
     * Adds a new listener to the model and fire a change event at it at once. The change event is implemented by a
     * call to the accept method of the listener. This model instance is passed as the parameter.
     *
     * @param listener the change listener, a {@link Consumer<TelegramDataModel>}
     */
    public void addChangeListener(Consumer<TelegramDataModel> listener) {
        changeListeners.add(listener);
        listener.accept(this);
    }

    /**
     * Imports Telegram data from a file and populates the model with it.
     *
     * @param dataFile source file
     * @throws ImportException when the data are malformed or the file can not be accessed
     * @see TelegramImporter#importChats()
     */
    public void importTgData(File dataFile) throws ImportException {
        setChats(new TelegramImporter(dataFile).importChats());
    }

    public LocalDateTime getEarliestEntryTimestamp() {
        return earliestTimestamp;
    }

    public LocalDateTime getLatestEntryTimestamp() {
        return latestTimestamp;
    }

    private void recalculateBounds() {
        List<LocalDateTime> timestamps = chats.stream()
                .map(chat -> chat.getMessages().stream())
                .reduce(Stream::concat)
                .orElse(Stream.empty())
                .map(TelegramMessage::getTimestamp)
                .collect(Collectors.toList());
        earliestTimestamp = timestamps.stream().min(LocalDateTime::compareTo).orElse(null);
        latestTimestamp = timestamps.stream().max(LocalDateTime::compareTo).orElse(null);
    }
}
