package info.saladlam.example.spring.noticeboard.service

import java.time.LocalDateTime

interface ApplicationDateTimeService {

    fun getCurrentLocalDateTime(): LocalDateTime

}
