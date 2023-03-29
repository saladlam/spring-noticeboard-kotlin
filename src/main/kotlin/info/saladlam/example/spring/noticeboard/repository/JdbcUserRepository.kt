package info.saladlam.example.spring.noticeboard.repository

import info.saladlam.example.spring.noticeboard.entity.CustomUser
import org.slf4j.LoggerFactory
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.core.userdetails.jdbc.JdbcDaoImpl

open class JdbcUserRepository : JdbcDaoImpl(), UserRepository, UserDetailsService {

    @Throws(UsernameNotFoundException::class)
    override fun loadUserByUsername(username: String?): UserDetails {
        return fillUserDetail(super.loadUserByUsername(username))
    }

    protected fun fillUserDetail(user: UserDetails): UserDetails {
        val map = jdbcTemplate!!.queryForMap(JdbcUserRepository.DEF_USERS_DETAIL_BY_USERNAME_QUERY, user.username)
        return CustomUser(
            user.username,
            user.password,
            user.authorities,
            map["name"] as String?,
            map["email"] as String?
        )
    }

    companion object {
        const val DEF_USERS_DETAIL_BY_USERNAME_QUERY: String = ("select name, email "
                + "from users "
                + "where username = ? "
                + "limit 1")
        val logger = LoggerFactory.getLogger(JdbcUserRepository::class.java)
    }

}
