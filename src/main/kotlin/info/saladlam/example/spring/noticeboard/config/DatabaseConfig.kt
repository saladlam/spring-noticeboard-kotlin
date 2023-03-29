package info.saladlam.example.spring.noticeboard.config

import info.saladlam.example.spring.noticeboard.support.Helper.Companion.getEmbeddedDatabaseBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import javax.sql.DataSource

@Configuration
class DatabaseConfig {

    @Bean
    @Profile("!test")
    fun dataSource(): DataSource = getEmbeddedDatabaseBuilder("noticeboard").build()

}
