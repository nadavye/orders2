package works.weave.socks.orders.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.mvc.TypeReferences;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import works.weave.socks.orders.ObjectMapper;
import works.weave.socks.orders.config.OrdersConfigurationProperties;
import works.weave.socks.orders.entities.*;
import works.weave.socks.orders.repositories.CustomerOrderRepository;
import works.weave.socks.orders.resources.NewOrderResource;
import works.weave.socks.orders.services.AsyncGetService;
import works.weave.socks.orders.values.PaymentRequest;
import works.weave.socks.orders.values.PaymentResponse;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


//@RepositoryRestController
@RestController
public class OrdersController {
    private final Logger LOG = LoggerFactory.getLogger(getClass());

    private OrdersHelper ordersHelper = new OrdersHelper();
    ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private OrdersConfigurationProperties config;

    @Autowired
    private AsyncGetService asyncGetService;

    @Autowired
    private CustomerOrderRepository customerOrderRepository;

    @Value(value = "${http.timeout:5}")
    private long timeout;

    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(path = "/orders", consumes = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.POST)
    public
    @ResponseBody
    CustomerOrder newOrder(@RequestBody NewOrderResource item) {
        try {
            System.out.println("newOrder - Nadav!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");


            if (item.address == null || item.customer == null || item.card == null || item.items == null) {
                throw new InvalidOrderException("Invalid order request. Order requires customer, address, card and items.");
            }


            LOG.debug("Starting calls");
            Future<Resource<Address>> addressFuture = asyncGetService.getResource(item.address, new TypeReferences
                    .ResourceType<Address>() {
            });
            Future<Resource<Customer>> customerFuture = asyncGetService.getResource(item.customer, new TypeReferences
                    .ResourceType<Customer>() {
            });
            Future<Resource<Card>> cardFuture = asyncGetService.getResource(item.card, new TypeReferences
                    .ResourceType<Card>() {
            });
            Future<List<Item>> itemsFuture = asyncGetService.getDataList(item.items, new
                    ParameterizedTypeReference<List<Item>>() {
            });
            LOG.debug("End of calls.");

            float amount = calculateTotal(itemsFuture.get(timeout, TimeUnit.SECONDS));
            amount = (float) ordersHelper.applyDiscountIfRelevant((double) amount);

            // Call payment service to make sure they've paid
            PaymentRequest paymentRequest = new PaymentRequest(
                    addressFuture.get(timeout, TimeUnit.SECONDS).getContent(),
                    cardFuture.get(timeout, TimeUnit.SECONDS).getContent(),
                    customerFuture.get(timeout, TimeUnit.SECONDS).getContent(),
                    amount);

            objectMapper.toInternalPaymentRequest(paymentRequest);

            LOG.info("Sending payment request: " + paymentRequest);
            Future<PaymentResponse> paymentFuture = asyncGetService.postResource(
                    config.getPaymentUri(),
                    paymentRequest,
                    new ParameterizedTypeReference<PaymentResponse>() {
                    });
            PaymentResponse paymentResponse = paymentFuture.get(timeout, TimeUnit.SECONDS);
            LOG.info("Received payment response: " + paymentResponse);
            if (paymentResponse == null) {
                throw new PaymentDeclinedException("Unable to parse authorisation packet");
            }
            if (!paymentResponse.isAuthorised()) {
                throw new PaymentDeclinedException(paymentResponse.getMessage());
            }

            // Ship
            String customerId = parseId(customerFuture.get(timeout, TimeUnit.SECONDS).getId().getHref());
            Future<Shipment> shipmentFuture = asyncGetService.postResource(config.getShippingUri(), new Shipment
                    (customerId), new ParameterizedTypeReference<Shipment>() {
            });



            CustomerOrder order = new CustomerOrder(
                    null,
                    customerId,
                    customerFuture.get(timeout, TimeUnit.SECONDS).getContent(),
                    addressFuture.get(timeout, TimeUnit.SECONDS).getContent(),
                    cardFuture.get(timeout, TimeUnit.SECONDS).getContent(),
                    itemsFuture.get(timeout, TimeUnit.SECONDS),
                    shipmentFuture.get(timeout, TimeUnit.SECONDS),
                    Calendar.getInstance().getTime(),
                    amount);
            LOG.debug("Received data: " + order.toString());

            CustomerOrder savedOrder = customerOrderRepository.save(order);
            LOG.debug("Saved order: " + savedOrder);

            ordersHelper.collectStatistics("OrderCompleted");
            return savedOrder;
        } catch (TimeoutException e) {
            LOG.error("Failed to update order due to timeout exception.", e);
            throw new IllegalStateException("Unable to create order due to timeout from one of the services.", e);
        } catch (InterruptedException | IOException | ExecutionException e) {
            LOG.error("Failed to update order due to error.", e);
            throw new IllegalStateException("Unable to create order due to unspecified IO error.", e);
        }
    }

    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(path = "/orders/{orderId}", consumes = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.PUT)
    public
    @ResponseBody
    CustomerOrder updateOrder(   @PathVariable("orderId") Long orderId,
                                 @RequestBody NewOrderResource orderToUpdate) {
        try {
            System.out.println("Update order - Some change by Nadav!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            if (orderId == null || orderToUpdate.address == null  || orderToUpdate.card == null || orderToUpdate.items == null) {
                throw new InvalidOrderException("Invalid update order request. Order requires id, address, card and items.");
            }

            if (orderToUpdate.customer != null)
                throw new InvalidOrderException("Invalid update order request. Customer name cannot be updated.");


            LOG.debug("Starting calls");

            Optional<CustomerOrder> order2 = customerOrderRepository.findById(orderId.toString());

            CustomerOrder order = new CustomerOrder();
            order.setId(orderId.toString());
            order.setItems(order2.get().getItems());
            order.setCard(order2.get().getCard());
            order.setAddress(order2.get().getAddress());
            order.setCustomerId(order2.get().getCustomerId());
            order.setDate(Calendar.getInstance().getTime());
            LOG.debug("Received data: " + order.toString());

            CustomerOrder savedOrder = customerOrderRepository.save(order);
            LOG.debug("Saved order: " + savedOrder);

            ordersHelper.collectStatistics("OrderUpdated");

            return savedOrder;
        } catch (RuntimeException e) {
            LOG.error("Failed to update order due to error.", e);
            throw new InvalidOrderException("Unable to update order due to internal error:" + e.getMessage());
        }
    }

    private String parseId(String href) {
        Pattern idPattern = Pattern.compile("[\\w-]+$");
        Matcher matcher = idPattern.matcher(href);
        if (!matcher.find()) {
            throw new IllegalStateException("Could not parse user ID from: " + href);
        }
        return matcher.group(0);
    }

       private String myUntestedFunction(String href) {
        Pattern idPattern = Pattern.compile("[\\w-]+$");
        Matcher matcher = idPattern.matcher(href);
        if (!matcher.find()) {
            throw new IllegalStateException("Could not parse user ID from: " + href);
        }
        return matcher.group(0);
    }

//    TODO: Add link to shipping
//    @RequestMapping(method = RequestMethod.GET, value = "/orders")
//    public @ResponseBody
//    ResponseEntity<?> getOrders() {
//        List<CustomerOrder> customerOrders = customerOrderRepository.findAll();
//
//        Resources<CustomerOrder> resources = new Resources<>(customerOrders);
//
//        resources.forEach(r -> r);
//
//        resources.add(linkTo(methodOn(ShippingController.class, CustomerOrder.getShipment::ge)).withSelfRel());
//
//        // add other links as needed
//
//        return ResponseEntity.ok(resources);
//    }

    private float calculateTotal(List<Item> items) {
        float amount = 0F;
        float shipping = 4.99F;
        amount += items.stream().mapToDouble(i -> i.getQuantity() * i.getUnitPrice()).sum();
        amount += shipping;
        return amount;
    }

    @ResponseStatus(value = HttpStatus.NOT_ACCEPTABLE)
    public class PaymentDeclinedException extends IllegalStateException {
        public PaymentDeclinedException(String s) {
            super(s);
        }
    }

    @ResponseStatus(value = HttpStatus.NOT_ACCEPTABLE)
    public class InvalidOrderException extends IllegalStateException {
        public InvalidOrderException(String s) {
            super(s);
        }
    }

}
