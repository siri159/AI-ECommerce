package com.ecommerce.backend.config;

import com.ecommerce.backend.entity.Category;
import com.ecommerce.backend.entity.Product;
import com.ecommerce.backend.entity.User;
import com.ecommerce.backend.repository.CategoryRepository;
import com.ecommerce.backend.repository.ProductRepository;
import com.ecommerce.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            User admin = User.builder()
                    .name("System Admin")
                    .email("admin@ecommerce.com")
                    .password(passwordEncoder.encode("Admin@123"))
                    .phone("9999999999")
                    .role(User.Role.ADMIN)
                    .build();
            userRepository.save(admin);

            User customer = User.builder()
                    .name("Demo Customer")
                    .email("customer@ecommerce.com")
                    .password(passwordEncoder.encode("Customer@123"))
                    .phone("8888888888")
                    .role(User.Role.CUSTOMER)
                    .build();
            userRepository.save(customer);
        }

        Category electronics = categoryRepository.findByName("Electronics")
                .orElseGet(() -> categoryRepository.save(Category.builder().name("Electronics").description("Smart devices and gadgets").build()));
        Category fashion = categoryRepository.findByName("Fashion")
                .orElseGet(() -> categoryRepository.save(Category.builder().name("Fashion").description("Trendy lifestyle products").build()));
        Category home = categoryRepository.findByName("Home & Living")
                .orElseGet(() -> categoryRepository.save(Category.builder().name("Home & Living").description("Daily essentials and home décor").build()));
        Category books = categoryRepository.findByName("Books")
                .orElseGet(() -> categoryRepository.save(Category.builder().name("Books").description("Educational and entertainment reading").build()));

        List<Product> products = List.of(
                Product.builder().name("Dell Inspiron Laptop").description("13th Gen laptop with AI features").price(new BigDecimal("67999")).stockQuantity(15).imageUrl("https://images.unsplash.com/photo-1496181133206-80ce9b88a853?auto=format&fit=crop&w=1200&q=80").brand("Dell").category(electronics).active(true).build(),
                Product.builder().name("Samsung Galaxy S24").description("Premium smartphone with excellent camera").price(new BigDecimal("79999")).stockQuantity(25).imageUrl("https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?auto=format&fit=crop&w=1200&q=80").brand("Samsung").category(electronics).active(true).build(),
                Product.builder().name("Sony WH-1000XM5").description("Noise cancelling wireless headphones").price(new BigDecimal("29999")).stockQuantity(30).imageUrl("https://images.unsplash.com/photo-1546435770-a3e426bf472b?auto=format&fit=crop&w=1200&q=80").brand("Sony").category(electronics).active(true).build(),
                Product.builder().name("Apple iPad Air").description("Powerful tablet for work and play").price(new BigDecimal("59999")).stockQuantity(12).imageUrl("https://images.unsplash.com/photo-1544244015-0df4b3ffc6b0?auto=format&fit=crop&w=1200&q=80").brand("Apple").category(electronics).active(true).build(),
                Product.builder().name("Nike Air Max 270").description("Lightweight sporty sneakers").price(new BigDecimal("7499")).stockQuantity(50).imageUrl("https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=1200&q=80").brand("Nike").category(fashion).active(true).build(),
                Product.builder().name("Classic Leather Jacket").description("Premium everyday jacket").price(new BigDecimal("14999")).stockQuantity(18).imageUrl("https://images.unsplash.com/photo-1521572267360-ee0c2909d518?auto=format&fit=crop&w=1200&q=80").brand("Levi's").category(fashion).active(true).build(),
                Product.builder().name("Premium Coffee Maker").description("Automatic coffee machine for modern kitchens").price(new BigDecimal("8999")).stockQuantity(22).imageUrl("https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?auto=format&fit=crop&w=1200&q=80").brand("Philips").category(home).active(true).build(),
                Product.builder().name("Bamboo Storage Basket").description("Minimal style home organizer").price(new BigDecimal("1499")).stockQuantity(80).imageUrl("https://images.unsplash.com/photo-1505693416388-ac5ce068fe85?auto=format&fit=crop&w=1200&q=80").brand("EcoHome").category(home).active(true).build(),
                Product.builder().name("Java for Beginners").description("Step-by-step guide to develop Java skills").price(new BigDecimal("499")).stockQuantity(40).imageUrl("https://images.unsplash.com/photo-1516979187457-637abb4f9353?auto=format&fit=crop&w=1200&q=80").brand("EduPress").category(books).active(true).build(),
                Product.builder().name("Machine Learning Handbook").description("A modern practical guide for data science learners").price(new BigDecimal("899")).stockQuantity(12).imageUrl("https://images.unsplash.com/photo-1524995997946-a1c2e315a42f?auto=format&fit=crop&w=1200&q=80").brand("TechReads").category(books).active(true).build()
        );

        for (Product product : products) {
            productRepository.findByNameAndCategoryId(product.getName(), product.getCategory().getId())
                    .ifPresentOrElse(existing -> {
                        existing.setDescription(product.getDescription());
                        existing.setPrice(product.getPrice());
                        existing.setStockQuantity(product.getStockQuantity());
                        existing.setImageUrl(product.getImageUrl());
                        existing.setBrand(product.getBrand());
                        existing.setActive(product.isActive());
                        productRepository.save(existing);
                    }, () -> productRepository.save(product));
        }
    }
}
