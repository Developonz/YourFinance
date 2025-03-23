import androidx.room.Embedded
import androidx.room.Relation
import com.example.yourfinance.model.entities.MoneyAccount
import com.example.yourfinance.model.entities.Payment

data class FullPayment(
    @Embedded val payment: Payment,
    @Relation(
        parentColumn = "moneyAccId",
        entityColumn = "id"
    )
    val moneyAcc: MoneyAccount
)
