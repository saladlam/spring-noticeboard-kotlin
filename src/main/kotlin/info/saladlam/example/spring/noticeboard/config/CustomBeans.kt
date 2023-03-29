package info.saladlam.example.spring.noticeboard.config

import info.saladlam.example.spring.noticeboard.repository.JdbcUserRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder

@Configuration
class CustomBeans {

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun userRepository(jdbcTemplate: JdbcTemplate): JdbcUserRepository {
        val repository = JdbcUserRepository();
        repository.jdbcTemplate = jdbcTemplate
        return repository
    }

}
