package com.example.CC;

public class RideModel {
    private String source, destination, date, time, carNo, phone, noOfSeats, basePrice, carType;

    public RideModel(String source, String destination, String date, String time, String carNo, String phone, String noOfSeats, String basePrice, String carType) {
        this.source = source;
        this.destination = destination;
        this.date = date;
        this.time = time;
        this.carNo = carNo;
        this.phone = phone;
        this.noOfSeats = noOfSeats;
        this.basePrice = basePrice;
        this.carType = carType;
    }

    public String getSource() { return source; }
    public String getDestination() { return destination; }
    public String getDate() { return date; }
    public String getTime() { return time; }
    public String getCarNo() { return carNo; }
    public String getPhone() { return phone; }
    public String getNoOfSeats() { return noOfSeats; }
    public String getBasePrice() { return basePrice; }
    public String getCarType(){return carType;}

}