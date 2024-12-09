package com.nicollasprado.gerenciadorPedidos.repositories;

import com.nicollasprado.gerenciadorPedidos.models.Cart;
import com.nicollasprado.gerenciadorPedidos.models.CartProducts;
import com.nicollasprado.gerenciadorPedidos.models.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CartProductsRepository extends JpaRepository<CartProducts, Long> {
    @Query(value = "DELETE FROM cart_products cp WHERE cp.user_id = :userID AND cp.product_id = :productID",nativeQuery = true)
    void removeFromCartByIds(@Param("userId") Long userId, @Param("productId") Long productId);

    @Query(value = "SELECT product_id FROM cart_products cp WHERE cp.user_id = :userId AND cp.product_id = :productId LIMIT 1", nativeQuery = true)
    Long getProductIdByUserIdAndProductId(@Param("userId") Long userId, @Param("productId") Long productId);

    @Query(value = "SELECT quantity FROM cart_products cp WHERE cp.user_id = :userId AND cp.product_id = :productId LIMIT 1", nativeQuery = true)
    int getQuantityByUserIdAndProductId(@Param("userId") Long userId, @Param("productId") Long productId);
}
