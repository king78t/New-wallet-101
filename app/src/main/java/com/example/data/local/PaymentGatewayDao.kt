package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.PaymentGatewayEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentGatewayDao {
    @Query("SELECT * FROM payment_gateways WHERE isEnabled = 1")
    fun getAllGateways(): Flow<List<PaymentGatewayEntity>>

    @Query("SELECT * FROM payment_gateways WHERE currency = :currency AND isEnabled = 1")
    fun getGatewaysByCurrency(currency: String): Flow<List<PaymentGatewayEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGateway(gateway: PaymentGatewayEntity)

    @Delete
    suspend fun deleteGateway(gateway: PaymentGatewayEntity)
}
