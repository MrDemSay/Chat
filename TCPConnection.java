package ru.nova.network;

import sun.awt.X11.XToolkit;

import java.io.*;
import java.net.Socket;
import java.nio.charset.Charset;

public class TCPConnection {

    private final Socket socket;
    private final Thread rxThread; //работа потока
    private final BufferedReader in; //поток ввода
    private final BufferedWriter out; //поток вывода
    private final TCPConnectionListener eventListener;

    public TCPConnection (TCPConnectionListener eventListener, Socket socket) throws IOException { //задекларируем исключение IOExeption
        this.socket = socket;
        this.eventListener = eventListener;
        in = new BufferedReader(new InputStreamReader(socket.getInputStream(), Charset.forName("UTF-8"))); //получение потока ввода с фиксированной кодировкой
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), Charset.forName("UTF-8"))); //получение потока вывода с фиксированной кодировкой

        rxThread = new Thread(new Runnable() {
            /*создание потока для прослушивания соединения и обработка исключения*/
            @Override
            public void run() {
                try{
                    eventListener.onConnectionReady(TCPConnection.this); //передали экземпляр обрамляющего класса
                    while (!rxThread.isInterrupted()) {
                        String msg = in.readLine();
                        eventListener.onReceiveString(TCPConnection.this, msg);
                    }

                } catch (IOException e) {
                    eventListener.onException(TCPConnection.this, e);
                } finally {
                    eventListener.onDisconnect(TCPConnection.this);
                }

            }
        });

        rxThread.start();
    }

    // synchronized - для потокобезопасности
    public synchronized void sendString(String value) {
        try {
            out.write(value + "\r\n"); //перевод строки доавляем специально, т.к. метод write() не видит конец строки
            out.flush(); //сбрасывает все буферы и отправляет
        } catch (IOException e) {
            eventListener.onException(TCPConnection.this, e);
            disconnect();
        }
    }

    public synchronized void disconnect() {
        rxThread.interrupt();
        try {
            socket.close();
        } catch (IOException e) {
            eventListener.onException(TCPConnection.this, e);
        }

    }


    @Override
    public String toString() {
        return "TCPConnection: " + socket.getInetAddress() + ": " + socket.getPort();
    } //текстовая информация о подключениях
}
