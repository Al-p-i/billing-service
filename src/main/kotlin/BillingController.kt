import com.beust.klaxon.Klaxon
import spark.Spark.*

class BillingController(billingService: BillingService) {
    init {
        post("/account/create") { req, res ->
            res.type("application/json")
            val amount = (req.queryMap("amount").value() ?: "0").toLong()
            if (amount < 0) return@post "Initial amount must not be negative".also { res.status(400) }
            val account = billingService.createAccount(amount)
            Klaxon().toJsonString(account)
        }
        put("/transfer") { req, res ->
            val from = req.queryMap()["from"].value() ?: return@put "Source account not specified".also { res.status(400) }
            val to = req.queryMap()["to"].value() ?: return@put "Target account not specified".also { res.status(400) }
            val amount = req.queryMap()["amount"].value()?.toLong() ?: return@put "Amount not specified".also { res.status(400) }

            val fromAcc = billingService.accounts[from] ?: return@put "No such account $from".also { res.status(404) }
            val toAcc = billingService.accounts[to] ?: return@put "No such account $to".also { res.status(404) }
            try {
                billingService.transfer(fromAcc, toAcc, amount)
            } catch (e: TransferException) {
                return@put e.message.also { res.status(406) }
            }
        }
        get("/account/list") { req, res ->
            res.type("application/json")
            Klaxon().toJsonString(billingService.accounts.values.sortedBy { it.id })
        }
        get("/account/id/:id") { req, res ->
            res.type("application/json")
            Klaxon().toJsonString(billingService.accounts[req.params(":id")])
        }
    }
}

fun main(args: Array<String>) {
    BillingController(BillingService()) //Keep it simple, no DI framework this time
}