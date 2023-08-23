//package works.weave.socks.orders.controllers;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.mockito.ArgumentMatchers.any;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.mockito.Mockito;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.context.annotation.Import;
//import org.springframework.core.ParameterizedTypeReference;
//import org.springframework.hateoas.Resource;
//import org.springframework.hateoas.mvc.TypeReferences;
//import org.springframework.http.MediaType;
//import org.springframework.scheduling.annotation.AsyncResult;
//import org.springframework.test.context.junit4.SpringRunner;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
//import works.weave.socks.orders.config.OrdersConfiguration;
//import works.weave.socks.orders.entities.*;
//import works.weave.socks.orders.repositories.CustomerOrderRepository;
//import works.weave.socks.orders.resources.NewOrderResource;
//import works.weave.socks.orders.services.AsyncGetService;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
//
//import java.net.URI;
//import java.util.ArrayList;
//import java.util.List;
//
//@RunWith(SpringRunner.class)
//@WebMvcTest(controllers = OrdersController.class)
//@Import(value = {OrdersConfiguration.class})
//public class UnitOrdersControllerTest {
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    @MockBean
//    AsyncGetService asyncGetService;
//    @MockBean
//    private CustomerOrderRepository customerOrderRepository;
//
//    @Autowired
//    private OrdersController ordersController;
//    private NewOrderResource newOrderResource;
//
//    private NewOrderResource orderToUpdate;
//    @Before
//    public void setUp() throws Exception {
//        // Prepare a dummy order resource with all URIs, but we'll set address to null for our specific test
//        newOrderResource = new NewOrderResource();
//        orderToUpdate = new NewOrderResource();
//        newOrderResource.customer = new URI("http://example.com/customer");
//        newOrderResource.card = new URI("http://example.com/card");
//        newOrderResource.items = new URI("http://example.com/items");
//        newOrderResource.address = new URI("http://example.com/address");
//
//        orderToUpdate.card = new URI("http://example.com/card");
//        orderToUpdate.items = new URI("http://example.com/items");
//        orderToUpdate.address = new URI("http://example.com/address");
//    }
//
//    @Test(expected = OrdersController.InvalidOrderException.class)
//    public void testNewOrder_MissingAddress() {
//        newOrderResource.address = null;
//        ordersController.newOrder(newOrderResource);
//    }
//
//    @Test
//    public void contextLoads() throws Exception {
//        assertThat(ordersController).isNotNull();
//    }
//
//    @Test
//    public void whenAddressIsNull_thenReturnsNotAcceptableHttpStatus() throws Exception {
//        newOrderResource.address = null;
//        mockMvc.perform(post("/orders")
//                        .contentType( MediaType.APPLICATION_JSON_VALUE)
//                        .content(objectMapper.writeValueAsString(newOrderResource))
//                        .characterEncoding("utf-8"))
//                .andExpect(status().isNotAcceptable());
//    }
//
//    @Test
//    public void whenAllDataIsValid_thenReturnsCreatedHttpStatus() throws Exception {
//        mockMvc.perform(post("/orders")
//                        .contentType(MediaType.APPLICATION_JSON_VALUE)
//                        .content(objectMapper.writeValueAsString(newOrderResource))
//                        .characterEncoding("utf-8"))
//                .andExpect(status().isCreated());
//    }
//
//    @Test(expected = OrdersController.InvalidOrderException.class)
//    public void testNewOrder_MissingCustomer() {
//        newOrderResource.customer = null;
//        ordersController.newOrder(newOrderResource);
//    }
//
//    @Test(expected = OrdersController.InvalidOrderException.class)
//    public void testNewOrder_MissingCard() {
//        newOrderResource.card = null;
//        ordersController.newOrder(newOrderResource);
//    }
//
//    @Test(expected = OrdersController.InvalidOrderException.class)
//    public void testNewOrder_MissingItems() {
//        newOrderResource.items = null;
//        ordersController.newOrder(newOrderResource);
//    }
//
//    @Test
//    public void whenGetMethodIsUsed_thenReturnsMethodNotAllowed() throws Exception {
//        mockMvc.perform(MockMvcRequestBuilders.get("/orders"))
//                .andExpect(status().isMethodNotAllowed());
//    }
//
//
//    @Test
//    public void whenPayloadIsMalformed_thenReturnsBadRequest() throws Exception {
//        String malformedJson = "{\"customer\":\"valueWithoutClosingBracket";
//        mockMvc.perform(post("/orders")
//                        .contentType(MediaType.APPLICATION_JSON_VALUE)
//                        .content(malformedJson)
//                        .characterEncoding("utf-8"))
//                .andExpect(status().isBadRequest());
//    }
//
//    @Test
//    public void testUpdateOrder_Success() throws Exception {
//        Mockito.when(asyncGetService.getDataList(orderToUpdate.items, new ParameterizedTypeReference<List<Item>>() {})).thenReturn(new AsyncResult<>(new ArrayList<>()));
//
//        Mockito.when(asyncGetService.getResource(orderToUpdate.customer, new TypeReferences.ResourceType<Customer>() {})).thenReturn(new AsyncResult<>(new Resource<Customer>(new Customer())));
//        Mockito.when(asyncGetService.getResource(orderToUpdate.card, new TypeReferences.ResourceType<Address>() {})).thenReturn(new AsyncResult(new Card()));
//        Mockito.when(asyncGetService.getResource(orderToUpdate.address, new TypeReferences.ResourceType<Address>() {})).thenReturn(new AsyncResult(new Address()));
//
//        mockMvc.perform(put("/orders/{orderId}", 1)  // Replace with the actual URL path from your controller and the actual order ID
//                        .contentType(MediaType.APPLICATION_JSON_VALUE)
//                        .content(objectMapper.writeValueAsString(orderToUpdate))
//                        .characterEncoding("utf-8"))
//                .andExpect(status().isOk());
//    }
//
//    @Test
//    public void testUpdateOrder_MissingMandatoryFields() throws Exception {
//        newOrderResource.address = null; // Assuming address is mandatory
//
//        mockMvc.perform(put("/orders/{orderId}", 1)
//                        .contentType(MediaType.APPLICATION_JSON_VALUE)
//                        .content(objectMapper.writeValueAsString(orderToUpdate))
//                        .characterEncoding("utf-8"))
//                .andExpect(status().isNotAcceptable());
//    }
//
//    @Test
//    public void testUpdateOrder_NonExistentOrder() throws Exception {
//        mockMvc.perform(put("/orders/{orderId}", 99999L) // Example non-existent order ID
//                        .contentType(MediaType.APPLICATION_JSON_VALUE)
//                        .content(objectMapper.writeValueAsString(orderToUpdate))
//                        .characterEncoding("utf-8"))
//                .andExpect(status().isNotFound());
//    }
//
//    @Test
//    public void testUpdateOrder_CustomerIDUpdate() throws Exception {
//        orderToUpdate.customer = new URI("http://example.com/new-customer-id"); // Attempt to change customer ID
//
//        mockMvc.perform(put("/orders/{orderId}", 1)
//                        .contentType(MediaType.APPLICATION_JSON_VALUE)
//                        .content(objectMapper.writeValueAsString(orderToUpdate))
//                        .characterEncoding("utf-8"))
//                .andExpect(status().isNotAcceptable());  // Assuming server responds with a 400 error when there's an attempt to change customer ID.
//    }
//
//}
