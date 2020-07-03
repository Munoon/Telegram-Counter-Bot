package com.munoon.counter.user.inMemory

import com.munoon.counter.user.TelegramSettings
import com.munoon.counter.user.User
import com.munoon.counter.user.UserRepository
import javassist.NotFoundException
import org.springframework.stereotype.Component

@Component
class InMemoryUserRepository : UserRepository {
    private val storage: MutableList<User>
    private var index: Int

    constructor(inMemoryConfiguration: InMemoryConfiguration) {
        storage = inMemoryConfiguration.users.mapIndexed { index, item ->
            User(index.toString(), TelegramSettings(item.telegramChatId, item.telegramUserId))
        }.toMutableList()
        index = storage.size
    }

    override fun getById(id: String): User
            = storage.findLast { it.id == id } ?: throw NotFoundException("User with id $id is not found")

    override fun getByTelegramUserId(id: String): User
            = storage.findLast { it.telegramSettings.userId == id }
                ?: throw NotFoundException("User with telegram user id $id is not found")

    override fun getByTelegramChatId(chatId: String): User
            = storage.findLast { it.telegramSettings.chatId == chatId }
                ?: throw NotFoundException("User with telegram chat id $chatId is not found")

    override fun updateUser(user: User): User {
        val newUser = user.copy(id = index++.toString())
        storage.add(newUser)
        return newUser
    }
}