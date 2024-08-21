package com.sparta.myselectshop.service;

import com.sparta.myselectshop.dto.ProductMypriceRequestDto;
import com.sparta.myselectshop.dto.ProductRequestDto;
import com.sparta.myselectshop.dto.ProductResponseDto;
import com.sparta.myselectshop.entity.Product;
import com.sparta.myselectshop.entity.User;
import com.sparta.myselectshop.entity.UserRoleEnum;
import com.sparta.myselectshop.naver.dtos.ItemDto;
import com.sparta.myselectshop.repository.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    public static final int MIN_MY_PRICE = 100;

    @Transactional
    public ProductResponseDto createProduct(ProductRequestDto requestDto, User user) {
        Product product = new Product(requestDto,user);
        Product saveProduct =  productRepository.save(product);
        return new ProductResponseDto(saveProduct);
    }

    @Transactional
    public ProductResponseDto updateProduct(Long id, ProductMypriceRequestDto requestDto, User user) {
        int myprice = requestDto.getMyprice();
        if (myprice < MIN_MY_PRICE) throw new IllegalArgumentException("유효하지않은 관심 가격 입니다 최소 " + MIN_MY_PRICE + "원 이상 입니다");
        Product product = productRepository.findById(id).orElseThrow(() -> new NullPointerException("유효하지 않은 상품 입니다"));
        product.update(requestDto);
        return new ProductResponseDto(product);
    }

    public Page<ProductResponseDto> getProducts(User user, int page, int size, String sortBy, boolean isAsc) {

        Sort.Direction direction = isAsc ?  Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, sortBy);
        Pageable pageable = PageRequest.of(page,size,sort);

        UserRoleEnum userRoleEnum = user.getRole();
        Page<Product> productList;
        if (userRoleEnum == UserRoleEnum.USER) productList = productRepository.findAllByUser(user,pageable);
        else productList = productRepository.findAll(pageable);

        return productList.map(ProductResponseDto::new);
    }

    @Transactional
    public void updateBySearch(Long id, ItemDto itemDto) {
        Product product = productRepository.findById(id).orElseThrow(() -> new NullPointerException("유효한 상품이 아닙니다"));
        product.updateByItemDto(itemDto);
    }

}
