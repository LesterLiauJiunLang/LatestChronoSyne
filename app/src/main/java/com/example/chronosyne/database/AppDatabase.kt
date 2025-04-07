import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.chronosyne.database.dao.UserDao
import com.example.chronosyne.database.entities.EventCategoryCrossRef
import com.example.chronosyne.database.entities.Reminder
import com.example.chronosyne.database.entities.User
import com.example.chronosyne.database.entities.Event
import com.example.chronosyne.database.entities.Category
import com.example.chronosyne.database.entities.Converters


@Database(
    entities = [
        User::class,
        Event::class,
        Reminder::class,
        Category::class,
        EventCategoryCrossRef::class
    ],
    version = 1
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun eventDao(): EventDao
    abstract fun reminderDao(): ReminderDao
    abstract fun categoryDao(): CategoryDao
}
