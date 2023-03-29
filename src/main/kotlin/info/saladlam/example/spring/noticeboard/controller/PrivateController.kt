package info.saladlam.example.spring.noticeboard.controller

import info.saladlam.example.spring.noticeboard.dto.MessageDto
import info.saladlam.example.spring.noticeboard.dto.MessageDtoValidator
import info.saladlam.example.spring.noticeboard.service.ApplicationDateTimeService
import info.saladlam.example.spring.noticeboard.service.MessageService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.WebDataBinder
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDateTime
import java.util.*
import java.util.stream.Collectors
import javax.validation.Valid

@Controller
@RequestMapping("/manage")
class PrivateController {

    @Autowired
    private lateinit var messageService: MessageService

    @Autowired
    private lateinit var timeService: ApplicationDateTimeService


    @InitBinder
    fun initBinder(binder: WebDataBinder) {
        binder.setDisallowedFields("id", "owner", "approvedBy", "approvedDate", "status")
        binder.validator = MessageDtoValidator()
    }

    @GetMapping
    fun manage(model: Model): String {
        model.addAttribute("statusMap", statusMap)
        model.addAttribute("userMessages", messageService.findByOwner(getLoginName(), getCurrentLocalDateTime()))
        if (getLoginAuthorities().contains("ADMIN")) {
            model.addAttribute("waitingApproveMessages", messageService.findWaitingApprove())
        }
        return "private/manage"
    }

    @GetMapping("/new")
    fun createMessage(model: Model): String {
        model.addAttribute("isEdit", false)
        model.addAttribute("postHandler", "new/save")
        model.addAttribute("message", MessageDto())
        return "private/message"
    }

    @PostMapping("/new/save")
    fun saveCreateMessage(@ModelAttribute message: @Valid MessageDto, errors: BindingResult): String {
        if (errors.hasErrors()) {
            return "redirect:/manage/new"
        }
        message.owner = getLoginName()
        messageService.save(message)
        return REDIRECT_MANAGE
    }

    @GetMapping("/{messageId}")
    fun editMessage(@PathVariable("messageId") id: Long, model: Model): String {
        val message = messageService.findOne(id, getCurrentLocalDateTime())
        if (Objects.nonNull(message) && (message!!.status!! == MessageDto.WAITING_APPROVE) && (message.owner == getLoginName())) {
            model.addAttribute("isEdit", true)
            model.addAttribute("postHandler", "$id/save")
            model.addAttribute("message", message)
        } else {
            throw ResponseStatusException(HttpStatus.FORBIDDEN)
        }
        return "private/message"
    }

    @PostMapping("/{messageId}/save")
    fun saveEditMessage(@PathVariable("messageId") id: Long, @ModelAttribute message: @Valid MessageDto, errors: BindingResult): String {
        if (errors.hasErrors()) {
            return "redirect:/manage/$id"
        }
        val originalMessage = messageService.findOne(id, getCurrentLocalDateTime())
        if (Objects.nonNull(originalMessage) && (originalMessage!!.status!! == MessageDto.WAITING_APPROVE) && (originalMessage.owner == getLoginName())) {
            originalMessage.publishDate = message.publishDate
            originalMessage.removeDate = message.removeDate
            originalMessage.description = message.description
            messageService.save(originalMessage)
        } else {
            throw ResponseStatusException(HttpStatus.FORBIDDEN)
        }
        return REDIRECT_MANAGE
    }

    @GetMapping("/{messageId}/approve")
    fun approve(@PathVariable("messageId") id: Long): String {
        if (getLoginAuthorities().contains("ADMIN")) {
            messageService.approve(id, getLoginName(), getCurrentLocalDateTime())
        } else {
            throw ResponseStatusException(HttpStatus.FORBIDDEN)
        }
        return REDIRECT_MANAGE
    }

    private fun getLoginName(): String = SecurityContextHolder.getContext().authentication.name

    private fun getLoginAuthorities(): List<String> = SecurityContextHolder.getContext().authentication.authorities.stream().map { obj: GrantedAuthority -> obj.authority }.collect(Collectors.toList())

    private fun getCurrentLocalDateTime(): LocalDateTime = timeService.getCurrentLocalDateTime()

    companion object {
        private const val REDIRECT_MANAGE = "redirect:/manage"
        private val statusMap: MutableMap<Int?, String> = HashMap()

        init {
            statusMap[null] = "status.unknown";
            statusMap[MessageDto.WAITING_APPROVE] = "status.waitingApprove";
            statusMap[MessageDto.APPROVED] = "status.approved";
            statusMap[MessageDto.PUBLISHED] = "status.published";
            statusMap[MessageDto.EXPIRED] = "status.expired";
        }
    }

}
