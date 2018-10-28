package com.github.oczarnecki.messageevaluator;

import com.github.oczarnecki.messageevaluator.telegram.ImportException;
import com.github.oczarnecki.messageevaluator.telegram.TelegramChat;
import com.github.oczarnecki.messageevaluator.telegram.TelegramImporter;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

/**
 * Entry point of the application
 */
public final class MessageEvaluator {
    public static void main(String[] args) throws IOException, ImportException {
        if (args.length == 0) {
            System.out.println("Please provide the path of the json file to import");
            System.exit(1);
        }
        Path tgDataPath = Paths.get(args[0]);
        InputStream tgDataStream = Files.newInputStream(tgDataPath);
        Collection<TelegramChat> chats = new TelegramImporter(tgDataStream).importChats();
        chats.forEach(chat -> System.out.println(chat.getName()));
    }
}
