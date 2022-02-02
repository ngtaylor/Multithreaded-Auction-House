/*
 * Client.java
 * EE422C Final Project submission by
 * Nicholas Taylor
 * ngt333
 * 16160
 * Fall 2020
 */
package finalproject;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.*;
import java.io.*;
import java.net.*;
import java.util.*;


public class Client extends Application {
	// I/O streams 
	ObjectOutputStream toServer = null;
	ObjectInputStream fromServer = null;

	//Map of current auction listings
	HashMap<String, Item> clientsideAuction = new HashMap<>();

	//JavaFX scene elements used in action methods and when updating GUI
	private TableView listings = new TableView<>();
	private TableColumn<Item, String> itemNameColumn = new TableColumn<>("Item Name");
	private TableColumn<Item, Double> currentBidColumn = new TableColumn<>("Current Bid ($)");
	private TableColumn<Item, Double> buyNowColumn = new TableColumn<>("Buy Now Price ($)");
	private TableColumn<Item, String> statusColumn = new TableColumn<>("Status");

	private ComboBox<String> itemSelector = new ComboBox<>();
	private TextField bidBox = new TextField();
	private Label clientMessage = new Label("Waiting for Bid to be Placed...");

	@Override
	public void start(Stage primaryStage) {
		VBox mainPane = new VBox(15);
		BorderPane topPane = new BorderPane();

		VBox midBox = new VBox(30);
			HBox topBoxBox = new HBox(5);
				HBox midCentBox = new HBox();
				VBox buttonBox = new VBox(10);
			BorderPane botBoxPane = new BorderPane();

		BorderPane lowPane = new BorderPane();

		mainPane.getChildren().addAll(topPane, midBox, lowPane);
		midBox.getChildren().addAll(topBoxBox, botBoxPane);

		//Top of GUI
		itemNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
		itemNameColumn.setMinWidth(150);
		itemNameColumn.setMaxWidth(150);

		currentBidColumn.setCellValueFactory(new PropertyValueFactory<>("currentBid"));
		currentBidColumn.setMinWidth(120);
		currentBidColumn.setMaxWidth(120);

		buyNowColumn.setCellValueFactory(new PropertyValueFactory<>("maxBid"));
		buyNowColumn.setMinWidth(120);
		buyNowColumn.setMaxWidth(120);

		statusColumn.setCellValueFactory(new PropertyValueFactory<>("activeListing"));
		statusColumn.setMinWidth(110);
		statusColumn.setMaxWidth(110);
		listings.getColumns().addAll(itemNameColumn, currentBidColumn, buyNowColumn, statusColumn);
		topPane.setCenter(listings);

		//Middle of GUI
		itemSelector.setValue("Choose Item");
		itemSelector.setMinWidth(170);

		bidBox.setEditable(true);
		bidBox.setMinWidth(100);
		Label bidBoxLbl = new Label("Bid: $");
		midCentBox.getChildren().addAll(bidBoxLbl, bidBox);

		Button placeBidBut = new Button();
		placeBidBut.setText("Place Bid");
		Label orLbl = new Label("OR");
		Button buyNow = new Button();
		buyNow.setText("Buy Now");
		buttonBox.getChildren().addAll(placeBidBut, orLbl, buyNow);

		topBoxBox.getChildren().addAll(itemSelector, midCentBox, buttonBox);
		botBoxPane.setCenter(clientMessage);

		//Bottom of GUI
		Button quitButton = new Button();
		quitButton.setText("Quit");
		lowPane.setLeft(quitButton);

		// Create a scene and place it in the stage 
		Scene scene = new Scene(mainPane, 500, 600);

		placeBidBut.setOnAction(e -> {
			try {
				String bidName = itemSelector.getValue();
				Double bidValue = Double.parseDouble(bidBox.getText());
				Item itemCopy = clientsideAuction.get(bidName);
				Item bid = new Item(itemCopy.getName(), bidValue, itemCopy.getMaxBid(), itemCopy.isItemActive());
				toServer.reset();
				toServer.writeObject(bid);
				toServer.flush();
			} catch (IOException ioException) { }
		});

		buyNow.setOnAction(e -> {
			try {
				String bidName = itemSelector.getValue();
				Item itemCopy = clientsideAuction.get(bidName);
				Item bid = new Item(itemCopy.getName(), itemCopy.getMaxBid(), itemCopy.getMaxBid(), itemCopy.isItemActive());
				toServer.reset();
				toServer.writeObject(bid);
				toServer.flush();
			} catch (IOException ioException) { }
		});

		quitButton.setOnAction(e -> {
			System.exit(0);
		});

		try { 
			// Create a socket to connect to the server 
			@SuppressWarnings("resource")
			Socket socket = new Socket("localhost", 5000);

			System.out.println("connection established"); //Test Line

			// Create an input stream to receive data from the server 
			fromServer = new ObjectInputStream(socket.getInputStream()); 

			// Create an output stream to send data to the server 
			toServer = new ObjectOutputStream(socket.getOutputStream());

			//Receive initial HashMap of auction items from server before running readerThread
			HashMap<String, Item> currentAuctionItems = (HashMap<String, Item>) fromServer.readObject();
			clientsideAuction.putAll(currentAuctionItems);

			//Initialize Table and Combo Box with newly populated HashMap
			updateTable();
			for(Item i : clientsideAuction.values()){
				itemSelector.getItems().add(i.getName());
			}
		} 
		catch (IOException | ClassNotFoundException ex) {
			ex.printStackTrace();
		}

		Thread readerThread = new Thread(new Runnable() {
			Integer status;

			@Override
			public void run() {

				Runnable clientUpdate = new Runnable() {
					@Override
					public void run() {
						updateClientMessage(status);
					}
				};

				while (true) {
					boolean isStatus = false;
					try {
						Message message = (Message) fromServer.readObject();
						isStatus = processRequest(message);
						status = message.status;
					} catch (IOException | ClassNotFoundException e) {
						e.printStackTrace();
					}
					if(isStatus) {
						Platform.runLater(clientUpdate);
					}
				}
			}
		});

		readerThread.start();

		primaryStage.setTitle("Auction Client"); // Set the stage title
		primaryStage.setScene(scene); // Place the scene in the stage
		primaryStage.show(); // Display the stage

	}

	private boolean processRequest(Message message){
		if(message.type.equals("Integer")){
			return true;
		} else if(message.type.equals("Item")){
			clientsideAuction.replace(message.updatedItem.getName(), message.updatedItem);
			updateTable();
			return false;
		}
		return false;
	}

	private void updateClientMessage(Integer status){
		switch (status){
			case 0: clientMessage.setText("Bid Accepted!");
				break;
			case 1: clientMessage.setText("Bid not accepted: value too low.");
				break;
			case 2: clientMessage.setText("Sorry, bidding for this item is closed.");
				break;
			case 3: clientMessage.setText("Congratulations! You have won the auction on this item!");
				break;
		}
	}

	private void updateTable() {
		listings.getItems().clear();
		for(Item i : clientsideAuction.values()){
			listings.getItems().add(clientsideAuction.get(i.getName()));
		}
	}

	public static void main(String[] args) {
		launch(args);
	}
}