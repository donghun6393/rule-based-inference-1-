package org.DroolsEngine;

import java.net.URISyntaxException;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
public class SocketIO {
	public static void main(String[] args) throws URISyntaxException {
        Socket socket = IO.socket("http://rnd.dexta.kr:34801/");
        socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                System.out.println("Connected to server!");
            }
        });
        socket.on("time", new Emitter.Listener() {
            
            @Override
            public void call(Object... args) {
                System.out.println("Time: " + args[0]);
            }
            
        });
        socket.connect();
    }
    
}

//private static Socket socket;
//public static void main(String[] args) throws URISyntaxException {
//    socket = IO.socket("http://rnd.dexta.kr:34801/");
//    socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
//        @Override
//        public void call(Object... args) {
//            System.out.println("Connected!");
//        }
//    });
//    socket.connect();
//}