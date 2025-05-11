package com.kdajv.project.system.domain;

public class DifficultyStats {
    private Integer level;
    private Long count;

    public DifficultyStats() {
    }

    public DifficultyStats(Integer level, Long count) {
        this.level = level;
        this.count = count;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }
}
