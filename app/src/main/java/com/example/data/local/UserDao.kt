package com.example.data.local

import androidx.room.*
import com.example.data.model.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: String): UserEntity?

    @Query("SELECT * FROM users WHERE role = 'USER'")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users")
    suspend fun getAllUsersList(): List<UserEntity>

    @Query("SELECT COUNT(*) FROM users WHERE role = 'USER'")
    fun getUsersCount(): Flow<Int>

    @Query("SELECT SUM(walletBalance) FROM users WHERE role = 'USER'")
    fun getTotalCirculation(): Flow<Double?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("UPDATE users SET betProUsername = :username, betProPassword = :password, betProStatus = :status WHERE id = :userId")
    suspend fun updateBetProCredentials(userId: String, username: String, password: String, status: String)

    @Query("UPDATE users SET walletBalance = walletBalance + :amount WHERE id = :userId")
    suspend fun addWalletBalance(userId: String, amount: Double)

    @Query("UPDATE users SET walletBalance = :balance WHERE id = :userId")
    suspend fun setWalletBalance(userId: String, balance: Double)

    @Query("UPDATE users SET isApproved = :approved, isBlocked = :blocked WHERE id = :userId")
    suspend fun updateUserStatus(userId: String, approved: Boolean, blocked: Boolean)

    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUser(userId: String)
}
