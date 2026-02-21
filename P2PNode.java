import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.*;

/**
 * PHASE 6: DISK PERSISTENCE
 * File: P2PNode.java
 */
public class P2PNode {
    private int port;
    private List<Block> localChain;
    private PrivateKey privateKey;
    public String publicKey;
    private static final String DATA_FILE = "blockchain_data.bin";

    public P2PNode(int port) {
        this.port = port;
        // Load existing chain from disk on startup
        blockchain.loadChain(DATA_FILE);
        this.localChain = blockchain.blockchain;
        generateWallet();
    }

    private void generateWallet() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC");
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            keyGen.initialize(256, random);
            KeyPair keyPair = keyGen.generateKeyPair();
            this.privateKey = keyPair.getPrivate();
            this.publicKey = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
            System.out.println("[Wallet] Address: " + publicKey.substring(0, 15) + "...");
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    public void signTransaction(Transaction tx) {
        try {
            Signature dsa = Signature.getInstance("SHA256withECDSA");
            dsa.initSign(privateKey);
            dsa.update(tx.getData().getBytes(StandardCharsets.UTF_8));
            tx.signature = dsa.sign();
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    public void startServer() {
        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(port)) {
                System.out.println("[System] Node listening on port: " + port);
                while (true) {
                    Socket socket = serverSocket.accept();
                    new Thread(new PeerHandler(socket)).start();
                }
            } catch (IOException e) { e.printStackTrace(); }
        }).start();
    }

    public void connectToPeer(String host, int peerPort) {
        try (Socket socket = new Socket(host, peerPort);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
            out.writeObject("GET_CHAIN");
            List<Block> receivedChain = (List<Block>) in.readObject();
            handleReceivedChain(receivedChain);
        } catch (Exception e) {
            System.out.println("[System] Failed to connect to port " + peerPort);
        }
    }

    private synchronized void handleReceivedChain(List<Block> receivedChain) {
        if (blockchain.isChainValid(receivedChain) && receivedChain.size() > localChain.size()) {
            System.out.println("[Sync] Syncing to longer valid chain...");
            localChain.clear();
            localChain.addAll(receivedChain);
            blockchain.saveChain(DATA_FILE); // Save synced chain to disk
        }
    }

    private class PeerHandler implements Runnable {
        private Socket socket;
        public PeerHandler(Socket socket) { this.socket = socket; }
        @Override
        public void run() {
            try (ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                 ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {
                String request = (String) in.readObject();
                if ("GET_CHAIN".equals(request)) out.writeObject(new ArrayList<>(localChain));
            } catch (Exception e) { }
        }
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter port: ");
        int myPort = sc.nextInt();
        
        P2PNode node = new P2PNode(myPort);
        node.startServer();

        while (true) {
            System.out.println("\n1. Mine Block | 2. Connect | 3. View Chain | 4. Check Balance | 5. Save & Exit");
            int choice = sc.nextInt();
            
            if (choice == 1) {
                double balance = blockchain.getBalance(node.publicKey);
                System.out.println("[Wallet] Balance: " + balance);
                System.out.print("Recipient: "); String rec = sc.next();
                System.out.print("Amount: "); double amt = sc.nextDouble();

                if (balance >= amt || blockchain.blockchain.isEmpty()) {
                    Transaction tx = new Transaction(node.publicKey, rec, amt);
                    node.signTransaction(tx);
                    if (blockchain.addBlock(new Block(blockchain.blockchain.isEmpty() ? "0" : blockchain.blockchain.get(blockchain.blockchain.size()-1).hash, Arrays.asList(tx)))) {
                        System.out.println("[Success] Block mined.");
                        blockchain.saveChain(DATA_FILE); // Auto-save after mining
                    }
                } else {
                    System.out.println("[Error] Insufficient funds!");
                }
            } else if (choice == 2) {
                System.out.print("Enter peer port: ");
                node.connectToPeer("localhost", sc.nextInt());
            } else if (choice == 3) {
                for (Block b : blockchain.blockchain) {
                    System.out.println("Hash: " + b.hash.substring(0,10) + "... | Transactions: " + b.transactions.size());
                }
            } else if (choice == 4) {
                System.out.println("Address: " + node.publicKey);
                System.out.println("Current Balance: " + blockchain.getBalance(node.publicKey));
            } else if (choice == 5) {
                blockchain.saveChain(DATA_FILE);
                System.exit(0);
            }
        }
    }
}