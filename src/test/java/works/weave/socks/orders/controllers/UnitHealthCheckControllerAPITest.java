//package works.weave.socks.orders.controllers;
//
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.mockito.InjectMocks;
//import org.mockito.MockitoAnnotations;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.context.ApplicationContext;
//import org.springframework.test.context.junit4.SpringRunner;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
//import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
//import org.springframework.test.web.servlet.setup.MockMvcBuilders;
//import works.weave.socks.orders.OrderApplication;
//import works.weave.socks.orders.controllers.HealthCheckController;
//import org.springframework.boot.test.context.SpringBootTest;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
//
//@RunWith(SpringRunner.class)
////@WebMvcTest(controllers = HealthCheckController.class)
//@SpringBootTest(classes = OrderApplication.class)
//public class UnitHealthCheckControllerAPITest {
//
//    @Autowired
//    private HealthCheckController healthCheckController;
//
//    private MockMvc mockMvc;
//
//    @Before
//    public void setUp() {
//        MockitoAnnotations.initMocks(this);
//        mockMvc = MockMvcBuilders.standaloneSetup(healthCheckController).build();
//    }
//
//    @Test
//    public void contextLoads() throws Exception {
//        assertThat(healthCheckController).isNotNull();
//    }
//
//    @Test
//    public void testGetHealth() throws Exception {
//        mockMvc.perform(MockMvcRequestBuilders.get("/health"))
//                .andDo(print())
//                .andExpect(MockMvcResultMatchers.status().isOk())
//                .andExpect(MockMvcResultMatchers.jsonPath("$.health[0].status").value("OK"));
//    }
//}
