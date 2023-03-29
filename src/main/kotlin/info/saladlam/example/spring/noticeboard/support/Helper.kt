package info.saladlam.example.spring.noticeboard.support

import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType

class Helper {

    companion object {
        @JvmStatic
        fun getEmbeddedDatabase(name: String, vararg addScripts: String): EmbeddedDatabase {
            val db = getEmbeddedDatabaseBuilder(name)
                .setScriptEncoding("UTF-8")
                .addScript("classpath:/sql/dbschema.sql")
            for (script in addScripts) {
                db.addScript(script)
            }
            return db.build()
        }

        @JvmStatic
        fun getEmbeddedDatabaseBuilder(name: String): EmbeddedDatabaseBuilder = EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .setName(name)

    }

}
