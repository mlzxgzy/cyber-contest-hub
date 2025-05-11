package com.kdajv.project.system.domain;

public class CompetitionTimeStats {
    private String date;
    private Long count;

    public CompetitionTimeStats() {
    }

    public CompetitionTimeStats(String date, Long count) {
        this.date = date;
        this.count = count;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}