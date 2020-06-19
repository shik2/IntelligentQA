package com.appleyk.domain;

public class Question {

    private Integer id;
    private String query;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    @Override
    public String toString() {
        return "Question{" +
                "id=" + id +
                ", query='" + query + '\'' +
                '}';
    }
}
