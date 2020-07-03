package com.munoon.counter.user

interface UserRepository {
    fun getById(id: String): User
    fun getByTelegramUserId(id: String): User
    fun getByTelegramChatId(chatId: String): User
    fun updateUser(user: User): User
    fun findAll(): List<User>
}