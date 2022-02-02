/*
 * AuctionHouse.java
 * EE422C Final Project submission by
 * Nicholas Taylor
 * ngt333
 * 16160
 * Fall 2020
 */
package finalproject;

import java.util.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/*
 * This class holds a static HashMap of the current up to date items being auctions, and initializes this Map
 * by reading the item data in from a text file.
 */
public class AuctionHouse {

    private static HashMap<String, Item> items = new HashMap<>();

    AuctionHouse (String fileName){
        try {
            //Reads all item names and the starting and max bids for each, then creates a hashmap to store this data
            File itemFile = new File(fileName);
            Scanner reader = new Scanner(itemFile);
            ArrayList<String> itemNames = new ArrayList<>();
            ArrayList<Double> startingBids = new ArrayList<>();
            ArrayList<Double> maxBids = new ArrayList<>();
            int lineCount = 0;
            while(reader.hasNextLine()){
                lineCount++;
                switch (lineCount) {
                    case 1: itemNames.add(reader.nextLine());
                            break;
                    case 2: startingBids.add(Double.parseDouble(reader.nextLine()));
                            break;
                    case 3: maxBids.add(Double.parseDouble(reader.nextLine()));
                            lineCount = 0;
                            break;
                }
            }
            //Create items HashMap
            for(int i = 0; i < itemNames.size(); i++){
                String itemName;
                Double itemStartBid, itemMaxBid;
                itemName = itemNames.get(i);
                itemStartBid = startingBids.get(i);
                itemMaxBid = maxBids.get(i);
                Item nextItem = new Item(itemName, itemStartBid, itemMaxBid, true);
                items.put(itemName, nextItem);
            }
        } catch (FileNotFoundException e) {
            System.out.println("Items file not found");
        }
    }

    public Item getItem(String itemName){
        return items.get(itemName);
    }

    public void updateItem(Item updatedItem){
        items.replace(updatedItem.getName(), updatedItem);
    }

    public HashMap<String, Item> getAllItemsMap(){
        return items;
    }

}
