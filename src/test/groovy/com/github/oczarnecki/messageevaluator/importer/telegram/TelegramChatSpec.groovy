package com.github.oczarnecki.messageevaluator.importer.telegram

import nl.jqno.equalsverifier.EqualsVerifier
import spock.lang.Specification

import java.time.LocalDateTime

import static java.util.Collections.emptyList
import static java.util.Collections.singletonList

class TelegramChatSpec extends Specification {
    def MESSAGE = new TelegramMessage(LocalDateTime.now(), "sender", "content")

    def "equals and hash code work correctly"() {
        expect: 'equals and hashCode work'
        EqualsVerifier.forClass(TelegramChat).verify()
    }

    def "a telegram chat holds its own copy of messages"() {
        given: 'a list of messages'
        def messages = new ArrayList(singletonList(MESSAGE))

        when: 'a chat is created an the list is modified'
        def chat = new TelegramChat("chat", messages)
        messages.clear()

        then: 'the chat is not affected'
        chat.getMessages().size() == 1
    }

    def "the messages of a chat are not modifiable"() {
        given: 'a chat'
        def chat = new TelegramChat("chat", emptyList())

        when: 'someone tries to modify the chats messages'
        chat.getMessages().add(MESSAGE)

        then: 'an UnsupportedOperationException is thrown'
        thrown(UnsupportedOperationException)
    }
}
