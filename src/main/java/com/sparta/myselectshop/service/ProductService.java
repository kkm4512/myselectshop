package com.sparta.myselectshop.service;

import com.sparta.myselectshop.dto.ProductMypriceRequestDto;
import com.sparta.myselectshop.dto.ProductRequestDto;
import com.sparta.myselectshop.dto.ProductResponseDto;
import com.sparta.myselectshop.entity.*;
import com.sparta.myselectshop.naver.dtos.ItemDto;
import com.sparta.myselectshop.repository.FolderRepository;
import com.sparta.myselectshop.repository.ProductFolderRepository;
import com.sparta.myselectshop.repository.ProductRepository;
import com.sparta.myselectshop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    public static final int MIN_MY_PRICE = 100;
    private final FolderRepository folderRepository;
    private final ProductFolderRepository productFolderRepository;
    private final UserRepository userRepository;

    @Transactional
    public ProductResponseDto createProduct(ProductRequestDto requestDto, User user) {
        Product product = new Product(requestDto,user);
        Product saveProduct =  productRepository.save(product);
        return new ProductResponseDto(saveProduct);
    }

    @Transactional
    public ProductResponseDto updateProduct(Long id, ProductMypriceRequestDto requestDto) {
        int myprice = requestDto.getMyprice();
        if (myprice < MIN_MY_PRICE) throw new IllegalArgumentException("유효하지않은 관심 가격 입니다 최소 " + MIN_MY_PRICE + "원 이상 입니다");
        Product product = productRepository.findById(id).orElseThrow(() -> new NullPointerException("유효하지 않은 상품 입니다"));
        product.update(requestDto);
        return new ProductResponseDto(product);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponseDto> getProducts(int page, int size, String sortBy, boolean isAsc) {
        User user = userRepository.findById(1).orElseThrow();
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

    public void addFolder(Long productId, Long folderId, User user) {
        Product product = productRepository.findById(productId).orElseThrow(() -> new NullPointerException("해당 상품이 존재하지 않습니다"));
        Folder folder = folderRepository.findById(folderId).orElseThrow(() -> new NullPointerException("해당 폴더이 존재하지 않습니다"));
        if (!product.getUser().getId().equals(user.getId()) || !folder.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("회원의 관심사 폴더가 아니거나, 회원님의 폴더가 아닙니다");
        }

        Optional<ProductFolder> overlapFolder =  productFolderRepository.findByProductAndFolder(product,folder);
        if (overlapFolder.isPresent()) {
            throw new IllegalArgumentException("중복된 폴더 입니다");
        }

        productFolderRepository.save(new ProductFolder(product,folder));

    }

    public Page<ProductResponseDto>  getProductsInFolder(Long folderId, int page, int size, String sortBy, boolean isAsc, User user) {
        Sort.Direction direction = isAsc ?  Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, sortBy);
        Pageable pageable = PageRequest.of(page,size,sort);
        Page<Product> responseDtoList = productRepository.findAllByUserAndProductFolderList_FolderId(user,folderId,pageable);
        return responseDtoList.map(ProductResponseDto::new);
    }
}
