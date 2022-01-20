package indigo

class VirtualCardMenu {
    companion object {
        fun show() {
            val options = MenuOption.values().joinToString(", ","(","):", 4) { it.value }
            println("Choose an action $options")
        }
    }
}

class VirtualCardDeck {
    fun execute() {
        var isRunning = true
        while (isRunning) {
            VirtualCardMenu.show()
            val prompt = readLine()!!

            val function: FunctionCard = when (findByValue(prompt)) {
                MenuOption.RESET -> {
                    Reset()
                }
                MenuOption.SHUFFLE -> {
                    Shuffle()
                }
                MenuOption.GET -> {
                    Get()
                }
                MenuOption.EXIT -> {
                    isRunning = false
                    Exit()
                }
                MenuOption.NULL -> {
                    Wrong()
                }
            }
            function.execute()
        }
    }
}

class Wrong : FunctionCard {
    override fun execute() {
        println("Wrong action.")
    }
}

fun findByValue(value: String): MenuOption {
    for (enum in MenuOption.values()) {
        if (value == enum.value) return enum
    }
    return MenuOption.NULL
}

class Exit : FunctionCard {
    override fun execute() {
        println("Bye")
    }
}

class Get : FunctionCard {
    override fun execute() {
        val numberOfCards = readLine()!!
        try {
            println("Number of cards:")
            CardDeck.get(numberOfCards)
        } catch (e: InvalidNumberOfCardsException) {
            println(e.message)
        }
    }
}

class Shuffle : FunctionCard {
    override fun execute() {
        println("Card deck is shuffled.")
        CardDeck.shuffle()
    }
}

class Reset : FunctionCard {
    override fun execute() {
        println("Card deck is reset.")
        CardDeck.reset()
    }
}

interface FunctionCard {
    fun execute()
}

private const val INVALID_NUMBER_OF_CARDS = "Invalid number of cards."

private const val REMAINING_CARDS_ARE_INSUFFICIENT = "The remaining cards are insufficient to meet the request."

class CardDeck {
    companion object {
        var deck: MutableList<Card> = mutableListOf()
        val ranks = mutableListOf("A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K")
        val suits = mutableListOf("♦", "♥", "♠", "♣")

        fun reset() {
            val zipped = ranks.flatMap { rank -> suits.map { suit -> Card(rank, suit) } }
            deck = zipped.toMutableList()
        }

        fun shuffle() {
            deck.shuffle()
        }

        fun get(numberOfCards: String): MutableList<Card> {
            val numberOfCardsInt: Int
            if (numberOfCards.matches(Regex("\\d+"))) {
                numberOfCardsInt = numberOfCards.toInt()
            } else {
                throw InvalidNumberOfCardsException(INVALID_NUMBER_OF_CARDS)
            }
            if (numberOfCardsInt > 52 || numberOfCardsInt < 1) {
                throw InvalidNumberOfCardsException(INVALID_NUMBER_OF_CARDS)
            }
            if (numberOfCardsInt > deck.size) {
                throw InvalidNumberOfCardsException(REMAINING_CARDS_ARE_INSUFFICIENT)
            }
            val subL = deck.take(numberOfCardsInt)
            deck.subList(0, numberOfCardsInt).clear()
//            println(subL.joinToString(" "))
            return subL.toMutableList()
        }
    }
}

class InvalidNumberOfCardsException(message: String) : Exception(message)

enum class MenuOption(val value: String) {
    RESET("reset"), SHUFFLE("shuffle"), GET("get"), EXIT("exit"), NULL("null")
}