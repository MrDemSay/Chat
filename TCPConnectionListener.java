// интерфейс для обработки возможных событий возникающий при TCP-соединении
package ru.nova.network;

public interface TCPConnectionListener {
    void onConnectionReady(TCPConnection tcpConnection); //когда соединение уже готово
    void onReceiveString(TCPConnection tcpConnection, String value); //когда приняли входящую строку
    void onDisconnect(TCPConnection tcpConnection); // когда соединение упало
    void onException(TCPConnection tcpConnection, Exception e); //когда возникает исключение
}
