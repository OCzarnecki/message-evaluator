package com.github.oczarnecki.messageevaluator

import com.github.oczarnecki.messageevaluator.importer.telegram.TelegramChat
import com.github.oczarnecki.messageevaluator.importer.telegram.TelegramMessage
import spock.lang.Specification
import spock.lang.Subject

import java.time.LocalDateTime
import java.util.function.Consumer

class TelegramDataModelSpec extends Specification {

    private static final TelegramChat A_CHAT = new TelegramChat("chat", Collections.emptyList())

    @Subject
    private TelegramDataModel model

    def setup() {
        model = new TelegramDataModel()
    }

    def "new model contains empty collection of chats"() {
        expect:
        model.getChats().isEmpty()
    }

    def "the chats of a new model are immutable"() {
        when: 'one tries to modify the chats'
        model.getChats().add(A_CHAT)

        then: 'an exception is thrown'
        thrown(UnsupportedOperationException)
    }

    def "the chats of a used model are immutable"() {
        given: 'a used model'
        model.setChats(new ArrayList<>())

        when: 'one tries to modify the chats'
        model.getChats().add(A_CHAT)

        then: 'an exception is thrown'
        thrown(UnsupportedOperationException)
    }

    def "after a change listener is added, it is notified once, initially"() {
        given: 'a listener'
        def listener = Mock(Consumer)

        when: 'the listener is added'
        model.addChangeListener(listener)

        then: 'it is also notified'
        1 * listener.accept(model)
    }

    def "the setter notifies all listeners"() {
        given: 'a model with a change listener'
        def listener = Mock(Consumer)
        model.addChangeListener(listener)

        when: 'a new collection of chats is set'
        model.setChats(Collections.singleton(A_CHAT))

        then: 'the listener is called'
        1 * listener.accept(model)
    }

    def "chats may not be set to null"() {
        when: 'one attempts it'
        model.setChats(null)

        then: 'a NullPointerException is thrown'
        thrown(NullPointerException)
    }

    def "data import works correctly"() {
        when: 'telegram data are imported into the model'
        def resource = getClass().getClassLoader().getResource('telegramTestData/minimal.json')
        model.importTgData(new File(resource.toURI()))

        then: 'it is retrievable via getChats'
        model.getChats().size() == 1
        model.getChats().contains(new TelegramChat('Chat name',
                Collections.singletonList(new TelegramMessage(
                        LocalDateTime.of(2016, 12, 24, 6, 0, 0), 'Sender', 'Message text'))))

    }
}
