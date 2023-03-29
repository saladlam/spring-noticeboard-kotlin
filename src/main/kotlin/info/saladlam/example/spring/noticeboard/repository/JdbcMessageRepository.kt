package info.saladlam.example.spring.noticeboard.repository

import info.saladlam.example.spring.noticeboard.entity.Message
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.BeanPropertyRowMapper
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.stereotype.Repository
import java.sql.Connection
import java.sql.Timestamp
import java.time.LocalDateTime
import java.util.*

@Repository
class JdbcMessageRepository : MessageRepository {

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    private fun toTimestamp(datetime: LocalDateTime?): Timestamp? {
        return if (Objects.isNull(datetime)) {
            null
        } else {
            Timestamp.valueOf(datetime)
        }
    }

    override fun findPublished(at: LocalDateTime): List<Message> {
        return jdbcTemplate.query("SELECT * FROM message WHERE approved_by IS NOT NULL AND publish_date <= ? AND (remove_date IS NULL OR remove_date > ?) ORDER BY publish_date DESC",
            BeanPropertyRowMapper.newInstance(Message::class.java), at, at
        )
    }

    override fun findWaitingApprove(): List<Message> {
        return jdbcTemplate.query("SELECT * FROM message WHERE approved_by IS NULL ORDER BY publish_date DESC",
            BeanPropertyRowMapper.newInstance(Message::class.java)
        )
    }

    override fun findByOwner(owner: String): List<Message> {
        return jdbcTemplate.query(
            "SELECT * FROM message WHERE owner = ? ORDER BY publish_date DESC",
            BeanPropertyRowMapper.newInstance(Message::class.java), owner
        )
    }

    override fun findOne(id: Long): Message? {
        return jdbcTemplate.queryForObject("SELECT * FROM message WHERE id = ?",
            BeanPropertyRowMapper.newInstance(Message::class.java), id
        )
    }

    override fun save(message: Message): Message {
        if (message.id == null) {
            val sql = "INSERT INTO message(publish_date, remove_date, owner, description, approved_by, approved_date) VALUES (?, ?, ?, ?, ?, ?)"
            val keyHolder = GeneratedKeyHolder()
            jdbcTemplate.update({ con: Connection ->
                val ps = con.prepareStatement(sql, arrayOf("id"))
                ps.setTimestamp(1, toTimestamp(message.publishDate))
                ps.setTimestamp(2, toTimestamp(message.removeDate))
                ps.setString(3, message.owner)
                ps.setString(4, message.description)
                ps.setString(5, message.approvedBy)
                ps.setTimestamp(6, toTimestamp(message.approvedDate))
                ps
            }, keyHolder)
            message.id = Objects.requireNonNull(keyHolder.key)!!.toLong()
        } else {
            val sql = "UPDATE message SET publish_date = ?, remove_date = ?, owner = ?, description = ?, approved_by = ?, approved_date = ? WHERE id = ?"
            jdbcTemplate.update(
                sql, message.publishDate, message.removeDate, message.owner,
                message.description, message.approvedBy, message.approvedDate, message.id
            )
        }

        return message
    }

}
