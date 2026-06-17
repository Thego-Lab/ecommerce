package com.ecommerce.service.impl;

import com.ecommerce.dto.request.AddCartItemRequest;
import com.ecommerce.dto.request.UpdateCartItemRequest;
import com.ecommerce.dto.response.CartItemResponse;
import com.ecommerce.dto.response.CartResponse;
import com.ecommerce.entity.Cart;
import com.ecommerce.entity.CartItem;
import com.ecommerce.entity.Product;
import com.ecommerce.exception.BusinessException;
import com.ecommerce.mapper.CartItemMapper;
import com.ecommerce.mapper.CartMapper;
import com.ecommerce.mapper.ProductMapper;
import com.ecommerce.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private CartItemMapper cartItemMapper;

    @Autowired
    private ProductMapper productMapper;

    @Override
    public CartResponse getCart(Long userId) {
        Cart cart = getOrCreateCart(userId);
        List<CartItem> items = cartItemMapper.selectByCartId(cart.getId());

        CartResponse response = new CartResponse();
        response.setCartId(cart.getId());

        List<CartItemResponse> itemResponses = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (CartItem item : items) {
            Product product = productMapper.selectById(item.getProductId());
            if (product == null || product.getStatus() != 1) {
                continue;
            }

            CartItemResponse ir = new CartItemResponse();
            ir.setCartItemId(item.getId());
            ir.setProductId(product.getId());
            ir.setProductName(product.getName());
            ir.setProductImage(product.getImage());
            ir.setPrice(product.getPrice());
            ir.setQuantity(item.getQuantity());
            ir.setChecked(item.getChecked());

            // 计算小计（仅已勾选的计入总价）
            BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            if (item.getChecked() == 1) {
                total = total.add(subtotal);
            }

            itemResponses.add(ir);
        }

        response.setItems(itemResponses);
        response.setTotalAmount(total);
        return response;
    }

    @Override
    public void addItem(Long userId, AddCartItemRequest request) {
        Product product = productMapper.selectById(request.getProductId());
        if (product == null || product.getStatus() != 1) {
            throw new BusinessException("商品不存在或已下架");
        }

        int quantity = request.getQuantity() != null ? request.getQuantity() : 1;
        if (quantity <= 0) {
            throw new BusinessException("数量必须大于0");
        }

        Cart cart = getOrCreateCart(userId);

        // 检查购物车中是否已有同商品
        CartItem exist = cartItemMapper.selectByCartIdAndProductId(cart.getId(), request.getProductId());
        if (exist != null) {
            exist.setQuantity(exist.getQuantity() + quantity);
            cartItemMapper.update(exist);
            return;
        }

        CartItem item = new CartItem();
        item.setCartId(cart.getId());
        item.setProductId(request.getProductId());
        item.setQuantity(quantity);
        item.setChecked(1);

        try {
            cartItemMapper.insert(item);
        } catch (DuplicateKeyException e) {
            // 并发下可能重复，再查一次并累加
            CartItem dup = cartItemMapper.selectByCartIdAndProductId(cart.getId(), request.getProductId());
            if (dup != null) {
                dup.setQuantity(dup.getQuantity() + quantity);
                cartItemMapper.update(dup);
            }
        }
    }

    @Override
    public void updateItem(Long userId, Long itemId, UpdateCartItemRequest request) {
        Cart cart = getOrCreateCart(userId);
        CartItem item = cartItemMapper.selectById(itemId);
        if (item == null || !item.getCartId().equals(cart.getId())) {
            throw new BusinessException("购物车项不存在");
        }

        if (request.getQuantity() != null) {
            if (request.getQuantity() <= 0) {
                cartItemMapper.delete(itemId);
                return;
            }
            item.setQuantity(request.getQuantity());
        }
        if (request.getChecked() != null) {
            item.setChecked(request.getChecked());
        }
        cartItemMapper.update(item);
    }

    @Override
    public void removeItem(Long userId, Long itemId) {
        Cart cart = getOrCreateCart(userId);
        CartItem item = cartItemMapper.selectById(itemId);
        if (item == null || !item.getCartId().equals(cart.getId())) {
            throw new BusinessException("购物车项不存在");
        }
        cartItemMapper.delete(itemId);
    }

    @Override
    public void clearCart(Long userId) {
        Cart cart = cartMapper.selectByUserId(userId);
        if (cart != null) {
            cartItemMapper.deleteByCartId(cart.getId());
        }
    }

    private Cart getOrCreateCart(Long userId) {
        Cart cart = cartMapper.selectByUserId(userId);
        if (cart == null) {
            cart = new Cart();
            cart.setUserId(userId);
            cartMapper.insert(cart);
        }
        return cart;
    }
}
