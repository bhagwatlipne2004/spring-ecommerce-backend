package com.bhagwat.springcommerce.common.mapper;

import com.bhagwat.springcommerce.common.dto.PageResponse;
import org.springframework.data.domain.Page;

public class PageMapper {

    public static <T>PageResponse<T> toPageResponse(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );

    }
}
