package indigo

import kotlin.random.Random

data class Table(val cards: MutableList<Card>)

class GameTurn(private val isPlayerFirst: Boolean) {
    private var table: Table
    private var player: Human
    private var computer: Computer
    private var lastWinner: Player

    init {
        CardDeck.reset()
        CardDeck.shuffle()
        table = Table(CardDeck.get("4"))

        player = Human(CardDeck.get("6"))
        computer = Computer(CardDeck.get("6"))
        lastWinner = if (isPlayerFirst) {
            player
        } else {
            computer
        }
    }

    fun play() {
        initialCards()
        cardsOnTable()
        val sumCardsInHands = computer.hand.size + player.hand.size
        while (sumCardsInHands <= 52) {
            if (isPlayerFirst) {
                try {
                    player.turn(table)
                } catch (e: ExitException) {
                    break
                } catch (e: WonException) {
                    score()
                    lastWinner = player
                } catch (e: InvalidNumberOfCardsException) {
                    remainingCards()
                    break
                }

                cardsOnTable()

                try {
                    computer.turn(table)
                } catch (e: WonException) {
                    score()
                    lastWinner = computer
                } catch (e: InvalidNumberOfCardsException) {
                    remainingCards()
                    break
                }

                cardsOnTable()
            } else {
                try {
                    computer.turn(table)
                } catch (e: WonException) {
                    score()
                    lastWinner = computer
                } catch (e: InvalidNumberOfCardsException) {
                    remainingCards()
                    break
                }

                cardsOnTable()

                try {
                    player.turn(table)
                } catch (e: ExitException) {
                    break
                } catch (e: WonException) {
                    score()
                    lastWinner = player
                } catch (e: InvalidNumberOfCardsException) {
                    remainingCards()
                    break
                }

                cardsOnTable()
            }
        }
        println("Game over")
    }

    private fun remainingCards() {
        val count = countPoints(table)
        if (lastWinner == player) {
            player.cards += table.cards.size
            player.score += count
        } else {
            computer.cards += table.cards.size
            computer.score += count
        }
        finalScore()
    }

    private fun finalScore() {
        if (computer.cards == player.cards) {
            if (isPlayerFirst) {
                player.score += 3
            } else {
                computer.score += 3
            }
        } else if (computer.cards > player.cards) {
            computer.score += 3
        } else {
            player.score += 3
        }
        score()
    }

    private fun score() {
        println("Score: ${player.name} ${player.score} - ${computer.name} ${computer.score}")
        println("Cards: ${player.name} ${player.cards} - ${computer.name} ${computer.cards}")
    }

    private fun cardsOnTable() {
        if (table.cards.size == 0) {
            println("No cards on the table")
        } else {
            println("${table.cards.size} cards on the table, and the top card is ${table.cards.last()}")
        }
    }

    private fun initialCards() {
        println("Initial cards on the table: ${table.cards.joinToString(" ")}")
    }
}

abstract class Player(var hand: MutableList<Card>) {
    abstract fun turn(table: Table)

    fun hasWon(table: Table, chosen: Card): Boolean {
        if (table.cards.size == 0) {
            return false
        }
        val topCard = table.cards.last()
        return topCard.suit == chosen.suit || topCard.rank == chosen.rank
    }
}

private fun countPoints(table: Table): Int {
    val count =
        table.cards.count { it.rank == "A" || it.rank == "10" || it.rank == "J" || it.rank == "Q" || it.rank == "K" }
    return count
}

class Human(hand: MutableList<Card>) : Player(hand) {
    val name = "Player"
    var score: Int = 0
    var cards: Int = 0

    override fun turn(table: Table) {
        if (hand.isEmpty()) {
            hand = CardDeck.get("6")
        }
        val enumerateCards = hand.withIndex().map { (index, card) -> "${index + 1})$card" }.toMutableList()
        println("Cards in hand: ${enumerateCards.joinToString(" ")}")

        val (numberChosenCard, chosenCard) = chosenCard()
        val hasWon = hasWon(table, chosenCard)
        if (hasWon) {
            table.cards.add(chosenCard)
            val count = countPoints(table)
            score += count
            cards += table.cards.size
            table.cards.clear()
            hand.removeAt(numberChosenCard - 1)
            println("$name wins cards")
            throw WonException()
        } else {
            table.cards.add(hand[numberChosenCard - 1])
            hand.removeAt(numberChosenCard - 1)
        }
    }

    private fun chosenCard(): Pair<Int, Card> {
        while (true) {
            val size = hand.size
            println("Choose a card to play (1-$size):")
            val numberChosenCard = readLine()!!
            if (numberChosenCard.matches(Regex("\\d")) && numberChosenCard.toInt() in 1..size) {
                val chosenCard = hand[numberChosenCard.toInt() - 1]
                return Pair(numberChosenCard.toInt(), chosenCard)
            } else if (numberChosenCard == "exit") {
                throw ExitException()
            }
        }
    }
}

class Computer(hand: MutableList<Card>) : Player(hand) {
    val name = "Computer"
    var score: Int = 0
    var cards: Int = 0

    override fun turn(table: Table) {
        if (hand.isEmpty()) {
            hand = CardDeck.get("6")
        }
        val handCards = hand.joinToString(" ")
        println(handCards)
        val chosenCard = chooseCard(table)
        println("$name plays $chosenCard")

        val hasWon = hasWon(table, chosenCard)
        if (hasWon) {
            table.cards.add(chosenCard)
            val count = countPoints(table)
            score += count
            cards += table.cards.size
            table.cards.clear()
            hand.remove(chosenCard)
            println("$name wins cards")
            throw WonException()
        } else {
            table.cards.add(chosenCard)
            hand.remove(chosenCard)
        }
    }

    private fun chooseCard(table: Table): Card {
        if (hand.size == 1) return hand.first() //1
        else if (table.cards.size != 0 && candidates(table).size == 1) return candidates(table).first() //2
        else if (table.cards.size == 0) { //3
            return if (sameSuitInHand().isNotEmpty()) sameSuitInHand()[Random.nextInt(0, sameSuitInHand().size)] //*
            else if (sameSuitInHand().isEmpty() && sameRankInHand().isNotEmpty()) {
                sameRankInHand()[Random.nextInt(0, sameRankInHand().size)] //**
            } else {
                hand[Random.nextInt(0, hand.size)] //***
            }
        } else if (candidates(table).isEmpty()) { //4
            return if (sameSuitInHand().isNotEmpty()) sameSuitInHand()[Random.nextInt(0, sameSuitInHand().size)] //*
            else if (sameSuitInHand().isEmpty() && sameRankInHand().isNotEmpty()) {
                sameRankInHand()[Random.nextInt(0, sameRankInHand().size)] //**
            } else {
                hand[Random.nextInt(0, hand.size)] //***
            }
        } else if (candidates(table).size >= 2) { //5
            return if (sameSuitInCandidates(table).size >= 2) { //*
                sameSuitInCandidates(table)[Random.nextInt(0, sameSuitInCandidates(table).size)]
            } else if (sameRankInCandidates(table).size >= 2) { //**
                sameRankInCandidates(table)[Random.nextInt(0, sameRankInCandidates(table).size)]
            } else {
                candidates(table)[Random.nextInt(0, candidates(table).size)] //***
            }
        }
        return hand[Random.nextInt(0, hand.size)]
    }

    private fun sameRankInCandidates(table: Table): List<Card> {
        val candidates = candidates(table)
        return candidates.groupBy { it.rank }.map { it.value }.filter { it.size > 1 }.flatten()
    }

    private fun sameSuitInCandidates(table: Table): List<Card> {
        val candidates = candidates(table)
        return candidates.groupBy { it.suit }.map { it.value }.filter { it.size > 1 }.flatten()
    }

    private fun sameRankInHand(): List<Card> {
        return hand.groupBy { it.rank }.map { it.value }.filter { it.size > 1 }.flatten()
    }

    private fun sameSuitInHand(): List<Card> {
        return hand.groupBy { it.suit }.map { it.value }.filter { it.size > 1 }.flatten()
    }

    private fun candidates(table: Table): List<Card> {
        val last = table.cards.last()
        return hand.filter { it.rank == last.rank || it.suit == last.suit }
    }
}

class Game {
    companion object {
        fun show() {
            println("Indigo Card Game")
            var isRunning = true
            while (isRunning) {
                println("Play first?")
                val type = when (readLine()!!.lowercase()) {
                    "yes" -> {
                        isRunning = false
                        GameTurn(true)
                    }
                    "no" -> {
                        isRunning = false
                        GameTurn(false)
                    }
                    else -> {
                        continue
                    }
                }
                type.play()
            }
        }
    }
}

class ExitException : Exception()
class WonException : Exception()
