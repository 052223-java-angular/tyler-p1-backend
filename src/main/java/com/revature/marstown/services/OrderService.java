package com.revature.marstown.services;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Collections;

import org.springframework.stereotype.Service;

import com.revature.marstown.components.StripePrices;
import com.revature.marstown.dtos.responses.StripePriceResponse;
import com.revature.marstown.entities.Cart;
import com.revature.marstown.entities.CartMenuItemOffer;
import com.revature.marstown.entities.Order;
import com.revature.marstown.entities.OrderMenuItemOffer;
import com.revature.marstown.entities.User;
import com.revature.marstown.repositories.OrderMenuItemOfferRepository;
import com.revature.marstown.repositories.OrderRepository;
import com.revature.marstown.utils.PriceUtil;

import lombok.AllArgsConstructor;

/**
 * The OrderService class provides operations related orders
 */
@Service
@AllArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderMenuItemOfferRepository orderMenuItemOfferRepository;
    private final UserService userService;
    private final StripeService stripeService;
    private final PointService pointService;
    private final StripePrices stripePrices;

    public void createOrderFromCart(Cart cart) {
        User user = userService.getById(cart.getUser().getId());
        Order order = new Order(user);
        orderRepository.save(order);
        createOrderMenuItemOffers(order, cart);
    }

    private void createOrderMenuItemOffers(Order order, Cart cart) {
        double amount = 0;
        for (CartMenuItemOffer cartMenuItemOffer : cart.getCartMenuItemOffers()) {
            if (cartMenuItemOffer.getParentCartMenuItemOffer() == null) {
                BigDecimal price = getStripePriceFromCartMenuItemOffer(cartMenuItemOffer);
                OrderMenuItemOffer orderMenuItemOffer = new OrderMenuItemOffer(order, cartMenuItemOffer, price);
                orderMenuItemOfferRepository.save(orderMenuItemOffer);
                amount = amount + orderMenuItemOffer.getQuantity() * price.doubleValue();
                for (CartMenuItemOffer childCartMenuItemOffer : cartMenuItemOffer.getChildCartMenuItemOffers()) {
                    BigDecimal childPrice = getStripePriceFromCartMenuItemOffer(childCartMenuItemOffer);
                    OrderMenuItemOffer childOrderMenuItemOffer = new OrderMenuItemOffer(order, childCartMenuItemOffer,
                            childPrice,
                            orderMenuItemOffer);
                    orderMenuItemOfferRepository.save(childOrderMenuItemOffer);
                    amount = amount + childOrderMenuItemOffer.getQuantity() * childPrice.doubleValue();
                }
            }
        }
        pointService.addPoints(order.getUser().getId(), (long) Math.floor(amount));
        order.setAmount(new BigDecimal(amount - (cart.getPointsApplied().doubleValue() / 100)));
        order.setCreatedDate(new Date());
        orderRepository.save(order);
    }

    private BigDecimal getStripePriceFromCartMenuItemOffer(CartMenuItemOffer cartMenuItemOffer) {
        StripePriceResponse priceResponse = stripeService.findStripePrice(
                cartMenuItemOffer.getMenuItemOffer().getStripePriceId(), stripePrices.getStripePriceResponse());
        String priceString = priceResponse.getUnit_amount_decimal();
        return PriceUtil.stripePriceStringToBigDecimal(priceString);
    }

    public List<Order> getAllOrdersForUser(String userId) {
        var orders = orderRepository.findByUserId(userId);
        Collections.sort(orders, new Comparator<Order>() {
            public int compare(Order o1, Order o2) {
                if (o1.getCreatedDate() == null || o2.getCreatedDate() == null)
                    return 0;
                return o2.getCreatedDate().compareTo(o1.getCreatedDate());
            }
        });

        return orders;
    }

    public Optional<Order> getById(String orderId) {
        return orderRepository.findById(orderId);
    }

    public Optional<Order> getLatestOrder(String userId) {
        return orderRepository.findFirstByUserIdOrderByCreatedDateDesc(userId);
    }
}
