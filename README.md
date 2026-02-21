# Custom Decentralized P2P Ledger Protocol #

A high-performance, peer-to-peer blockchain protocol built from scratch in Java. This project demonstrates the core principles of distributed systems, cryptographic security, and data persistence without the use of external blockchain frameworks.

# Key Features 

P2P Networking Engine: Built using Java NIO Sockets to allow decentralized nodes to discover peers and synchronize the "Longest Valid Chain" automatically.

ECDSA Cryptography: Implementation of Elliptic Curve Digital Signature Algorithm to ensure non-repudiable transaction authorization.

Custom Mining Engine: Proof-of-Work (PoW) algorithm with adjustable difficulty and Z-score spike detection for network security.

Enterprise Persistence: A binary persistence layer that saves and loads the ledger state from disk, ensuring data durability across node restarts.

Balance Tracking: Real-time auditing of the entire ledger history to prevent double-spending and unauthorized transactions.

# Technical Tech Stack #

Language: Java (JDK 17+)

Cryptography: SHA-256, ECDSA (Elliptic Curve)

Networking: TCP/IP Sockets, Multi-threading

Data Persistence: Java Object Serialization

# Project Structure 

├── src/

│   ├── Blockchain.java    # Core ledger logic, Block & Transaction classes

│   └── P2PNode.java       # P2P Server/Client logic and Wallet management

├── .gitignore             # Prevents binary data from being committed

└── README.md              # Project documentation


# Getting Started

Prerequisites

Java Development Kit (JDK) 17 or higher installed.

Installation & Execution

Clone the repository:

git clone [https://github.com/rezelrex/Java-P2P-Blockchain.git](https://github.com/YOUR_USERNAME/Java-P2P-Blockchain.git)
cd Java-P2P-Blockchain


Compile the source code:

javac src/Blockchain.java src/P2PNode.java


Start the first node (Terminal 1):

java -cp src P2PNode
# Enter port: 8888


Start the second node (Terminal 2):

java -cp src P2PNode
# Enter port: 8889


Synchronize: In Terminal 2, select Option 2 (Connect) and enter 8888. The node will automatically validate and sync the ledger.

📝 Analysis & Learning Outcomes

This project was developed as part of a deep dive into Market Microstructure and Distributed Ledger Technology (DLT). It highlights the challenges of achieving consensus in a decentralized environment and the importance of cryptographic signatures in financial protocols.

Developed by Mohamed Azfar as part of the Fintech & AI specialization at SUTD.
