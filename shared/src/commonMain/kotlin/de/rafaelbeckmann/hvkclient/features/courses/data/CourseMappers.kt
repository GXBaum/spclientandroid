package de.rafaelbeckmann.hvkclient.features.courses.data

import de.rafaelbeckmann.hvkclient.features.courses.domain.UserCourse
import de.rafaelbeckmann.hvkclient.features.courses.domain.UserMark

fun NetworkUserMark.toEntity(): UserMarkEntity {
    return UserMarkEntity(
        id = this.id,
        name = this.name,
        date = this.date,
        grade = this.grade,
        course_id = this.courseId,
        half_year = this.halfYear,
        isDeleted = this.isDeleted
    )
}

fun UserMarkEntity.toDomain(): UserMark {
    return UserMark(
        id = this.id,
        name = this.name,
        date = this.date,
        grade = this.grade,
        courseId = this.course_id,
        halfYear = this.half_year,
        isDeleted = this.isDeleted
    )
}

fun NetworkUserCourse.toEntity(): UserCourseEntity {
    return UserCourseEntity(
        courseId = this.id,
        name = this.name
    )
}

fun UserCourseEntity.toDomain(): UserCourse {
    return UserCourse(
        id = this.courseId,
        name = this.name
    )
}