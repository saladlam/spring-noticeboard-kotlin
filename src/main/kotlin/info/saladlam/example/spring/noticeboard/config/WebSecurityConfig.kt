package info.saladlam.example.spring.noticeboard.config

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.password.PasswordEncoder

@Configuration
@EnableWebSecurity
class WebSecurityConfig: WebSecurityConfigurerAdapter() {

    @Autowired
    private lateinit var userDetailsService: UserDetailsService
    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    @Throws(Exception::class)
    override fun configure(auth: AuthenticationManagerBuilder) {
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder)
    }

    @Throws(Exception::class)
    override fun configure(http: HttpSecurity) {
        http.authorizeRequests()
                // must put more restricted rule at first
                .antMatchers("/manage/*/approve").hasAuthority("ADMIN")
                .antMatchers("/manage/**").hasAuthority("USER")
            .and()
                .httpBasic().disable()
                .formLogin()
                    .loginPage("/login")
                    .loginProcessingUrl("/loginHandler")
                    .failureUrl("/login?error=true")
                    .permitAll()
            .and()
                .logout().logoutSuccessUrl("/")
    }

}
