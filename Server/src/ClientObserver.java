/*
 * ClientObserver.java
 * EE422C Final Project submission by
 * Nicholas Taylor
 * ngt333
 * 16160
 * Fall 2020
 */
package finalproject;

import java.io.*;
import java.util.*;

public class ClientObserver extends ObjectOutputStream implements Observer {
    public ClientObserver(OutputStream out) throws IOException {
        super(out);
    }

    @Override
    public void update(Observable o, Object arg){
        try {
            this.reset();
            this.writeObject(arg);
            this.flush();
        } catch (IOException e) { }
    }

}
