package test.support.com.pyxis.petstore.builders;

import com.pyxis.petstore.domain.billing.Address;
import com.pyxis.petstore.domain.billing.CreditCardDetails;
import com.pyxis.petstore.domain.order.Cart;
import com.pyxis.petstore.domain.order.Order;
import com.pyxis.petstore.domain.order.OrderNumber;

import static test.support.com.pyxis.petstore.builders.CartBuilder.aCart;
import static test.support.com.pyxis.petstore.builders.OrderNumberFaker.aNumber;

public class OrderBuilder implements Builder<Order> {

    private OrderNumber orderNumber = aNumber();
    private Cart cart = aCart().build();
    private Address billingAddress;
    private CreditCardDetails creditCardDetails;

    public static OrderBuilder anOrder() {
        return new OrderBuilder();
    }

    public OrderBuilder from(CartBuilder cart) {
        return from(cart.build());
    }

    public OrderBuilder from(Cart cart) {
        this.cart = cart;
        return this;
    }

    public Order build() {
        Order order = new Order(orderNumber);
        order.addItemsFromCart(cart);
        order.markPaidWith(creditCardDetails);
        return order;
    }

    public OrderBuilder withNumber(String orderNumber) {
        return with(new OrderNumber(orderNumber));
    }

    public OrderBuilder with(final OrderNumber orderNumber) {
        this.orderNumber = orderNumber;
        return this;
    }

    public OrderBuilder paidWith(CreditCardBuilder creditCardBuilder) {
        this.creditCardDetails = creditCardBuilder.build();
        return this;
    }

    public OrderBuilder billedTo(AddressBuilder addressBuilder) {
        this.billingAddress = addressBuilder.build();
        return this;
    }
}