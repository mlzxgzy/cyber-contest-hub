package com.kdajv.project.system.domain.dto;

public class ActiveCompetitorDTO {
    private Long id;
    private String name;
    private Integer count;

    public ActiveCompetitorDTO() {
    }

    public ActiveCompetitorDTO(Long id, String name, Integer count) {
        this.id = id;
        this.name = name;
        this.count = count;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}