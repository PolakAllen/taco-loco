package com.tacoloco.model;

import lombok.Data;

@Data
public class Order{

  public Integer veggie = 0;
  public Integer chicken = 0;
  public Integer beef = 0;
  public Integer chorizo = 0;

  public static final double VEGPRICE = 2.50;
  public static final double CHICKPRICE = 3;
  public static final double BEEFPRICE = 3;
  public static final double CHORIZPRICE = 3.50;
  
  public double getTotalPrice(){
    double totalPrice = veggie*VEGPRICE + chicken*CHICKPRICE + beef*BEEFPRICE + chorizo*CHORIZPRICE;

    if (veggie + chicken + beef + chorizo >= 4) {
      return (totalPrice * 80.0)/100.0;
    }
    else {
      return (totalPrice * 100.0)/100.0;
    }
  }
}


