import com.beust.klaxon.Klaxon
import io.github.rybalkinsd.kohttp.dsl.httpGet
import io.github.rybalkinsd.kohttp.dsl.httpPost
import io.github.rybalkinsd.kohttp.dsl.httpPut
import org.junit.Assert.assertEquals
import org.junit.Test


class BillingServiceTest {
    @Test
    fun createAccount() {
        val bs = BillingService()
        val account = bs.createAccount(100)
        assertEquals(1, bs.accounts.size)
        assertEquals(account, bs.accounts[account.id])
    }

    @Test
    fun transfer() {
        val bs = BillingService()
        val from = bs.createAccount(100)
        val to = bs.createAccount(100)
        assertEquals(2, bs.accounts.size)
        bs.transfer(from, to, 50)
        assertEquals(50, bs.accounts[from.id]!!.amount)
        assertEquals(150, bs.accounts[to.id]!!.amount)
    }
}

class BillingAPITest {
    @Test
    fun createAccounts() {
        BillingController(BillingService())

        assertEquals("[]", listAccountsFromAPI())

        val acc1 = createAccount()
        val acc2 = createAccount()

        assertEquals(acc2, Klaxon().parse<Account>(getAccountFromAPI(acc2.id)))

        assertEquals(Klaxon().toJsonString(mutableListOf(acc1, acc2).sortedBy { it.id }), listAccountsFromAPI())
        assertEquals(Klaxon().toJsonString(acc1), getAccountFromAPI(acc1.id))
        assertEquals(Klaxon().toJsonString(acc2), getAccountFromAPI(acc2.id))
    }

    @Test
    fun transfer() {
        BillingController(BillingService())

        val from = createAccount()
        val to = createAccount()

        httpPut {
            host = "localhost"
            port = 4567
            path = "/transfer"
            param {
                "amount" to 50
                "from" to from.id
                "to" to to.id
            }
        }.body()!!.string()

        assertEquals(50, Klaxon().parse<Account>(getAccountFromAPI(from.id))!!.amount)
        assertEquals(150, Klaxon().parse<Account>(getAccountFromAPI(to.id))!!.amount)
    }

    @Test
    fun `transfer when not enough money`() {
        BillingController(BillingService())

        val from = createAccount()
        val to = createAccount()

        val transferResult = httpPut {
            host = "localhost"
            port = 4567
            path = "/transfer"
            param {
                "amount" to 200
                "from" to from.id
                "to" to to.id
            }
        }.body()!!.string()

        assertEquals("{\"error\":\"Not enough money in account\"}", transferResult)
        assertEquals(100, Klaxon().parse<Account>(getAccountFromAPI(from.id))!!.amount)
        assertEquals(100, Klaxon().parse<Account>(getAccountFromAPI(to.id))!!.amount)
    }

    @Test
    fun `transfer from un-existing account`() {
        BillingController(BillingService())

        val acc = createAccount()

        val transferResult = httpPut {
            host = "localhost"
            port = 4567
            path = "/transfer"
            param {
                "amount" to 50
                "from" to acc.id
                "to" to "NOT-EXISTING-ACCOUNT-ID"
            }
        }.body()!!.string()

        assertEquals("{\"error\":\"No such account NOT-EXISTING-ACCOUNT-ID\"}", transferResult)
        assertEquals(100, Klaxon().parse<Account>(getAccountFromAPI(acc.id))!!.amount)
    }

    @Test
    fun `transfer to un-existing account`() {
        BillingController(BillingService())

        val acc = createAccount()

        val transferResult = httpPut {
            host = "localhost"
            port = 4567
            path = "/transfer"
            param {
                "amount" to 50
                "from" to "NOT-EXISTING-ACCOUNT-ID"
                "to" to acc.id
            }
        }.body()!!.string()

        assertEquals("{\"error\":\"No such account NOT-EXISTING-ACCOUNT-ID\"}", transferResult)
        assertEquals(100, Klaxon().parse<Account>(getAccountFromAPI(acc.id))!!.amount)
    }

    private fun createAccount() =
        Klaxon().parse<Account>(httpPost {
            host = "localhost"
            port = 4567
            path = "/account/create"
            param { "amount" to 100 }
        }.body()!!.string())!!

    private fun listAccountsFromAPI() = httpGet {
        host = "localhost"
        port = 4567
        path = "/account/list"
    }.body()!!.string()

    private fun getAccountFromAPI(id: String) = httpGet {
        host = "localhost"
        port = 4567
        path = "/account/id/$id"
    }.body()!!.string()
}