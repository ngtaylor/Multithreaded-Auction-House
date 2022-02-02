/*
 * Server.java
 * EE422C Final Project submission by
 * Nicholas Taylor
 * ngt333
 * 16160
 * Fall 2020
 */
package finalproject;

import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.Thread;

public class Server extends Observable {
    static Server server;
    static AuctionHouse auctionHouse;

    public static void main (String [] args) {
        server = new Server();
        auctionHouse = new AuctionHouse("items.txt");
        server.SetupNetworking();
    }

    //Server waits for new clients to connect, and then redirects them to a ClientHandler thread
    private void SetupNetworking() {
        int port = 5000;
        try {
            ServerSocket ss = new ServerSocket(port);
            while (true) {
                Socket clientSocket = ss.accept();
                ClientObserver writer = new ClientObserver(clientSocket.getOutputStream());
                Thread t = new Thread(new ClientHandler(clientSocket, writer));
                t.start();
                addObserver(writer);
                System.out.println("got a connection");
            }
        } catch (IOException e) {}
    }

    class ClientHandler implements Runnable {
        private ObjectInputStream reader;
        private  ClientObserver writer;
        Socket clientSocket;

        public ClientHandler(Socket clientSocket, ClientObserver writer) {
            try {
                this.clientSocket = clientSocket;
                this.writer = writer;
                reader = new ObjectInputStream(clientSocket.getInputStream());
            } catch(IOException e){ }
        }

        public void run() {
            try {
                //send all items to client for initialization before they can start bidding
                HashMap<String, Item> allItems = auctionHouse.getAllItemsMap();
                writer.writeObject(allItems);
                while (true) {

                    Item newBid = (Item) reader.readObject();

                    synchronized (server) {
                        Item currentItem = auctionHouse.getItem(newBid.getName());

                        //Depending on what the bid was, the server determines the validity of the bid and sends that back to Client
                        Integer status;
                        if (!currentItem.isItemActive()) {
                            status = 2;
                        } else if (newBid.getCurrentBid() <= currentItem.getCurrentBid()) {
                            status = 1;
                        } else if (newBid.getCurrentBid() >= currentItem.getMaxBid()) {
                            status = 3;
                            newBid.setItemActivity(false);
                        } else {
                            status = 0;
                        }
                        Message statusMessage = new Message("Integer", status, null);
                        writer.reset();
                        writer.writeObject(statusMessage);
                        writer.flush();

                        //If valid bid, update AuctionHouse and send updatedItem to all clients
                        if (status.equals(0) || status.equals(3)) {
                            auctionHouse.updateItem(newBid);
                            Message itemMessage = new Message("Item", null, newBid);
                            setChanged();
                            notifyObservers(itemMessage);
                        }
                    }
                }
            } catch (IOException | ClassNotFoundException e){ }
        }
    } // end of class ClientHandler

}