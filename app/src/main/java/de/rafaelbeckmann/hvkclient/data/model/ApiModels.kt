package de.rafaelbeckmann.hvkclient.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

// TODO: split into multiple files

data class TokenUpdateRequest(
    val token: String,
    val spUsername: String
)

data class UserCourses(
    val courses: List<UserCourse>
)
@Entity
data class UserCourse(
    @PrimaryKey val courseId: Int,
    val name: String,
)

data class UserMarks(
    val marks: List<UserMark>
)
@Entity
data class UserMark(
    @PrimaryKey val mark_id: Int,
    val name: String,
    val date: String,
    val grade: String,
    val course_id: Int,
    val sp_username: String,
    val half_year: Int
)


@Entity
data class VpSelectedCourse(
    @PrimaryKey val courseName: String
)

data class VpSubstitutions(
    val substitutions: List<VpSubstitution>
)

@Entity
data class VpSubstitution(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val hour: String,
    val original: String,
    val replacement: String,
    val description: String
)


data class VpSubstitutionsAll(
    val substitutions: List<List<VpSubstitution>>
)

// TODO: mit VpSubstitutionsAll zusammenlegen, also VpSubstitutionsAll mit diesen sachen anotieren

@Entity(tableName = "vp_substitutions_all")
@TypeConverters(VpSubstitutionsAllConverter::class)
data class VpSubstitutionsAllCache(
    @PrimaryKey val courseName: String,
    val substitutions: List<List<VpSubstitution>>
)

// TODO: ist es goofy dass das mit JSON gespeichert wird?
class VpSubstitutionsAllConverter {
    private val moshi = Moshi.Builder().build()
    private val listMyData = Types.newParameterizedType(List::class.java, VpSubstitution::class.java)
    private val listListMyData = Types.newParameterizedType(List::class.java, listMyData)
    private val jsonAdapter = moshi.adapter<List<List<VpSubstitution>>>(listListMyData)

    @TypeConverter
    fun fromString(value: String): List<List<VpSubstitution>>? {
        return jsonAdapter.fromJson(value)
    }

    @TypeConverter
    fun fromList(list: List<List<VpSubstitution>>): String {
        return jsonAdapter.toJson(list)
    }
}