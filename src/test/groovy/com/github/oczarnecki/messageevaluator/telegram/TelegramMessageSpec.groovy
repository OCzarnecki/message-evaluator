package com.github.oczarnecki.messageevaluator.telegram

import nl.jqno.equalsverifier.EqualsVerifier
import spock.lang.Specification

class TelegramMessageSpec extends Specification {
    def "equals and hash code work correctly"() {
        expect: 'equals and hashCode work'
        EqualsVerifier.forClass(TelegramMessage).verify()
    }
}
