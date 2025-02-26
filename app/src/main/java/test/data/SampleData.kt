package test.data
import test.model.ChefAccount

object SampleData {

    val accounts = listOf(
        ChefAccount("chef1", "123456", 1),
        ChefAccount("chef2", "123456", 2),
        ChefAccount("chef3", "123456", 3),
        ChefAccount("chef4", "123456", 4),
        ChefAccount("chef5", "123456", 5)
    )
}