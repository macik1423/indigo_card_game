package indigo

class Card(val rank: String, val suit: String) {
    override fun toString(): String {
        return "$rank$suit"
    }
}