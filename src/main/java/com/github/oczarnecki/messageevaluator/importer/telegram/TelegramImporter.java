package com.github.oczarnecki.messageevaluator.importer.telegram;

import javax.json.*;
import javax.json.stream.JsonParsingException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Imports all telegram chats from a json data source
 */
public final class TelegramImporter {
    /**
     * json field names of telegram export file
     */
    private static final String CHATS_FIELD = "chats",
            CHATS_LIST_FIELD = "list",
            CHAT_NAME_FIELD = "name",
            MESSAGE_LIST_FIELD = "messages",
            MESSAGE_TYPE_FIELD = "type",
            MESSAGE_FROM_FIELD = "from",
            MESSAGE_TEXT_FIELD = "text",
            MESSAGE_DATE_FIELD = "date",
            MESSAGE_PART_TEXT_FIELD = MESSAGE_TEXT_FIELD;

    private final InputStream input;

    /**
     * Create a new Importer with an underlying input stream.
     *
     * @param input the {@link InputStream} where the telegram data is to be read from
     */
    public TelegramImporter(InputStream input) {
        this.input = input;
    }

    /**
     * Create a new Importer with an underlying file input stream.
     *
     * @param dataFile the file from which to create the {@link InputStream}
     * @throws ImportException when {@link FileInputStream#FileInputStream(File)} throws a FileNotFoundException
     */
    public TelegramImporter(File dataFile) throws ImportException {
        try {
            this.input = new FileInputStream(dataFile);
        } catch (FileNotFoundException exception) {
            throw new ImportException("source file does not exist, is not readable or is no file", exception);
        }
    }


    /**
     * Perform the import operation, by converting data from the underlying {@link InputStream} to a Collection of
     * {@link TelegramChat}s. After this method has completed, the stream is closed. Hence, it may only be called once.
     *
     * @return a collection of {@link TelegramChat}s represented by the underlying input stream.
     * @throws ImportException if the {@link InputStream} causes an {@link java.io.IOException} or the json data itself
     *                         is malformed
     */
    public Collection<TelegramChat> importChats() throws ImportException {
        try (JsonReader reader = Json.createReader(input)) {
            JsonObject telegramData = reader.readObject();
            JsonArray chatsEt = telegramData.getJsonObject(CHATS_FIELD).getJsonArray(CHATS_LIST_FIELD);
            List<TelegramChat> chats = new ArrayList<>();
            for (JsonValue jsonValue : chatsEt) {
                TelegramChat telegramChat = parseChat(jsonValue);
                chats.add(telegramChat);
            }
            return chats;
        } catch (JsonParsingException cause) {
            throw new ImportException("malformed json", cause);
        } catch (JsonException cause) {
            throw new ImportException("IOException while parsing json", cause.getCause());
        } catch (ClassCastException cause) {
            throw new ImportException("wrong json field type", cause);
        } catch (NullPointerException cause) {
            throw new ImportException("required json field not set", cause);
        }
    }

    /**
     * Convert a json representation of a chat to a DTO. Apart from the checked exception, it might also throw others on
     * invalid input. It should only be called from importChats().
     *
     * @param chatEtValue a {@link JsonValue} representing a {@link TelegramChat}
     * @return a {@link TelegramChat}
     * @throws ImportException if the json data are malformed.
     */
    private TelegramChat parseChat(JsonValue chatEtValue) throws ImportException {
        JsonObject chatEt = chatEtValue.asJsonObject();
        String name = chatEt.getString(CHAT_NAME_FIELD);
        List<TelegramMessage> messages = new ArrayList<>();
        for (JsonValue jsonValue : chatEt.getJsonArray(MESSAGE_LIST_FIELD)) {
            TelegramMessage telegramMessage = parseMessage(jsonValue);
            if (telegramMessage != null) {
                messages.add(telegramMessage);
            }
        }
        return new TelegramChat(name, messages);
    }

    /**
     * Convert a json representation of a telegram message to a DTO. Drops messages without text content (like files or
     * images), messages which are not of type message (like inviting members to groups) and messages where the sender
     * is null (messages sent by people who have deleted their accounts) by returning null. All other irregularities
     * cause exceptions to be thrown (not necessarily checked ones!). Hence, this method should only be called through
     * importChats().
     *
     * @param messageEtValue the json entity representing a messag
     * @return a {@link TelegramMessage}
     * @throws ImportException if (but not always if!) the data are malformed
     */
    private TelegramMessage parseMessage(JsonValue messageEtValue) throws ImportException {
        JsonObject messageEt = messageEtValue.asJsonObject();
        String type = messageEt.getString(MESSAGE_TYPE_FIELD);

        if (!type.equals("message")) {
            return null;
        }

        String content = parseText(messageEt.get(MESSAGE_TEXT_FIELD));
        if (content.isEmpty()) {
            return null;
        }

        String sender = getStringOrNull(messageEt.get(MESSAGE_FROM_FIELD));
        if (sender == null) {
            return null;
        }

        String dateString = messageEt.getString(MESSAGE_DATE_FIELD);
        LocalDateTime date;
        try {
            date = LocalDateTime.parse(dateString);
        } catch (DateTimeParseException e) {
            throw new ImportException("Could not parse date", e);
        }
        return new TelegramMessage(date, sender, content);
    }

    /**
     * Parses the contents of a text field to a string. Text fields are either simple (normal JsonStrings) or composite
     * (JsonArrays). Composite messages consist of strings and JsonObjects (ex. hashtags or mentions) which have a text
     * field themselves. This method concatenates the strings with the content of the nested text fields.
     *
     * @param jsonValue either a JsonString or a JsonArray representing a composite text
     * @return the string resulting from flattening the JsonValue
     * @throws ImportException if (but not always if!) the data are malformed
     */
    private String parseText(JsonValue jsonValue) throws ImportException {
        switch (jsonValue.getValueType()) {
            case STRING:
                return asString(jsonValue);
            case ARRAY:
                return jsonValue.asJsonArray().stream().map(textPart -> {
                    if (textPart.getValueType() == JsonValue.ValueType.STRING) {
                        return asString(textPart);
                    } else {
                        return textPart.asJsonObject().getString(MESSAGE_PART_TEXT_FIELD);
                    }
                }).collect(Collectors.joining());
            default:
                throw new ImportException("Unexpected type of message text part. Should be either string or object");
        }
    }

    /**
     * @param value a JsonString
     * @return the java string contained in the value
     */
    private String asString(JsonValue value) {
        return ((JsonString) value).getString();
    }

    /**
     * @param value either JsonValue.NULL or a JsonString
     * @return null if the value represents null, otherwise the java string represented by the value
     */
    private String getStringOrNull(JsonValue value) {
        return value.getValueType() == JsonValue.ValueType.NULL ? null : asString(value);
    }
}
