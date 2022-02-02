/*
 * Message.java
 * EE422C Final Project submission by
 * Nicholas Taylor
 * ngt333
 * 16160
 * Fall 2020
 */
package finalproject;

import java.io.Serializable;

public class Message implements Serializable {
    String type;
    Integer status;
    Item updatedItem;

    protected Message(String type, Integer status, Item updatedItem){
        this.type = type;
        this.status = status;
        this.updatedItem = updatedItem;
    }

}
