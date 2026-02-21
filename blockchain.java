import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;

/**
 * PROJECT 4: DECENTRALIZED LEDGER PROTOCOL
 * File: blockchain.java
 * Updated with Persistence (Save/Load to Disk)
 */

class Transaction implements Serializable {
    private static final long serialVersionUID = 1L;
    public String sender;      
    public String recipient;
    public double amount;
    public byte[] signature;   

    public Transaction(String sender, String recipient, double amount) {
        this.sender = sender;
        this.recipient = recipient;
        this.amount = amount;
    }

    public String getData() {
        return sender + recipient + Double.toString(amount);
    }

    public boolean verifySignature() {
        if (sender.equals("Network")) return true;
        if (signature == null) return false;

        try {
            byte[] publicBytes = Base64.getDecoder().decode(sender);
            KeyFactory keyFactory = KeyFactory.getInstance("EC");
            PublicKey pubKey = keyFactory.generatePublic(new java.security.spec.X509EncodedKeySpec(publicBytes));

            Signature ecdsaVerify = Signature.getInstance("SHA256withECDSA");
            ecdsaVerify.initVerify(pubKey);
            ecdsaVerify.update(getData().getBytes(StandardCharsets.UTF_8));
            return ecdsaVerify.verify(signature);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String toString() {
        String shortSender = sender.length() > 10 ? sender.substring(0, 10) : sender;
        return shortSender + "... sent " + amount + " to " + recipient;
    }
}

class Block implements Serializable {
    private static final long serialVersionUID = 1L;
    public String hash;
    public String previousHash;
    public List<Transaction> transactions;
    public long timeStamp;
    public int nonce;

    public Block(String previousHash, List<Transaction> transactions) {
        this.previousHash = previousHash;
        this.transactions = transactions;
        this.timeStamp = new Date().getTime();
        this.hash = calculateHash();
    }

    public String calculateHash() {
        String dataToHash = previousHash + Long.toString(timeStamp) + Integer.toString(nonce) + transactions.toString();
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(dataToHash.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    public void mineBlock(int difficulty) {
        String target = new String(new char[difficulty]).replace('\0', '0');
        while (!hash.substring(0, difficulty).equals(target)) {
            nonce++;
            hash = calculateHash();
        }
    }
}

public class blockchain {
    public static List<Block> blockchain = new ArrayList<>();
    public static int difficulty = 4;

    public static double getBalance(String address) {
        double balance = 100.0; 
        for (Block b : blockchain) {
            for (Transaction tx : b.transactions) {
                if (tx.sender.equals(address)) balance -= tx.amount;
                if (tx.recipient.equals(address)) balance += tx.amount;
            }
        }
        return balance;
    }

    public static boolean addBlock(Block newBlock) {
        for (Transaction tx : newBlock.transactions) {
            if (!tx.verifySignature()) return false;
            if (!tx.sender.equals("Network") && getBalance(tx.sender) < tx.amount) return false;
        }
        newBlock.mineBlock(difficulty);
        blockchain.add(newBlock);
        return true;
    }

    public static Boolean isChainValid(List<Block> chain) {
        if (chain == null || chain.isEmpty()) return true;
        String hashTarget = new String(new char[difficulty]).replace('\0', '0');
        for (int i = 1; i < chain.size(); i++) {
            Block currentBlock = chain.get(i);
            if (!currentBlock.hash.equals(currentBlock.calculateHash())) return false;
            if (!currentBlock.previousHash.equals(chain.get(i - 1).hash)) return false;
            if (!currentBlock.hash.substring(0, difficulty).equals(hashTarget)) return false;
            for(Transaction tx : currentBlock.transactions) if(!tx.verifySignature()) return false;
        }
        return true;
    }

    // --- PERSISTENCE METHODS ---

    public static void saveChain(String filename) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
            oos.writeObject(blockchain);
            System.out.println("[Disk] Blockchain successfully saved to " + filename);
        } catch (IOException e) {
            System.err.println("[Error] Failed to save blockchain: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public static void loadChain(String filename) {
        File file = new File(filename);
        if (!file.exists()) return;

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
            List<Block> loaded = (List<Block>) ois.readObject();
            if (isChainValid(loaded)) {
                blockchain = loaded;
                System.out.println("[Disk] Valid blockchain loaded from disk. Size: " + loaded.size());
            }
        } catch (Exception e) {
            System.err.println("[Error] Failed to load blockchain: " + e.getMessage());
        }
    }
}