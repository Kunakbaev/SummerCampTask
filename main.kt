import java.util.Vector
import java.util.HashMap
import kotlin.system.exitProcess

class Book(stringToParse: String) {
    var name = ""
    var count = 0
    var cost = 0
    val stringToParse = stringToParse
    init {
        this.parse()
    }

    fun parse() {
        // stringToParse = (<name>, <count>, <cost>)
        // println("stringToParse : $stringToParse")
        var cnt = 0
        var temp = ""
        for (i in this.stringToParse.length - 1 downTo 0) {
            val ch = this.stringToParse[i]
            temp += ch
            if (ch == ',' && cnt != 2) {
                temp = temp.reversed()
                if (cnt == 0) cost = temp.drop(2).dropLast(1).toInt()
                if (cnt == 1) count = temp.drop(2).toInt()
                temp = ""; ++cnt
            }
        }
        name = temp.reversed().drop(1)
    }
}

class Server(startingState: String) {
    var startingState = startingState
    var balance = 0
    var COMMANDS = Vector<String>()
    var books = Vector<Book>()
    var boughtBooks = Vector<Book>()
    var bookInd = HashMap<String, Int>()

    init {
        COMMANDS.addAll(listOf("print balance", "show books in stock", "buy",
        "show bought books", "exit", "--help"))
        parseState()
    }

    fun parseState() {
        this.balance = this.startingState.split(",")[0].drop("balance: ".length).toInt()

        var line = ""
        val list = this.startingState.split("[")
        for (i in 1..list.size - 1) line += list[i]

        var balance = 0
        var quotationBalance = 0
        var temp = ""
        for (ch in line){
            if (ch == '(' && quotationBalance % 2 == 0) balance += 1
            if (ch == ')' && quotationBalance % 2 == 0) balance -= 1
            if (ch == '"') quotationBalance += 1
            temp += ch
            if (balance == 0 && quotationBalance % 2 == 0 && ch == ')') {
                if (this.books.size >= 1) temp = temp.drop(2)
                this.books.add(Book(temp))
                this.bookInd[this.books.lastElement().name] = this.books.size - 1
                temp = ""
            }
        }
    }

    fun shutServer() {
        println("Goodbye. Have a good day!")
        exitProcess(0)
    }

    fun printBalance() {
        val line = "balance : " + this.balance.toString() + " руб."
        this.printRussianString(line)
    }

    fun showBooks() {
        println("Our shop have these books:")
        for (book in this.books) {
            if (book.count >= 1)
                this.printRussianString("${book.name}, ${book.count} шт., ${book.cost} руб.")
        }
    }

    fun showBoughtBooks() {
        println("You have bought these books:")
        for (book in this.boughtBooks)
            this.printRussianString("${book.name}, ${book.count} шт.")
    }

    fun showHelp() {
        val breakLine = "--------------------"
        println("-----This is helping menu-----\n")
        println("Coomand: print balance")
        println("Description: shows user's current balance\n$breakLine")
        println("Command: show books in stock")
        println("Description: shows books that shop currently have\n$breakLine")
        println("Command: buy book <name> <count>")
        println("Description: allows user buy book\n$breakLine")
        println("Command: show bought books")
        println("Description: shows books that user have bought\n$breakLine")
        println("Command: exit")
        println("Description: shuts down server\n$breakLine")
        println("Command: --help")
        println("Description: shows helping menu\n\n")
    }

    fun buyBooks(garbageLine: String) {
        var line = ""
        val list = garbageLine.split("buy ")
        for (i in 1..list.size - 1) line += list[i]
        var count = -1
        var name = ""
        var temp = ""
        try {
            for (ch in line.reversed()) {
                temp += ch
                if (ch == ' ' && count == -1) {
                    count = temp.reversed().drop(1).toInt()
                    temp = "";
                }
            }
        } catch(e: java.lang.Exception){
            println("Something went wrong. Try type in a different way")
            return
        }
        name = temp.reversed()
        this.printRussianString("book name: $name, count: $count")
        if (count <= 0){
            println("Please type positive amount of books")
            return
        }
        if (!bookInd.containsKey(name)) {
            println("Sorry, no deal. We don't have this book in our store.")
            return
        }
        val book = this.books[this.bookInd[name]!!]
        if (book.count < count) {
            println("Sorry, no deal. We don't have amount of books that you want.")
            return
        }
        val cost = book.cost * count
        if (this.balance < cost) {
            println("Sorry, no deal. You don't have enought money.")
            return
        }
        println("Deal. Everything is fine.")
        this.books[this.bookInd[name]!!].count -= count
        this.balance -= cost
        book.count = count
        this.boughtBooks.add(book)
    }

    fun printRussianString(line: String) {
        val bytes = line.toByteArray()
        val newLine = String(bytes, charset("Windows-1251"))
        println(newLine)
    }

    fun completeCommand(line: String) {
        var currentCommand = "?"
        for (command in COMMANDS) {
            if ((line.indexOf(command) == 0 && command == "buy") || line == command) {
                currentCommand = command
                break
            }
        }
        when (currentCommand) {
            "exit" -> this.shutServer()
            "print balance" -> this.printBalance()
            "show books in stock" -> this.showBooks()
            "buy" -> this.buyBooks(line)
            "show bought books" -> this.showBoughtBooks()
            "--help" -> this.showHelp()
            else -> println("I don't understand")
        }
    }
}

fun main() {
    print("type please starting state: ")
    var line = readln()
    val server = Server(line)

    while (true) {
        print("your command: ")
        line = readln()
        server.completeCommand(line)
    }
}

/*

balance: 1000, books: [("Алгебра (99), 10 класс", 5, 100), ("Теория чисел, 2 класс", 42, 500)]
buy "Алгебра (99), 10 класс" 2
buy "daskljfdkjsfljild" 910
buy "dfafjljdf" 1092
show bought books
--help
show books in stock
print balance
exit

 */
