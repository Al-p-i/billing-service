import java.util.*
import java.util.concurrent.ConcurrentHashMap

data class Account(val id: String, var amount: Long)

class BillingService {
    val accounts = ConcurrentHashMap<String, Account>()

    @Throws(TransferException::class)
    fun transfer(fromAcc: Account, toAcc: Account, amount: Long) {
        val accs = listOf(fromAcc, toAcc).sortedBy { it.id }
        synchronized(accs[0]) {
            synchronized(accs[1]) {
                val newAmount = fromAcc.amount - amount
                if (newAmount < 0) throw TransferException("Not enough money in account")
                fromAcc.amount = fromAcc.amount - amount
                toAcc.amount = toAcc.amount + amount
            }
        }
    }

    fun createAccount(amount: Long): Account {
        val account = Account(UUID.randomUUID().toString(), amount)
        accounts[account.id] = account
        return account
    }
}

class TransferException(msg: String) : RuntimeException(msg)