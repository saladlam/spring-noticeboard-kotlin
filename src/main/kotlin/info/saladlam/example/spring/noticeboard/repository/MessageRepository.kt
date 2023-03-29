package info.saladlam.example.spring.noticeboard.repository

import info.saladlam.example.spring.noticeboard.entity.Message
import java.time.LocalDateTime

interface MessageRepository {

    fun findPublished(at: LocalDateTime): List<Message>

    fun findWaitingApprove(): List<Message>

    fun findByOwner(owner: String): List<Message>

    fun findOne(id: Long): Message?

    fun save(message: Message): Message

}
