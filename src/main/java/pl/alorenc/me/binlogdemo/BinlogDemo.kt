package pl.alorenc.me.binlogdemo

import com.github.shyiko.mysql.binlog.BinaryLogClient
import com.github.shyiko.mysql.binlog.event.DeleteRowsEventData
import com.github.shyiko.mysql.binlog.event.EventData
import com.github.shyiko.mysql.binlog.event.UpdateRowsEventData
import com.github.shyiko.mysql.binlog.event.WriteRowsEventData
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.core.SdkBytes
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.kinesis.KinesisClient
import software.amazon.awssdk.services.kinesis.model.PutRecordRequest
import java.io.Serializable

internal enum class UserTableColumns(val value: Int) {
    ID(0),
    FIRST_NAME(1),
    LAST_NAME(2);
}

internal data class User(
    val id: Int,
    val firstName: String,
    val lastName: String
)

object BinlogDemo {
    private val userStore: MutableMap<Int, User> = mutableMapOf()
    private val kinesisClient = KinesisClient.builder()
        .region(Region.EU_WEST_1)
        .credentialsProvider {
            AwsBasicCredentials.create(
                "",
                ""
            )
        }
        .build()

    @JvmStatic fun main(args: Array<String>) {
        val client = createBinaryLogClient()

        client.registerEventListener { ev ->
            when (val data = ev.getData<EventData>()) {
                is WriteRowsEventData -> handleUserWriteEventData(data)
                is UpdateRowsEventData -> handleUserUpdateEventData(data)
                is DeleteRowsEventData -> handleUserDeleteEventData(data)
            }
        }

        client.connect()
    }

    private fun createBinaryLogClient(): BinaryLogClient {
        return BinaryLogClient(
            "",
            3306,
            "",
            ""
        )
    }

    private fun handleUserDeleteEventData(data: DeleteRowsEventData) {
        data.rows.forEach { userRow ->
            val user = createUserFromTableRow(userRow)
            println("Removing user $user")

            userStore.remove(user.id)
            sendUserRecord(user)
        }
    }

    private fun handleUserUpdateEventData(data: UpdateRowsEventData) {
        data.rows.forEach { updateSet ->
            val updatedUserId = updateSet.key[0] as Int
            val userRow = updateSet.value
            val userBefore = userStore[updatedUserId]
            val userAfter = createUserFromTableRow(userRow)

            println("Updating user id $updatedUserId before: $userBefore after: $userAfter")

            userStore[updatedUserId] = userAfter
            sendUserRecord(userAfter)
        }
    }

    private fun handleUserWriteEventData(data: WriteRowsEventData) {
        data.rows.forEach { userRow ->
            val user = createUserFromTableRow(userRow)
            println("Storing user $user")

            userStore[user.id] = user
            sendUserRecord(user)
        }
    }

    private fun sendUserRecord(user: User) {
        val userId = 1.toString()
        val request = PutRecordRequest.builder()
            .partitionKey(userId)
            .streamName("kinesis-binlog")
            .data(SdkBytes.fromByteArray(user.firstName.toByteArray()))
            .build()
        kinesisClient.putRecord(request)
    }

    private fun createUserFromTableRow(row: Array<Serializable>?) =
        User(
            row!![UserTableColumns.ID.value] as Int,
            row[UserTableColumns.FIRST_NAME.value] as String,
            row[UserTableColumns.LAST_NAME.value] as String
        )
}
