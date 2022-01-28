# Git Protocol
The purpose of this project is to design and develop the Git protocol, distributed versioning control on a P2P network. Each peer can manage its projects (a set of files) using the Git protocol (a minimal version). The system allow the users to create a new repository in a specific folder, add new files to be tracked by the system, apply the changing on the local repository (commit function), push the network's changes, and pull the changing from the network. The git protocol has lot-specific behavior to manage the conflicts; in this version, it is only required that if there are some conflicts, the systems can download the remote copy, and the merge is manually done.

## Technologies
- [Java 7 or greater](https://www.oracle.com/java/technologies/downloads/#jdk17-windows)
- [Apache Maven](https://maven.apache.org/download.cgi)
- An IDE (optional)
- TomP2P
- JUnit
- [Docker](https://www.docker.com/products/docker-desktop)

## Project Structure
Using Maven you can add the dependencies to TomP2P in the pom.xml file.

```
<repositories>
    <repository>
        <id>tomp2p.net</id>
        <url>http://tomp2p.net/dev/mvn/</url>
    </repository>
</repositories>
<dependencies>
    <dependency>
        <groupId>net.tomp2p</groupId>
        <artifactId>tomp2p-all</artifactId>
        <version>5.0-Beta8</version>
    </dependency>
</dependencies>
```

The package `src/main/java/it/adc/p2p` provides four Java classes and an `entity` package.

### Java classes
The four Java classes are:
- *MessageListener*: an interface for listener of messages received by a peer
- *GitProtocol*: an interface that defines the Git protocol communication paradigm
- *GitProtocolImpl*: an implementation of the *GitProtocol* interface that exploits TomP2P library
- *Example*: an example REPL application of a peers network able to communicate using Git protocol

### Entity package
The `entity` package provides two Java classes:
- *Repository*: a class representing the repo
- *Commit*: a class representing the commit
- *SortByTime*: it allows to order commits by time, when they are added to repo

## Other functionalities implemented
- *getRepoFromDHT*: it searches a repo and, if the repo exists, it returns the repo
- *saveRepoOnDHT*: it loads the repo into DHT
- *leaveNetwork*: it allows to a peer leaving the P2P network
- *getFiles*: it gets files that are into the repo
- *getCommits*: it gets commits that are into the repo

## Build this in a Docker container
An example application is provided using Docker container, running on a local machine. See the Dockerfile, for the builing details.

First of all, you can build your docker container:
`docker build --no-cache -t gitprotocol .`

###### Start the master peer
After that, you can start the master peer, in interactive mode (-i) and with two (-e) environment variables:
`docker run -i --name MASTER-PEER -e MASTERIP="127.0.0.1" -e ID=0 gitprotocol`

The MASTERIP environment variable is the master peer ip address and the ID environment variable is the unique id of your peer. Remember you have to run the master peer using the ID=0.

**Note that**: after the first launch, you can launch the master node using the following command: `docker start -i MASTER-PEER`

**Start a generic peer**
When master is started, you have to check up the ip address of your container:
- Check the docker: `docker ps`
- Check the IP address: `docker inspect <container ID>`
Now, you can start your peers varying the unique peer id:
`docker run -i --name PEER-1 -e MASTERIP="172.17.0.2" -e ID=1 gitprotocol`
**Note that**: after the first launch, you can launch this peer node using the following command: `docker start -i PEER-1`