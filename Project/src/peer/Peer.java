package peer;

import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import channel.Channel;
import common.InterfaceRMI;
import protocols.Backup;

public class Peer implements InterfaceRMI {
	
	private static double version;
    private static int server_id;
    private static String peer_ap;
    
    private static Channel MC;
    private static Channel MDB;
    private static Channel MDR;
    
    private static Database database;
    private static Messages msg_forwarder;
        
 
    public static void main(String args[]) throws IOException {
    	
    	initialize(args);
    	

        try {
            Peer obj = new Peer();
            InterfaceRMI stub = (InterfaceRMI) UnicastRemoteObject.exportObject(obj, 0);

            // Bind the remote object's stub in the registry
            Registry registry = LocateRegistry.getRegistry();
            registry.rebind(peer_ap, stub);

            System.err.println("Peer ready");
        } catch (Exception e) {
            System.err.println("Peer exception: " + e.toString());
            e.printStackTrace();
        }
    }

	private static void initialize(String args[]) throws IOException {

		// variable initialization
    	version = Double.parseDouble(args[0]);
        server_id = Integer.parseInt(args[1]);
        peer_ap = args[2];
        
        // communication channels initialization
        MC = new Channel(args[3], args[4]);
        MDB = new Channel(args[5], args[6]);
        MDR = new Channel(args[7], args[8]);
        
        // start channel threads
        new Thread(MC).start();
        new Thread(MDB).start();
        new Thread(MDR).start();

        msg_forwarder = new Messages(version);
       
        database = new Database();
		saveDatabase();
        
        
        // print main info 
        System.out.println("version : " + version);
        System.out.println("server_id : " + server_id);
        System.out.println("access point : " + peer_ap);
        
        System.out.println();
        
        System.out.println("MC  : " + args[3] + " " + args[4]);
        System.out.println("MDB : " + args[5] + " " + args[6]);
        System.out.println("MDR : " + args[7] + " " + args[8]);
        
        System.out.println();
		
	}

	static void saveDatabase() {

		try {
			// saving of object in a file
			FileOutputStream file = new FileOutputStream("peer" + server_id +"_db");
	        ObjectOutputStream out = new ObjectOutputStream(file);
	        
	        // serialization of object
	        out.writeObject(database);
	        
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

        
	}

	@Override
	public void backup(String file_path, int rep_degree) throws RemoteException {
		Backup inititator = new Backup(file_path,rep_degree);
		new Thread(inititator).start();
	}
	
	public static Channel getMDB() {
		return MDB;
	}
	
	public static Messages getMsgForwarder(){
		return msg_forwarder;
	}

	public static int getServerID() {
		return server_id;
	}

}
