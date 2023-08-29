package works.weave.socks.orders;

import works.weave.socks.orders.values.InternalPaymentRequest;
import works.weave.socks.orders.values.PaymentRequest;

public class ObjectMapper {
    public InternalPaymentRequest toInternalPaymentRequest(PaymentRequest request){
        return new InternalPaymentRequest(request.getAddress(), request.getCard(), request.getCustomer(), request.getAmount());
    }
}
