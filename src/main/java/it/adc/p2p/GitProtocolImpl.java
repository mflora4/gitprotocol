package it.adc.p2p;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.time.LocalTime;
import java.util.ArrayList;

import it.adc.p2p.entity.Commit;
import it.adc.p2p.entity.Repository;
import net.tomp2p.dht.FutureGet;
import net.tomp2p.dht.FuturePut;
import net.tomp2p.dht.PeerBuilderDHT;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.futures.FutureBootstrap;
import net.tomp2p.futures.FutureDirect;
import net.tomp2p.p2p.Peer;
import net.tomp2p.p2p.PeerBuilder;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.rpc.ObjectDataReply;
import net.tomp2p.storage.Data;

public class GitProtocolImpl implements GitProtocol {

    final private Peer peer;
	final private PeerDHT _dht;
	final private int DEFAULT_MASTER_PORT = 4000;
    private Repository repository;
	final private int id;
	final private String peer_id;

	public GitProtocolImpl(int _id, String _master_peer, final MessageListener _listener) throws Exception {
		peer = new PeerBuilder(Number160.createHash(_id)).ports(DEFAULT_MASTER_PORT + _id).start();
		_dht = new PeerBuilderDHT(peer).start();
		id = _id;
		peer_id = "peer " + id + ": ";
		
		FutureBootstrap fb = peer.bootstrap().inetAddress(InetAddress.getByName(_master_peer)).ports(DEFAULT_MASTER_PORT).start();
		fb.awaitUninterruptibly();
		if (fb.isSuccess())
			peer.discover().peerAddress(fb.bootstrapTo().iterator().next()).start().awaitUninterruptibly();
		else
			throw new Exception("Error in master peer bootstrap.");
		
		peer.objectDataReply(new ObjectDataReply() {
			public Object reply(PeerAddress sender, Object request) throws Exception {
				return _listener.parseMessage(request);
			}
		});
	}

    @Override
    public boolean createRepository(String _repo_name, File _directory) {
		if (repository == null) {
			repository = new Repository(_directory, _repo_name, _dht.peer().peerAddress(), id);
			return true;
		}
		
		return false;
    }

    @Override
    public boolean addFilesToRepository(String _repo_name, ArrayList<File> files) {
		if (repository.getName().equals(_repo_name))
			return repository.addFiles(files);

		return false;
    }

    @Override
    public boolean commit(String _repo_name, String _message) {
		if (repository.getName().equals(_repo_name))
            return repository.addCommit(id, _message, LocalTime.now());
        
		return false;
    }

    @Override
    public String push(String _repo_name) {
		if (repository == null)
			return peer_id + "repo not created";
		if (!repository.getName().equals(_repo_name))
			return peer_id + "\"" + _repo_name + "\" repo not found";
		
		Repository repo = getRepoFromDHT(_repo_name);
		if (repo == null)
			if (!saveRepoOnDHT(_repo_name, repository))
				return peer_id + "push error";
			else
				return peer_id + "push done";

		int check = repository.checkCommits(repo.getCommits());
		if (check == 0)
			return peer_id + "maybe, you should do the commit";
		else if (check == 1)
			if (!saveRepoOnDHT(_repo_name, repository))
				return peer_id + "push error";
			else
				return peer_id + "push done!";
		else
			return peer_id + "pull is required";
    }

    @Override
    public String pull(String _repo_name) {
        if (repository == null)
			return peer_id + "repo not created";
		if (!repository.getName().equals(_repo_name))
			return peer_id + "\"" + _repo_name + "\" repo not found";
		
		Repository repo = getRepoFromDHT(_repo_name);
		if (repo == null)
			return peer_id + "\"" + _repo_name + "\" repo not found";
		
		int check = repository.checkCommits(repo.getCommits());
		if (check == 0)
			return peer_id + "the repo is already updated";
		else {
			if (!repository.addPeerAddresses(repo.getPeerAddresses()))
				return peer_id + "adding peer addresses error";
			if (!repository.addFiles(repo.getFiles()))
				return peer_id + "adding files error";
			if (!repository.addCommits(repo.getCommits()))
				return peer_id + "adding commits error";
			
			return peer_id + "pull done!";
		}
    }

	private Repository getRepoFromDHT(String _repo_name) {
		try {
			FutureGet futureGet = _dht.get(Number160.createHash(_repo_name)).start();
			futureGet.awaitUninterruptibly();
			if (futureGet.isSuccess()) {
				if (!futureGet.isEmpty())
					return null;

				Repository repo = (Repository) futureGet.dataMap().values().iterator().next().object();
				return repo;
			}
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}
        return null;
	}

	private boolean saveRepoOnDHT(String _repo_name, Repository repo) {
		try {
			FuturePut futurePut = _dht.put(Number160.createHash(_repo_name)).data(new Data(repo)).start();
			futurePut.awaitUninterruptibly();
			if (futurePut.isSuccess()) {
				for (PeerAddress peerAddress : repo.getPeerAddresses()) {
					if (!peerAddress.equals(_dht.peer().peerAddress())) {
						FutureDirect futureDirect = _dht.peer().sendDirect(peerAddress).object("Peer " + id + ": pushed").start();
						futureDirect.awaitUninterruptibly();
					}
				}
				return true;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
        return false;
	}

	public boolean showCommits(String repo_name) {
		if (repository == null)
			return false;
		if (!repository.getName().equals(repo_name))
			return false;

		for (Commit c : repository.getCommits())
			System.out.println(c);

		return true;
	}

	public void leaveNetwork() {
		_dht.peer().announceShutdown().start().awaitUninterruptibly();
	}
    
}