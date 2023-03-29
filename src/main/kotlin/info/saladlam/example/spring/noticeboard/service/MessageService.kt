package info.saladlam.example.spring.noticeboard.service

import info.saladlam.example.spring.noticeboard.dto.MessageDto
import java.time.LocalDateTime

interface MessageService {

    fun findOne(id: Long, time: LocalDateTime): MessageDto?

    fun findPublished(time: LocalDateTime): List<MessageDto>

    fun findWaitingApprove(): List<MessageDto>

    fun findByOwner(owner: String, time: LocalDateTime): List<MessageDto>

    fun save(messageDto: MessageDto)

    fun approve(id: Long, approvedBy: String, time: LocalDateTime)

}
