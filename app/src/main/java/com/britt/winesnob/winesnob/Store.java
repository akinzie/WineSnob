package com.britt.winesnob.winesnob;

public class Store {

    Integer id;
    String name;
    String add1;
    String add2;
    String city;
    String postal_code;
    String telephone;

    public Store(Integer id, String name, String add1, String add2, String city, String postal_code, String telephone) {
        this.id = id;
        this.name = name;
        this.add1 = add1;
        this.add2 = add2;
        this.city = city;
        this.postal_code = postal_code;
        this.telephone = telephone;
    }

    public String getName() {
        return name;
    }
    public String getAdd1() {
        return add1;
    }

    public String getCity() {
        return city;
    }

    public String getPostal_code() {
        return postal_code;
    }
}
