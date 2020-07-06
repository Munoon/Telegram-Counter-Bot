package com.munoon.counter.user

import com.munoon.counter.configuration.InMemoryConfiguration
import javassist.NotFoundException
import org.springframework.stereotype.Component

@Component
class InMemoryUserRepository : UserRepository {
    private val storage: MutableList<User>
    private var index: Int

    constructor(inMemoryConfiguration: InMemoryConfiguration) {
        storage = inMemoryConfiguration.users.mapIndexed { index, item ->
            User(index.toString(), item.telegramChatId)
        }.toMutableList()
        index = storage.size
    }

    override fun getById(id: String): User
            = storage.findLast { it.id == id } ?: throw NotFoundException("User with id $id is not found")

    override fun getByTelegramChatId(chatId: String): User
            = storage.findLast { it.chatId == chatId }
                ?: throw NotFoundException("User with telegram chat id $chatId is not found")

    override fun updateUser(user: User): User {
        val newUser = user.copy(id = index++.toString())
        storage.add(newUser)
        return newUser
    }

    override fun findAll(): List<User> = ArrayList(storage)
    override fun count(): Int = storage.size
}