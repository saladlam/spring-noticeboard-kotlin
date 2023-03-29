package info.saladlam.example.spring.noticeboard.dto

import org.springframework.validation.Errors
import org.springframework.validation.ValidationUtils
import org.springframework.validation.Validator

class MessageDtoValidator : Validator {

    override fun supports(clazz: Class<*>): Boolean = MessageDto::class.java.isAssignableFrom(clazz);

    override fun validate(target: Any, errors: Errors) {
        ValidationUtils.rejectIfEmpty(errors, "publishDate", "publishDate.empty")
        ValidationUtils.rejectIfEmpty(errors, "description", "description.empty")
    }

}
