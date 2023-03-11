package personal.chris.unciv.notifier

class Notifier {

    fun notify(target: String) {
        println("Notifying ${target} it is their turn")
        // TODO
        println("Notification sent to ${target}")
    }

    // TODO we could also regularly check how long it's been the current player's turn, and trigger some extra notification
}