package it.adc.p2p;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

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
	final private int author;
	private boolean commit = false;

	public GitProtocolImpl(int _id, String _master_peer, final MessageListener _listener) throws Exception {
		peer = new PeerBuilder(Number160.createHash(_id)).ports(DEFAULT_MASTER_PORT + _id).start();
		_dht = new PeerBuilderDHT(peer).start();
		author = _id;
		
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
			repository = new Repository(_directory, _repo_name, _dht.peer().peerAddress());
			return true;
		}
		
		return false;
    }

    @Override
    public boolean addFilesToRepository(String _repo_name, ArrayList<File> files) {
		if (repository != null)
			if (repository.getName().equals(_repo_name))
				return repository.addFiles(files);

		return false;
    }

    @Override
    public boolean commit(String _repo_name, String _message) {
		if (repository != null)
			if (repository.getName().equals(_repo_name) && !commit) {
				commit = true;
				return repository.addCommit(author, _message, LocalTime.now());
			}
        
		return false;
    }

    @Override
    public String push(String _repo_name) {
		if (repository == null)
			return "repo not created";
		if (!repository.getName().equals(_repo_name))
			return "\"" + _repo_name + "\" repo not found";
		
		Repository repo = getRepoFromDHT();
		if (repo == null || repository.checkCommits(repo.getCommits())) {
			if (!commit)
				return "you should do a commit";
			
			if (!saveRepoOnDHT())
				return "push error";
			else {
				commit = false;
				return "push done!";
			}
		} else
			return "pull is required";
    }

    @Override
    public String pull(String _repo_name) {
        if (repository == null)
			return "repo not created";
		if (!repository.getName().equals(_repo_name))
			return "\"" + _repo_name + "\" repo not found";
		
		Repository repo = getRepoFromDHT();
		if (repo == null)
			return "\"" + _repo_name + "\" repo not found";
		
		if (repository.checkCommits(repo.getCommits()))
			return "the repo is already updated";
		else {
			if (!repository.addContributors(repo.getContributors()))
				return "adding contributos error in pull";
			if (!repository.addFiles(repo.getFiles()))
				return "adding files error in pull";
			if (!repository.addCommits(repo.getCommits()))
				return "adding commits error in pull";
			
			return "pull done!";
		}
    }

	private Repository getRepoFromDHT() {
		try {
			FutureGet futureGet = _dht.get(Number160.createHash(repository.getName())).start();
			futureGet.awaitUninterruptibly();
			if (futureGet.isSuccess()) {
				Collection<Data> datas = futureGet.dataMap().values();
				if (datas.isEmpty())
					return null;

				return (Repository)futureGet.dataMap().values().iterator().next().object();
			}
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}
        return null;
	}

	private boolean saveRepoOnDHT() {
		try {
			FuturePut futurePut = _dht.put(Number160.createHash(repository.getName())).data(new Data(repository)).start();
			futurePut.awaitUninterruptibly();
			if (futurePut.isSuccess()) {
				for (PeerAddress peerAddress : repository.getContributors()) {
					if (!peerAddress.equals(_dht.peer().peerAddress())) {
						FutureDirect futureDirect = _dht.peer().sendDirect(peerAddress).object("[" + author + "] has just pushed").start();
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

	public HashSet<File> getFiles(String _repo_name) {
		if (repository != null)
			if (repository.getName().equals(_repo_name))
				return repository.getFiles();

		return null;
	}

	public ArrayList<Commit> getCommits(String _repo_name) {
		if (repository != null)
			if (repository.getName().equals(_repo_name))
				return repository.getCommits();

		return null;
	}

	public void leaveNetwork() {
		_dht.peer().announceShutdown().start().awaitUninterruptibly();
	}
    
}