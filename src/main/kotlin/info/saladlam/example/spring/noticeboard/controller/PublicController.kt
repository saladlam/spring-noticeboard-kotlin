package info.saladlam.example.spring.noticeboard.controller

import info.saladlam.example.spring.noticeboard.service.ApplicationDateTimeService
import info.saladlam.example.spring.noticeboard.service.MessageService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import java.time.LocalDateTime
import java.util.*

@Controller
@RequestMapping("/")
class PublicController {

    @Autowired
    private lateinit var messageService: MessageService

    @Autowired
    private lateinit var timeService: ApplicationDateTimeService

    @GetMapping
    fun index(model: Model): String {
        model.addAttribute("messages", messageService.findPublished(getCurrentLocalDateTime()))
        return "public/index"
    }

    @GetMapping("/login")
    fun login(error: Boolean, model: Model): String {
        model.addAttribute("error", Objects.nonNull(error) && error == java.lang.Boolean.TRUE)
        return "public/login"
    }

    private fun getCurrentLocalDateTime(): LocalDateTime = timeService.getCurrentLocalDateTime()

}
