package info.saladlam.example.spring.noticeboard.service

import info.saladlam.example.spring.noticeboard.dto.MessageDto
import info.saladlam.example.spring.noticeboard.entity.Message
import info.saladlam.example.spring.noticeboard.repository.MessageRepository
import org.dozer.Mapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*
import java.util.stream.Collectors

@Service
@Transactional
class MessageServiceImpl : MessageService {

    @Autowired
    private lateinit var mapper: Mapper

    @Autowired
    private lateinit var messageRepository: MessageRepository

    override fun findOne(id: Long, time: LocalDateTime): MessageDto? {
        val to = mapper.map(messageRepository.findOne(id), MessageDto::class.java)
        this.updateStatus(to, time)
        return to
    }

    override fun findPublished(time: LocalDateTime): List<MessageDto> {
        val publishedMessage = messageRepository.findPublished(time)
        return publishedMessage.stream().map { from: Message ->
            val to = mapper.map(from, MessageDto::class.java)
            to.status = MessageDto.PUBLISHED
            to
        }.collect(Collectors.toList())
    }

    override fun findWaitingApprove(): List<MessageDto> {
        val publishedMessage = messageRepository.findWaitingApprove()
        return publishedMessage.stream().map { from: Message ->
            val to = mapper.map(from, MessageDto::class.java)
            to.status = MessageDto.WAITING_APPROVE
            to
        }.collect(Collectors.toList())
    }

    override fun findByOwner(owner: String, time: LocalDateTime): List<MessageDto> {
        val publishedMessage = messageRepository.findByOwner(owner)
        return publishedMessage.stream().map { from: Message ->
            val to = mapper.map(from, MessageDto::class.java)
            updateStatus(to, time)
            to
        }.collect(Collectors.toList())
    }

    override fun save(messageDto: MessageDto) {
        if (Objects.nonNull(messageDto.removeDate) && messageDto.publishDate!!.isAfter(messageDto.removeDate)) {
            return
        }

        messageRepository.save(mapper.map(messageDto, Message::class.java))
    }

    override fun approve(id: Long, approvedBy: String, time: LocalDateTime) {
        val message = messageRepository.findOne(id)
        if (Objects.nonNull(message) && Objects.isNull(message!!.approvedBy)) {
            message.approvedBy = approvedBy
            message.approvedDate = time
            messageRepository.save(message)
        }
    }

    private fun updateStatus(message: MessageDto, currentTime: LocalDateTime) {
        if (Objects.isNull(message.approvedBy)) {
            message.status = MessageDto.WAITING_APPROVE
        } else {
            if (message.publishDate.isBefore(currentTime)) {
                message.status = MessageDto.PUBLISHED
            } else {
                message.status = MessageDto.APPROVED
            }
            if (Objects.nonNull(message.removeDate) && message.removeDate.isBefore(currentTime)) {
                message.status = MessageDto.EXPIRED
            }
        }
    }

}
