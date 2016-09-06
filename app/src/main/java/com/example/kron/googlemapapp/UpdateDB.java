package com.example.kron.googlemapapp;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

/**
 * Created by Kron on 4/11/2016.
 */
public class UpdateDB extends AppCompatActivity{
    Thread m_objThreadClient;
    DatabaseManager DB;
    int port = 3447;
    Socket clientSocket;
    String county, state;
    WifiManager wifi;

    /**
     * UpdateDB constructor. Sets the county we're dealing with, the state, and the context of MainActivity.
     * Uses the context to initiate our Database calls.
     */
    public UpdateDB(String count, String stat, Context con){
        county = count;
        state = stat;
        DB = new DatabaseManager(con);
    }

    protected void onCreate(Bundle SavedInstanceState) {
        super.onCreate(SavedInstanceState);
        wifi = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
    }

    private InetAddress getBroadcastAddress() throws IOException {
        DhcpInfo dhcp = wifi.getDhcpInfo();

        int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
        byte[] quads = new byte[4];
        for (int k = 0; k < 4; k++)
            quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
        return InetAddress.getByAddress(quads);
    }

    public void Start(View view) {
        m_objThreadClient = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    DatagramSocket udpPing = new DatagramSocket();
                    udpPing.setBroadcast(true);
                    String outstr = "Client Req";
                    byte outblock[] = outstr.getBytes();
                    DatagramPacket outpacket = new DatagramPacket(outblock, outblock.length, getBroadcastAddress(), port);
                    udpPing.send(outpacket);

                    byte inblock[] = new byte[256];
                    DatagramPacket inpacket = new DatagramPacket(inblock, inblock.length);
                    udpPing.receive(inpacket);
                    String instr = new String(inpacket.getData(), 0 ,inpacket.getLength());
                    udpPing.close();

                    if(instr.equals("Server Ack")) {
                        clientSocket = new Socket(inpacket.getAddress(), port);
                        clientSocket.setReuseAddress(true);
                        ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream());
                        oos.writeObject("Send now.");

                        String frmSrvr = "";
                        Scanner sc = new Scanner(clientSocket.getInputStream());
                        ArrayList<String> CroixDB = new ArrayList<String>();
                        frmSrvr = sc.nextLine();
                        while (frmSrvr.compareTo("Done") != 0) {
                            CroixDB.add(frmSrvr);
                            frmSrvr = sc.nextLine();
                        }
                        Set<String> AccidentSet = new HashSet<String>(CroixDB);
                        Message clientMessage = Message.obtain();
                        clientMessage.obj = AccidentSet;
                        mHandler.sendMessage(clientMessage);

                        sc.close();
                        clientSocket.close();
                    }
                } catch (IOException e) {
                    System.out.println("Nothing");
                }
            }
        });
        m_objThreadClient.start();
    }

    /**
     * Has the accident list thrown to it. Serves as an in-between between the toDatabase function
     * and the creation of the list in the thread.
     */
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg){
            toDatabase((Set<String>)msg.obj);
        }
    };

    /**
     * Called by the mHandler so that the accident list can be passed out of the new thread.
     */
    public void toDatabase(Set<String> serverMsg){
        //DB.createAccidentDB(serverMsg, county, state);
    }
}
