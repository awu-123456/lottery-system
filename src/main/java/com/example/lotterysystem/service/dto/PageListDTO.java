package com.example.lotterysystem.service.dto;

import lombok.Data;

import java.util.List;

@Data
public class PageListDTO<T> {
    private int total;

    private List<T> records;

    public PageListDTO(int total, List<T> records) {
        this.total = total;
        this.records = records;
    }
}
