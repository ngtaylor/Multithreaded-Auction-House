# Multithreaded-Auction-House

Multithreaded Auction House created using Java

Author: Nick Taylor

## Clients are able to:
 - Interact with the auction house using a GUI
 - View current items up for auction
 - Place a bid on an item or buy it now for the "Buy Now" price
 - Have a live update of the current auction market
 - Notification to the winner of an item and to other potential buyers that the item has sold

## The Auction House Server:
 - Uses an observable design pattern to interact with multiple clients
 - Uses an observer to update clients whenever the server's information is updated
 - Server waits for a bid, checks for validity, sends the validity status back to the client, then finally if valid updates the Auction House and 
   updates all the clients of the new change
 - This process is synchronized to ensure a fair first come first serve bidding process

