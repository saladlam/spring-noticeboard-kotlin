package info.saladlam.example.spring.noticeboard

import org.dozer.DozerBeanMapper
import org.dozer.Mapper
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean

@SpringBootApplication
class NoticeboardApplication {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            SpringApplication.run(NoticeboardApplication::class.java, *args)
        }
    }

    @Bean
    fun mapper(): Mapper {
        val mappingFiles: MutableList<String> = ArrayList()
        mappingFiles.add("dozerJdk8Converters.xml")

        val dozerBeanMapper = DozerBeanMapper()
        dozerBeanMapper.mappingFiles = mappingFiles
        return dozerBeanMapper
    }

}
