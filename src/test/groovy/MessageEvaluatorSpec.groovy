import spock.lang.Specification

class MessageEvaluatorSpec extends Specification {
    def "the correct greeting is returned"() {
        expect:
        new MessageEvaluator().getGreeting() == 'Hello World!'
    }
}
