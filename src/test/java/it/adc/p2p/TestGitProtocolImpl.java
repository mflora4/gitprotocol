package it.adc.p2p;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.util.ArrayList;

import org.junit.After;
import org.junit.Test;

public class TestGitProtocolImpl {

    protected GitProtocolImpl peer0, peer1;
    private static final String _REPO_NAME = "repo";
    private static final File _DIRECTORY = new File("dir");

    public TestGitProtocolImpl() throws Exception {

		class MessageListenerImpl implements MessageListener{

			public MessageListenerImpl(int peerid) {
				this.peerid = peerid;
			}

			public Object parseMessage(Object obj) {
				System.out.println("[" + peerid + "] (Direct Message Received) " + obj);
				return "success";
			}

            int peerid;
			
		}

		 peer0 = new GitProtocolImpl(0, "127.0.0.1", new MessageListenerImpl(0));	
		 peer1 = new GitProtocolImpl(1, "127.0.0.1", new MessageListenerImpl(1));
		
	}
    
    /**
     * TEST CASE 1: createRepository
     * @result it creates a repo
    */
    @Test
    public void createRepository1() {
        assertTrue(peer0.createRepository(_REPO_NAME, _DIRECTORY));
        System.out.println("repo created");
    }

    /**
     * TEST CASE 2: createRepository
     * @result it tries to duplicate the repo
    */
    @Test
    public void createRepository2() {
        peer0.createRepository(_REPO_NAME, _DIRECTORY);
        assertFalse(peer0.createRepository(_REPO_NAME, _DIRECTORY));
        System.out.println("the repo already exists");
    }

    /**
     * TEST CASE 3: addFilesToRepository
     * @result it adds files to the repo
     */
    @Test
    public void addFilesToRepository1() {
        peer0.createRepository(_REPO_NAME, _DIRECTORY);
        
        ArrayList<File> files = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            File f = new File("file" + (i + 1));
            files.add(f);
        }

        assertTrue(peer0.addFilesToRepository(_REPO_NAME, files));
        System.out.println("files added");
    }

    /**
     * TEST CASE 4: addFilesToRepository
     * @result it tries to add files to not created repo
     */
    @Test
    public void addFilesToRepository2() {
        ArrayList<File> files = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            File f = new File("file" + (i + 1));
            files.add(f);
        }

        assertFalse(peer0.addFilesToRepository(_REPO_NAME, files));
        System.out.println("repo not created");
    }

    /**
     * TEST CASE 5: addFilesToRepository
     * @result it tries to add files to not existing repo
     */
    @Test
    public void addFilesToRepository3() {
        peer0.createRepository(_REPO_NAME, _DIRECTORY);
        
        ArrayList<File> files = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            File f = new File("file" + (i + 1));
            files.add(f);
        }

        assertFalse(peer0.addFilesToRepository("_REPO_NAME", files));
        System.out.println("\"_REPO_NAME\" repo doesn't exist");
    }

    /**
     * TEST CASE 6: commit
     * @result it commits to the repo
     */
    @Test
    public void commit1() {
        peer0.createRepository(_REPO_NAME, _DIRECTORY);
        assertTrue(peer0.commit(_REPO_NAME, "default-commit"));
        System.out.println("commit added");
    }

    /**
     * TEST CASE 7: commit
     * @result it tries to commit to not created repo
     */
    @Test
    public void commit2() {
        assertFalse(peer0.commit(_REPO_NAME, "default-commit"));
        System.out.println("repo not created");
    }

    /**
     * TEST CASE 8: commit
     * @result it tries to commit to not existing repo
     */
    @Test
    public void commit3() {
        peer0.createRepository(_REPO_NAME, _DIRECTORY);
        assertFalse(peer0.commit("_REPO_NAME", "default-commit"));
        System.out.println("\"_REPO_NAME\" repo doesn't exist");
    }

    /**
     * TEST CASE 9: commit
     * @result it tries to add another commit, before it pushes
     */
    @Test
    public void commit4() {
        peer0.createRepository(_REPO_NAME, _DIRECTORY);
        assertTrue(peer0.commit(_REPO_NAME, "default-commit"));
        assertFalse(peer0.commit(_REPO_NAME, "default-commit"));
        System.out.println("before adding another commit, perform a push");
    }

    /**
     * TEST CASE 10: push
     * @result it pushes
     */
    @Test
    public void push1() {
        final String push = "push done!";
        peer0.createRepository(_REPO_NAME, _DIRECTORY);
        peer0.commit(_REPO_NAME, "default-commit");
        assertEquals(peer0.push(_REPO_NAME), push);
        System.out.println(push);
    }

    /**
     * TEST CASE 11: push
     * @result it tries to push to not created repo
     */
    @Test
    public void push2() {
        final String push = "repo not created";
        assertEquals(peer0.push(_REPO_NAME), push);
        System.out.println(push);
    }

    /**
     * TEST CASE 12: push
     * @result it tries to push to not existing repo
     */
    @Test
    public void push3() {
        final String push = "\"_REPO_NAME\" repo not found";
        peer0.createRepository(_REPO_NAME, _DIRECTORY);
        assertEquals(peer0.push("_REPO_NAME"), push);
        System.out.println(push);
    }

    /**
     * TEST CASE 13: push
     * @result it tries to push, before it commits
     */
    @Test
    public void push4() {
        final String push = "you should do a commit";
        peer0.createRepository(_REPO_NAME, _DIRECTORY);
        assertEquals(peer0.push(_REPO_NAME), push);
        System.out.println(push);
    }

    /**
     * TEST CASE 14: push
     * @result peer0 pushes, while peer1 tries to push, before it pulls
     */
    @Test
    public void push5() {
        final String push = "pull is required";
        peer0.createRepository(_REPO_NAME, _DIRECTORY);
        peer1.createRepository(_REPO_NAME, _DIRECTORY);
        peer0.commit(_REPO_NAME, "default-commit");
        peer0.push(_REPO_NAME);
        assertEquals(peer1.push(_REPO_NAME), push);
        System.out.println(push);
    }

    /**
     * TEST CASE 15: pull
     * @result peer0 pushes and peer1 pulls
     */
    @Test
    public void pull1() {
        final String pull = "pull done!";
        peer0.createRepository(_REPO_NAME, _DIRECTORY);
        peer1.createRepository(_REPO_NAME, _DIRECTORY);

        ArrayList<File> files = new ArrayList<>();
        files.add(new File("file"));

        peer0.addFilesToRepository(_REPO_NAME, files);
        peer0.commit(_REPO_NAME, "default-commit");
        peer0.push(_REPO_NAME);
        assertEquals(peer1.pull(_REPO_NAME), pull);
        System.out.println(pull);
    }

    /**
     * TEST CASE 16: pull
     * @result it tries to pull from a repo not created
     */
    @Test
    public void pull2() {
        final String pull = "repo not created";
        assertEquals(peer0.pull(_REPO_NAME), pull);
        System.out.println(pull);
    }

    /**
     * TEST CASE 17: pull
     * @result it tries to pull from not existing repo
     */
    @Test
    public void pull3() {
        final String pull = "\"_REPO_NAME\" repo not found";
        peer0.createRepository(_REPO_NAME, _DIRECTORY);
        assertEquals(peer0.pull("_REPO_NAME"), pull);
        System.out.println(pull);
    }

    /**
     * TEST CASE 18: pull
     * @result it tries to pull from repo not existing remotely
     */
    @Test
    public void pull4() {
        final String pull = "\"" + _REPO_NAME + "\" repo not found";
        peer0.createRepository(_REPO_NAME, _DIRECTORY);
        assertEquals(peer0.pull(_REPO_NAME), pull);
        System.out.println(pull);
    }

    /**
     * TEST CASE 19: pull
     * @result first it push and then it pulls
     */
    @Test
    public void pull5() {
        final String pull = "the repo is already updated";
        peer0.createRepository(_REPO_NAME, _DIRECTORY);
        peer0.commit(_REPO_NAME, "default-commit");
        peer0.push(_REPO_NAME);
        assertEquals(peer0.pull(_REPO_NAME), pull);
        System.out.println(pull);
    }

    /**
     * TEST CASE 20: getFiles
     * @result it adds files to repository and then it gets them
     */
    @Test
    public void getFiles1() {
        peer0.createRepository(_REPO_NAME, _DIRECTORY);
        
        ArrayList<File> files = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            File f = new File("file" + (i + 1));
            files.add(f);
        }

        peer0.addFilesToRepository(_REPO_NAME, files);
        assertNotNull(peer0.getFiles(_REPO_NAME));
        System.out.println("files presents");
    }

    /**
     * TEST CASE 21: getFiles
     * @result it tries to get files from repo not created
     */
    @Test
    public void getFiles2() {
        assertNull(peer0.getFiles(_REPO_NAME));
        System.out.println("repo not created");
    }

    /**
     * TEST CASE 22: getFiles
     * @result it tries to get files from not existing repo
     */
    @Test
    public void getFiles3() {
        peer0.createRepository(_REPO_NAME, _DIRECTORY);
        assertNull(peer0.getFiles("_REPO_NAME"));
        System.out.println("\"_REPO_NAME\" repo not existing");
    }

    /**
     * TEST CASE 23: getCommits
     * @result it commits and gets commits
     */
    @Test
    public void getCommits1() {
        peer0.createRepository(_REPO_NAME, _DIRECTORY);
        peer0.commit(_REPO_NAME, "default-commit");
        assertNotNull(peer0.getCommits(_REPO_NAME));
        System.out.println("commits presents");
    }

    /**
     * TEST CASE 24: getCommits
     * @result it tries to get commits from not creating repo
     */
    @Test
    public void getCommits2() {
        assertNull(peer0.getCommits(_REPO_NAME));
        System.out.println("repo not creating");
    }

    /**
     * TEST CASE 25: getCommits
     * @result it tries to get commits from not existing repo
     */
    @Test
    public void getCommits3() {
        peer0.createRepository(_REPO_NAME, _DIRECTORY);
        assertNull(peer0.getCommits("_REPO_NAME"));
        System.out.println("\"_REPO_NAME\" repo not found");
    }

    @After
    public void leaveNetwork() {
        peer0.leaveNetwork();
        peer1.leaveNetwork();
    }

}
