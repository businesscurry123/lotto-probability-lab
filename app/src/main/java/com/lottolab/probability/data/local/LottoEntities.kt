package com.lottolab.probability.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.lottolab.probability.domain.DailyCombination
import com.lottolab.probability.domain.DrawResult
import com.lottolab.probability.domain.SavedNumberSet

@Entity(tableName = "draw_results")
data class DrawResultEntity(
    @PrimaryKey val round: Int,
    val drawDate: String,
    val number1: Int,
    val number2: Int,
    val number3: Int,
    val number4: Int,
    val number5: Int,
    val number6: Int,
    val bonusNumber: Int,
)

@Entity(tableName = "saved_number_sets")
data class SavedNumberSetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val number1: Int,
    val number2: Int,
    val number3: Int,
    val number4: Int,
    val number5: Int,
    val number6: Int,
    val createdAt: Long,
    val updatedAt: Long,
    val favorite: Boolean = false,
    val collectionName: String = "기본",
)

@Entity(tableName = "daily_combinations", primaryKeys = ["date", "type"])
data class DailyCombinationEntity(
    val date: String,
    val type: String,
    val number1: Int,
    val number2: Int,
    val number3: Int,
    val number4: Int,
    val number5: Int,
    val number6: Int,
    val createdAt: Long,
)

fun DrawResultEntity.toDomain(): DrawResult = DrawResult(
    round = round,
    drawDate = drawDate,
    mainNumbers = listOf(number1, number2, number3, number4, number5, number6),
    bonusNumber = bonusNumber,
)

fun DrawResult.toEntity(): DrawResultEntity {
    val sortedNumbers = mainNumbers.sorted()
    return DrawResultEntity(
        round = round,
        drawDate = drawDate,
        number1 = sortedNumbers[0],
        number2 = sortedNumbers[1],
        number3 = sortedNumbers[2],
        number4 = sortedNumbers[3],
        number5 = sortedNumbers[4],
        number6 = sortedNumbers[5],
        bonusNumber = bonusNumber,
    )
}

fun SavedNumberSetEntity.toDomain(): SavedNumberSet = SavedNumberSet(
    id = id,
    name = name,
    numbers = listOf(number1, number2, number3, number4, number5, number6),
    createdAt = createdAt,
    updatedAt = updatedAt,
    favorite = favorite,
    collectionName = collectionName,
)

fun SavedNumberSet.toEntity(): SavedNumberSetEntity {
    val sortedNumbers = numbers.sorted()
    return SavedNumberSetEntity(
        id = id,
        name = name,
        number1 = sortedNumbers[0],
        number2 = sortedNumbers[1],
        number3 = sortedNumbers[2],
        number4 = sortedNumbers[3],
        number5 = sortedNumbers[4],
        number6 = sortedNumbers[5],
        createdAt = createdAt,
        updatedAt = updatedAt,
        favorite = favorite,
        collectionName = collectionName,
    )
}

fun DailyCombinationEntity.toDomain(): DailyCombination = DailyCombination(
    date = date,
    type = type,
    numbers = listOf(number1, number2, number3, number4, number5, number6),
    createdAt = createdAt,
)

fun DailyCombination.toEntity(): DailyCombinationEntity {
    val sortedNumbers = numbers.sorted()
    return DailyCombinationEntity(
        date = date,
        type = type,
        number1 = sortedNumbers[0],
        number2 = sortedNumbers[1],
        number3 = sortedNumbers[2],
        number4 = sortedNumbers[3],
        number5 = sortedNumbers[4],
        number6 = sortedNumbers[5],
        createdAt = createdAt,
    )
}
