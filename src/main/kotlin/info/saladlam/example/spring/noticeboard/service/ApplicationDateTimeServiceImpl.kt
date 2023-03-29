package info.saladlam.example.spring.noticeboard.service

import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class ApplicationDateTimeServiceImpl : ApplicationDateTimeService {

    override fun getCurrentLocalDateTime(): LocalDateTime = LocalDateTime.now();

}
