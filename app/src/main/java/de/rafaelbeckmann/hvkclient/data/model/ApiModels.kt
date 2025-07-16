package de.rafaelbeckmann.hvkclient.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

// TODO: split into multiple files

data class LoginRequest(
    val username: String,
    val password: String
)

data class LoginResponse(
    val accessToken: String,
    val refreshToken: String
)

data class RefreshTokenRequest(
    val token: String
)

data class TokenRefreshResponse(
    val accessToken: String
)

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

data class VpSelectedCoursesResponse(
    val courses: List<String>
)

@Entity
data class VpSubstitution(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val hour: String,
    val original: String,
    val replacement: String,
    val description: String,
    val vp_date: String
)

data class VpClass(
    val today: List<VpSubstitution> = emptyList(),
    val tomorrow: List<VpSubstitution> = emptyList()
)

data class VpResponse(
    val substitutions: Map<String, VpClass>
)

// TODO: mit VpSubstitutionsAll zusammenlegen, also VpSubstitutionsAll mit diesen sachen annotieren


// TODO: ist es goofy dass das mit JSON gespeichert wird?


// TODO
@Entity(tableName = "vp_substitutions_cache")
data class VpSubstitutionsCache(
    @PrimaryKey val courseName: String,
    val vpClass: VpClass
)

class VpClassConverter {
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    private val vpClassAdapter = moshi.adapter<VpClass>(VpClass::class.java)

    @TypeConverter
    fun fromVpClass(vpClass: VpClass): String {
        return vpClassAdapter.toJson(vpClass)
    }

    @TypeConverter
    fun toVpClass(value: String): VpClass? {
        return vpClassAdapter.fromJson(value)
    }
}