package com.olh.feeds.dto.mapper;


import com.olh.feeds.dto.response.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class PageMapper {

    public <T> PageResponse<T> toPageDto(Page<T> page) {
        PageResponse<T> result = new PageResponse<>();

        result.setContent(page.getContent());
        result.setLast(page.isLast());
        result.setTotalPages(page.getTotalPages());
        result.setTotalElements(page.getTotalElements());
        result.setNumber(page.getNumber());
        result.setSize(page.getSize());
        result.setFirst(page.isFirst());
        result.setNumberOfElements(page.getNumberOfElements());

        return result;
    }

}
