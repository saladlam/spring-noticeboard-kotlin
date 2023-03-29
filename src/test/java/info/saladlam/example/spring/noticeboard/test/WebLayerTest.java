package info.saladlam.example.spring.noticeboard.test;

import info.saladlam.example.spring.noticeboard.service.ApplicationDateTimeService;
import info.saladlam.example.spring.noticeboard.support.Helper;
import info.saladlam.example.spring.noticeboard.support.WithMockCustomUser;
import liquibase.integration.spring.SpringLiquibase;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import javax.sql.DataSource;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class WebLayerTest {

	public static class TestApplicationDateTimeService implements ApplicationDateTimeService {

		private LocalDateTime current = LocalDateTime.of(2022, 9, 1, 9, 0);

		@Override
		public LocalDateTime getCurrentLocalDateTime() {
			return current;
		}

		public void setCurrent(LocalDateTime current) {
			this.current = current;
		}

	}

	@TestConfiguration
	public static class WebLayerTestContextConfiguration {

		@Bean
		public DataSource testDataSource() {
			return Helper.getEmbeddedDatabaseBuilder(WebLayerTest.class.getName()).build();
		}

		@Bean
		public SpringLiquibase liquibase(DataSource dataSource) {
			SpringLiquibase liquibase = new SpringLiquibase();
			liquibase.setChangeLog("classpath:db/schema.xml");
			liquibase.setDataSource(dataSource);
			liquibase.setDropFirst(true);
			return liquibase;
		}

		@Bean
		@Primary
		public ApplicationDateTimeService dateTimeService() {
			return new TestApplicationDateTimeService();
		}

	}

	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private TestApplicationDateTimeService timeService;

	private Document getDocument(RequestBuilder requestBuilder) throws Exception {
		MvcResult result = mockMvc.perform(requestBuilder)
				.andExpect(status().isOk())
				.andReturn();
		return Jsoup.parse(result.getResponse().getContentAsString());
	}

	private void sendData(MockHttpServletRequestBuilder requestBuilder, String publishDate, String removeDate, String description) throws Exception {
		mockMvc.perform(requestBuilder.with(csrf())
				.param("publishDate", publishDate)
				.param("removeDate", removeDate)
				.param("description", description)
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)).andExpect(status().is3xxRedirection());
	}

	private void approve(int id, ResultMatcher matcher) throws Exception {
		mockMvc.perform(get("/manage/{id}/approve", id)).andExpect(matcher);
	}

	private void testGuestView(Document doc) {
		assertThat(doc.select("#buLogin")).hasSize(1);
		assertThat(doc.select("#buManage")).isEmpty();
	}

	private void testUserView(Document doc) {
		assertThat(doc.select("#buLogin")).isEmpty();
		assertThat(doc.select("#buManage")).hasSize(1);
	}

	private void checkPublicMessageNumber(Document doc, int except) {
		assertThat(doc.select("#taPublish > tr")).hasSize(except);
	}

	@Test
	@Order(1010)
	void guestViewMain1() throws Exception {
		Document doc = getDocument(get("/"));
		testGuestView(doc);
		checkPublicMessageNumber(doc, 0);
	}

	@Test
	@Order(1020)
	@WithMockCustomUser(username = "user1", authorities = {"USER"}, name = "First Last")
	void userAction1() throws Exception {
		timeService.setCurrent(timeService.getCurrentLocalDateTime().plusHours(1));
		Document doc = getDocument(get("/"));
		testUserView(doc);
		checkPublicMessageNumber(doc, 0);

		sendData(post("/manage/new/save"), "01-09-2022 10:00", "03-09-2022 17:00", "user1 message 1");
		doc = getDocument(get("/"));
		checkPublicMessageNumber(doc, 0);
		doc = getDocument(get("/manage"));
		assertThat(doc.select("#txUser").text())
				.contains("(user1)")
				.contains("First Last");
		assertThat(doc.select("#taMy > tr")).hasSize(1 + 1);
		assertThat(doc.select("#my1 .myPublishDate").text()).isEqualTo("01-09-2022 10:00");
		assertThat(doc.select("#my1 .myRemoveDate").text()).isEqualTo("03-09-2022 17:00");
		assertThat(doc.select("#my1 .myStatus").text()).isEqualTo("Waiting Approve");
		assertThat(doc.select("#my1 .myDescription").text()).isEqualTo("user1 message 1");
		doc = getDocument(get("/manage/1"));
		assertThat(doc.select("#edPublishDate").val()).isEqualTo("01-09-2022 10:00");
		assertThat(doc.select("#edRemoveDate").val()).isEqualTo("03-09-2022 17:00");
		assertThat(doc.select("#edDescription").val()).isEqualTo("user1 message 1");
	}

	@Test
	@Order(1030)
	@WithMockCustomUser(username = "admin1", authorities = {"USER", "ADMIN"}, name = "Admin User 1")
	void adminAction1() throws Exception {
		timeService.setCurrent(timeService.getCurrentLocalDateTime().plusHours(1));
		Document doc = getDocument(get("/"));
		testUserView(doc);
		checkPublicMessageNumber(doc, 0);

		sendData(post("/manage/new/save"), "01-09-2022 11:30", null, "admin1 message 2");
		doc = getDocument(get("/"));
		checkPublicMessageNumber(doc, 0);
		doc = getDocument(get("/manage"));
		assertThat(doc.select("#txUser").text())
				.contains("(admin1)")
				.contains("Admin User 1");
		assertThat(doc.select("#taMy > tr")).hasSize(2);
		assertThat(doc.select("#my2 .myPublishDate").text()).isEqualTo("01-09-2022 11:30");
		assertThat(doc.select("#my2 .myRemoveDate").text()).isEqualTo("Not set");
		assertThat(doc.select("#my2 .myStatus").text()).isEqualTo("Waiting Approve");
		assertThat(doc.select("#my2 .myDescription").text()).isEqualTo("admin1 message 2");

		assertThat(doc.select("#app1 .appOwner").text()).isEqualTo("user1");
		assertThat(doc.select("#app1 .appPublishDate").text()).isEqualTo("01-09-2022 10:00");
		assertThat(doc.select("#app1 .appRemoveDate").text()).isEqualTo("03-09-2022 17:00");
		assertThat(doc.select("#app1 .appDescription").text()).isEqualTo("user1 message 1");
		assertThat(doc.select("#app2 .appOwner").text()).isEqualTo("admin1");
		assertThat(doc.select("#app2 .appPublishDate").text()).isEqualTo("01-09-2022 11:30");
		assertThat(doc.select("#app2 .appRemoveDate").text()).isEqualTo("Not set");
		assertThat(doc.select("#app2 .appDescription").text()).isEqualTo("admin1 message 2");

		approve(1, status().is3xxRedirection());
		doc = getDocument(get("/"));
		checkPublicMessageNumber(doc, 1);
		assertThat(doc.select("#pub1 .pubPublishDate").text()).isEqualTo("01-09-2022 10:00");
		assertThat(doc.select("#pub1 .pubDescription").text()).isEqualTo("user1 message 1");
		approve(2, status().is3xxRedirection());
		doc = getDocument(get("/"));
		checkPublicMessageNumber(doc, 1);

		timeService.setCurrent(timeService.getCurrentLocalDateTime().plusMinutes(31));
		doc = getDocument(get("/"));
		checkPublicMessageNumber(doc, 2);
		assertThat(doc.select("#pub2 .pubPublishDate").text()).isEqualTo("01-09-2022 11:30");
		assertThat(doc.select("#pub2 .pubDescription").text()).isEqualTo("admin1 message 2");
	}

	@Test
	@Order(1040)
	@WithMockCustomUser(username = "user2", authorities = {"USER"}, name = "Alan Bush")
	void userAction2() throws Exception {
		timeService.setCurrent(timeService.getCurrentLocalDateTime().plusHours(1));
		Document doc = getDocument(get("/manage"));
		assertThat(doc.select("#txUser").text())
				.contains("(user2)")
				.contains("Alan Bush");
		assertThat(doc.select("#taMy > tr")).hasSize(0 + 1);
	}

	@Test
	@Order(1050)
	void guestViewMain2() throws Exception {
		timeService.setCurrent(LocalDateTime.of(2022, 9, 3, 17, 1));
		Document doc = getDocument(get("/"));
		checkPublicMessageNumber(doc, 1);
		assertThat(doc.select("#pub2 .pubPublishDate").text()).isEqualTo("01-09-2022 11:30");
		assertThat(doc.select("#pub2 .pubDescription").text()).isEqualTo("admin1 message 2");
	}

	@Test
	@Order(1060)
	@WithMockCustomUser(username = "user3", authorities = {"USER"}, name = "Teresa Mike")
	void userAction3() throws Exception {
		timeService.setCurrent(timeService.getCurrentLocalDateTime().plusHours(1));
		sendData(post("/manage/new/save"), "10-09-2022 10:00", null, "user3 message 3");
		timeService.setCurrent(timeService.getCurrentLocalDateTime().plusHours(1));
		sendData(post("/manage/3/save"), "06-09-2022 10:00", "06-09-2022 17:00", "user3 message 3a");

		Document doc = getDocument(get("/manage/3"));
		assertThat(doc.select("#edPublishDate").val()).isEqualTo("06-09-2022 10:00");
		assertThat(doc.select("#edRemoveDate").val()).isEqualTo("06-09-2022 17:00");
		assertThat(doc.select("#edDescription").val()).isEqualTo("user3 message 3a");
	}

}
