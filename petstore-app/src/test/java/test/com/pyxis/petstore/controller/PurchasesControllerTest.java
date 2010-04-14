package test.com.pyxis.petstore.controller;

import com.pyxis.petstore.controller.PurchasesController;
import com.pyxis.petstore.domain.billing.CreditCardDetails;
import com.pyxis.petstore.domain.billing.CreditCardType;
import com.pyxis.petstore.domain.order.Cart;
import com.pyxis.petstore.domain.order.CheckoutAssistant;
import com.pyxis.petstore.domain.order.Order;
import com.pyxis.petstore.domain.order.PaymentCollector;
import org.hamcrest.Matcher;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.ModelAndView;

import static com.pyxis.matchers.spring.SpringMatchers.hasAttribute;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.samePropertyValuesAs;
import static test.support.com.pyxis.petstore.builders.AddressBuilder.anAddress;
import static test.support.com.pyxis.petstore.builders.CartBuilder.aCart;
import static test.support.com.pyxis.petstore.builders.CreditCardBuilder.aVisa;
import static test.support.com.pyxis.petstore.builders.ItemBuilder.anItem;
import static test.support.com.pyxis.petstore.builders.OrderBuilder.anOrder;

@RunWith(JMock.class)
public class PurchasesControllerTest {

    Mockery context = new JUnit4Mockery();
    CheckoutAssistant checkoutAssistant = context.mock(CheckoutAssistant.class);
    PaymentCollector paymentCollector = context.mock(PaymentCollector.class);
    Cart cart = aCart().containing(anItem()).build();
    PurchasesController controller = new PurchasesController(cart, checkoutAssistant, paymentCollector);

    @Test public void
    checkoutCartsAndRendersPurchaseForm() {
        ModelAndView modelAndView = controller.checkout();
        assertThat(modelAndView.getViewName(), equalTo("purchases/new"));
        assertThat(modelAndView.getModel(), hasAttribute("cart", cart));
        assertThat(modelAndView.getModel(), hasAttribute("cardTypes", CreditCardType.values()));
    }

    @Test public void
    collectsPaymentAndRedirectToReceiptView() {
        final Order order = anOrder().from(cart).withNumber("12345678").build();

        final CreditCardDetails paymentDetails = aVisa().
                billedTo(anAddress().
                    withFirstName("John").
                    withLastName("Leclair").
                    withEmail("jleclair@gmail.com")).
                withNumber("9999 9999 9999").
                withExpiryDate("12/12").build();

        context.checking(new Expectations() {{
            oneOf(checkoutAssistant).checkout(cart); will(returnValue(order));
            oneOf(paymentCollector).collectPayment(
                    with(equal(order)),
                    with(samePaymentMethodAs(paymentDetails)));
        }});
        String view = controller.create(paymentDetails, dummyBindingOn(paymentDetails));
        assertThat(view, equalTo("redirect:/receipts/" + order.getNumber()));
    }

    private BindingResult dummyBindingOn(CreditCardDetails paymentDetails) {
        return new BeanPropertyBindingResult(paymentDetails, "paymentDetails");
    }

    private Matcher<CreditCardDetails> samePaymentMethodAs(CreditCardDetails paymentMethod) {
        return samePropertyValuesAs(paymentMethod);
    }
}