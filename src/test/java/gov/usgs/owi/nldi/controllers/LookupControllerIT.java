package gov.usgs.owi.nldi.controllers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.co.datumedge.hamcrest.json.SameJSONAs.sameJSONArrayAs;
import static uk.co.datumedge.hamcrest.json.SameJSONAs.sameJSONObjectAs;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.github.springtestdbunit.annotation.DatabaseSetup;

import gov.usgs.owi.nldi.BaseIT;
import gov.usgs.owi.nldi.dao.LogDao;
import gov.usgs.owi.nldi.dao.LookupDao;
import gov.usgs.owi.nldi.dao.NavigationDao;
import gov.usgs.owi.nldi.dao.StreamingDao;
import gov.usgs.owi.nldi.services.ConfigurationService;
import gov.usgs.owi.nldi.services.LogService;
import gov.usgs.owi.nldi.services.Navigation;
import gov.usgs.owi.nldi.services.Parameters;
import gov.usgs.owi.nldi.springinit.DbTestConfig;
import gov.usgs.owi.nldi.springinit.SpringConfig;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@EnableWebMvc
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.MOCK,
		classes={DbTestConfig.class, SpringConfig.class, 
		LookupController.class, LookupDao.class, StreamingDao.class,
		Navigation.class, NavigationDao.class, Parameters.class, 
		ConfigurationService.class, LogService.class, LogDao.class})
@DatabaseSetup("classpath:/testData/crawlerSource.xml")
public class LookupControllerIT extends BaseIT {

	@Autowired
	private WebApplicationContext wac;

	private MockMvc mockMvc;

	private static final String RESULT_FOLDER  = "lookup/";

	@Before
	public void setup() {
		mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
	}

	//DataSources Testing
	@Test
	public void getDataSourcesTest() throws Exception {
		MvcResult rtn = mockMvc.perform(get("/api"))
				.andExpect(status().isOk())
				.andExpect(header().string(NetworkController.HEADER_CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE))
				.andReturn();

		assertThat(new JSONArray(rtn.getResponse().getContentAsString()),
				sameJSONArrayAs(new JSONArray(getCompareFile(RESULT_FOLDER, "dataSources.json"))).allowingAnyArrayOrdering());
	}

	//Features Testing
	@Test
	public void getFeaturesTest() throws Exception {
		MvcResult rtn = mockMvc.perform(get("/api/comid"))
				.andExpect(status().isBadRequest())
				.andReturn();

		assertEquals("This functionality is not implemented.", rtn.getResponse().getErrorMessage());
	}

	//Object Testing Catchment
	@Test
	public void getComidTest() throws Exception {
		MvcResult rtn = mockMvc.perform(get("/api/comid/13297246"))
				.andExpect(status().isOk())
				.andExpect(header().string(NetworkController.HEADER_CONTENT_TYPE, NetworkController.MIME_TYPE_GEOJSON))
				.andReturn();

		assertThat(new JSONObject(rtn.getResponse().getContentAsString()),
				sameJSONObjectAs(new JSONObject(getCompareFile(RESULT_FOLDER, "comid_13297246.json"))).allowingAnyArrayOrdering());
	}

	//Linked Object Testing WQP
	@Test
	@DatabaseSetup("classpath:/testData/featureWqp.xml")
	public void getWqpTest() throws Exception {
		MvcResult rtn = mockMvc.perform(get("/api/wqp/USGS-05427880"))
				.andExpect(status().isOk())
				.andExpect(header().string(NetworkController.HEADER_CONTENT_TYPE, NetworkController.MIME_TYPE_GEOJSON))
				.andReturn();

		assertThat(new JSONObject(rtn.getResponse().getContentAsString()),
				sameJSONObjectAs(new JSONObject(getCompareFile(RESULT_FOLDER_WQP, "wqp_USGS-05427880.json"))).allowingAnyArrayOrdering());
	}

	//Linked Object Testing huc12pp
	@Test
	@DatabaseSetup("classpath:/testData/featureHuc12pp.xml")
	public void gethuc12ppTest() throws Exception {
		MvcResult rtn = mockMvc.perform(get("/api/huc12pp/070900020604"))
				.andExpect(status().isOk())
				.andExpect(header().string(NetworkController.HEADER_CONTENT_TYPE, NetworkController.MIME_TYPE_GEOJSON))
				.andReturn();

		assertThat(new JSONObject(rtn.getResponse().getContentAsString()),
				sameJSONObjectAs(new JSONObject(getCompareFile(RESULT_FOLDER_HUC, "huc12pp_070900020604.json"))).allowingAnyArrayOrdering());
	}

	//Navigation Types Testing
	@Test
	@DatabaseSetup("classpath:/testData/featureWqp.xml")
	public void getNavigationTypesTest() throws Exception {
		MvcResult rtn = mockMvc.perform(get("/api/wqp/USGS-05427880/navigate"))
				.andExpect(status().isOk())
				.andExpect(header().string(NetworkController.HEADER_CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE))
				.andReturn();

		assertThat(new JSONObject(rtn.getResponse().getContentAsString()),
				sameJSONObjectAs(new JSONObject(getCompareFile(RESULT_FOLDER, "wqp_USGS-05427880.json"))).allowingAnyArrayOrdering());
	}

	@Test
	public void getNavigationTypesNotFoundTest() throws Exception {
		mockMvc.perform(get("/api/wqx/USGS-05427880/navigate"))
				.andExpect(status().isNotFound())
				.andExpect(header().string(NetworkController.HEADER_CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE));

		mockMvc.perform(get("/api/wqp/USGX-05427880/navigate"))
				.andExpect(status().isNotFound())
				.andExpect(header().string(NetworkController.HEADER_CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE))
				.andReturn();
	}

}
