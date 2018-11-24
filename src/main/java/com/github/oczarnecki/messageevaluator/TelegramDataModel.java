package com.github.oczarnecki.messageevaluator;

import com.github.oczarnecki.messageevaluator.importer.telegram.ImportException;
import com.github.oczarnecki.messageevaluator.importer.telegram.TelegramChat;
import com.github.oczarnecki.messageevaluator.importer.telegram.TelegramImporter;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * The model represents a collection of {@link TelegramChat}s. Listeners can be added to it, which get notified when the
 * chats are changed.
 */
public final class TelegramDataModel {
    private Collection<TelegramChat> chats;
    private Collection<Consumer<TelegramDataModel>> changeListeners;

    /**
     * Constructor
     */
    public TelegramDataModel() {
        chats = Collections.emptySet();
        changeListeners = new ArrayList<>();
    }

    /**
     * @return an unmodifiable view on this model's collection of chats
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
}
