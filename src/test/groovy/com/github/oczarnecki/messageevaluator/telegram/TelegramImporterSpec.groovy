package com.github.oczarnecki.messageevaluator.telegram

import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

import javax.json.Json
import javax.json.JsonValue
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime

class TelegramImporterSpec extends Specification {

    LocalDateTime testDate = LocalDateTime.of(2015, 12, 24, 6, 0, 0)
    String testDateString = "2015-12-24T06:00:00"

    @Subject
    TelegramImporter importer
    InputStream input

    def setup() {
        input = Mock(InputStream)
        importer = new TelegramImporter(input)
    }


    def "a valid telegram message is parsed correctly"() {
        given: 'a valid telegram message'
        def messageEt = '{"type":"message", "date":"' + testDateString + '", "from":"Sender", "text":"message content"}'

        when: 'the message is parsed'
        def messageDto = importer.parseMessage(jsonValueFromString(messageEt))

        then: 'the contents of the dto match those of the entity'
        messageDto.getSender() == 'Sender'
        messageDto.getContent() == 'message content'
        messageDto.getTimestamp() == testDate
    }

    def "parsing a valid telegram chat containing more than messages is parsed correctly"() {
        given: 'a valid telegram chat'
        def chatEt = '{"name":"Chat Name", "messages":[' +
                '{"type":"message", "from":"someone", "text":""},' +
                '{"type":"message", "date":"' + testDateString + '", "from":"Sender", "text":"message content", "ignored field":true},' +
                '{"type":"not a message"}' +
                ']}'

        when: 'the chat is parsed'
        def chatDto = importer.parseChat(jsonValueFromString(chatEt))

        then: 'the contents of the dto match those of the entity'
        chatDto.getName() == 'Chat Name'
        chatDto.getMessages().size() == 1
        chatDto.getMessages().get(0) == new TelegramMessage(testDate,
                'Sender',
                'message content')
    }

    def "telegram data with multiple chats are parsed correctly"() {
        given: 'valid telegram data'
        def tgData = '{"chats":{"list":[' +
                '{"name":"ChatA", "messages":[{"type":"message", "date":"' + testDateString + '", "from":"Sender", "text":"message content"}]},' +
                '{"name":"ChatB", "messages":[{"type":"message", "date":"something illegal", "text":""}]}' +
                ']}}'

        when: 'the data are parsed'
        def chats = new TelegramImporter(inputStreamFromString(tgData)).importChats()

        then: 'the contents of the chats match the data'
        chats.size() == 2

        chats.stream().find({chat -> chat.name == 'ChatA'}).getMessages().size() == 1
        chats.stream().find({chat -> chat.name == 'ChatA'}).getMessages().get(0) == new TelegramMessage(testDate, "Sender", 'message content')

        chats.stream().find({chat -> chat.name == 'ChatB'}).getMessages().isEmpty()
    }

    @Unroll("an exception is thrown when given: '#tgData'")
    def "an exception is thrown on invalid data"() {
        when: 'the invalid data are parsed'
        def inputStream = inputStreamFromString(tgData)
        new TelegramImporter(inputStream).importChats()

        then: 'an ImportException is thrown'
        thrown(ImportException)

        where:
        tgData << [
                '{"chats":',
                '{"key":"unrelated data"}',
                '{key:"is not quoted!"}',
                '{"chats":{"list":"this is not a list"}}',
                '{"chats":{"list":[{"not a chat": true}]}}',
                '{"chats":{"list":[{"name": 42}]}}',
                '{"chats":{"list":[{"name": "Chat without messages"}]}}',
                '{"chats":{"list":[{"name": "Chat", "messages":"not a list"}]}}',
                '{"chats":{"list":[{"name": "Chat", "messages":[[]]}]}}',
                '{"chats":{"list":[{"name": "Chat", "messages":[{"not a message":true}]}]}}',
                '{"chats":{"list":[{"name": "Chat", "messages":[{"type":"message","date":"24.12.2015 06:00","text":"something", "from":"sender"}]}]}}',
                '{"chats":{"list":[{"name": "Chat", "messages":[{"type":"message","date":"2015-12-24T06:00:00","text_missing":"true", "from":"sender"}]}]}}',
                '{"chats":{"list":[{"name": "Chat", "messages":[{"type":"message","date":"2015-12-24T06:00:00","text":0, "from":"sender"}]}]}}',
                '{"chats":{"list":[{"name": "Chat", "messages":[{"type":"message","date":"2015-12-24T06:00:00","text":"something"}]}]}}',
                '{"chats":{"list":[{"name": "Chat", "messages":[{"type":"message","date":"2015-12-24T06:00:00","text":["expected:", false], "from":"sender"}]}]}}',
        ]
    }

    def "an ImportException is thrown when the underlying input stream produces an IOException"() {
        given: 'a bad InputStream'
        input._ >> { throw new IOException() }

        when: 'the importer attempts to parse data from the stream'
        importer.importChats()

        then: 'an ImportException is thrown'
        def exception = thrown(ImportException)
        exception.getCause() instanceof IOException
    }

    def "messages with null sender are ignored"() {
        given: 'a message with null sender'
        def message = '{"type":"message", "from":null, "text":"content", "date":"' + testDateString + '"}'

        when: 'the message is parsed'
        def messageDto = importer.parseMessage(jsonValueFromString(message))

        then: 'null is returned'
        messageDto == null
    }

    def "composite texts are parsed correctly"() {
        given: 'a composite text'
        def compositeText = '["text ", {"type":"hashtag", "text":"#composite"}]'

        when: 'the text is parsed'
        def parsedText = importer.parseText(jsonValueFromString(compositeText))

        then: 'the result is a flattened text'
        parsedText == 'text #composite'
    }

    InputStream inputStreamFromString(String str) {
        return new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8))
    }

    JsonValue jsonValueFromString(String str) {
        return Json.createReader(new StringReader(str)).readValue()
    }
}
