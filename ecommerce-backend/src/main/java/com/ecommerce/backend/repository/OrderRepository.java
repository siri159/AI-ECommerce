package com.ecommerce.backend.repository;

import com.ecommerce.backend.entity.Order;
import com.ecommerce.backend.entity.User;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUser(User user, Sort sort);
    List<Order> findByUserId(Long userId, Sort sort);
    List<Order> findByStatus(Order.OrderStatus status, Sort sort);
}
