/*
 * Item.java
 * EE422C Final Project submission by
 * Nicholas Taylor
 * ngt333
 * 16160
 * Fall 2020
 */
package finalproject;

import java.io.Serializable;

/*
 * This class object holds all the information pertaining to a single auction item.
 */
public class Item implements Serializable {
    private final String name;
    private Double currentBid;
    private final Double maxBid;
    private boolean activeListing;

    Item (String name, Double startingBid, Double maxBid, boolean activeListing){
        this.name = name;
        currentBid = startingBid;
        this.maxBid = maxBid;
        this.activeListing = activeListing;
    }

    public String getName() {
        return this.name;
    }

    public Double getCurrentBid() {
        return this.currentBid;
    }

    public void setCurrentBid(Double newBid){
        currentBid = newBid;
    }

    public Double getMaxBid(){
        return maxBid;
    }

    public boolean isItemActive(){
        return activeListing;
    }

    public void setItemActivity(boolean activity){
        activeListing = activity;
    }

    public String getActiveListing(){
        if(activeListing){
            return "Active";
        } else {
            return "Sold";
        }
    }

}
