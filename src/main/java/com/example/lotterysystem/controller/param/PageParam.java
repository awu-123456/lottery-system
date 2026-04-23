package com.example.lotterysystem.controller.param;

import lombok.Data;

import java.io.Serializable;

@Data
public class PageParam implements Serializable {

    private Integer currentPage;

    private Integer pageSize;

    public int offset() {
        return (currentPage - 1) * pageSize;
    }
}
