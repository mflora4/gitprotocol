package it.adc.p2p;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.beryx.textio.TextIO;
import org.beryx.textio.TextIoFactory;
import org.beryx.textio.TextTerminal;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import it.adc.p2p.entity.Commit;

public class Example {

    @Option(name = "-m", aliases = "--masterip", usage = "the master peer ip address", required = true)
	private static String master;

	@Option(name = "-id", aliases = "--identifierpeer", usage = "the unique identifier for this peer", required = true)
	private static int id;

    public static void main(String[] args) throws Exception {
        
        class MessageListenerImpl implements MessageListener{

			public MessageListenerImpl(int peerid) {
				this.peerid = peerid;
			}

			public Object parseMessage(Object obj) {
				TextIO textIO = TextIoFactory.getTextIO();
				TextTerminal terminal = textIO.getTextTerminal();
				terminal.printf("\n[" + peerid + "] (Direct Message Received) " + obj + "\n\n");
				return "success";
			}

            int peerid;

		}

        Example example = new Example();
		final CmdLineParser parser = new CmdLineParser(example);
        try {
            parser.parseArgument(args);
			TextIO textIO = TextIoFactory.getTextIO();
			TextTerminal terminal = textIO.getTextTerminal();
            GitProtocolImpl peer = new GitProtocolImpl(id, master, new MessageListenerImpl(id));

            File dir = new File(id + "");
            HashSet<File> files;

            terminal.printf("\nStaring peer id: %d on master node: %s\n", id, master);
            while (true) {
                printMenu(terminal);

                int option = textIO.newIntInputReader().withMinVal(1).withMaxVal(9).read("Option");
                String name;
                switch (option) {
                    case 1:
                        terminal.printf("\nENTER REPO NAME\n");
                        name = textIO.newStringInputReader()
					            .withDefaultValue("default-repo")
                                .read("Repo name:");
                        if (!peer.createRepository(name, dir))
                            terminal.printf("\n\"%s\" REPO ALREADY CREATED\n", name);
                        else
                            terminal.printf("\n\"%s\" REPO SUCCESFULLY CREATED\n", name);
                        break;
                    case 2:
                        terminal.printf("\nENTER REPOSITORY NAME\n");
                        name = textIO.newStringInputReader()
                                .withDefaultValue("default-repo")
                                .read("Repo name:");
                        ArrayList<File> filesToCreate = createFiles(terminal, textIO);
                        if (!peer.addFilesToRepository(name, filesToCreate))
                            terminal.printf("\nERROR IN ADDING FILES TO REPO\n");
                        else
                            terminal.printf("\nFILES ADDED TO REPO\n", name);
                        break;
                    case 3:
                        if (peer.getRepository() == null)
                            terminal.printf("\nFIRST OF ALL, CREATE A REPO\n");
                        else {
                            files = (HashSet<File>) peer.getRepository().getFiles().clone();
                            if (files.isEmpty())
                                terminal.printf("\nNO FILE\n");
                            else {
                                terminal.printf("\nENTER REPOSITORY NAME\n");
                                name = textIO.newStringInputReader()
                                        .withDefaultValue("default-repo")
                                        .read("Repo name:");
                                terminal.printf("\nDO YOU WANT TO REMOVE ALL FILES?\n");
                                boolean all = textIO.newBooleanInputReader().withDefaultValue(false).read("all files?");
                                if (all)
                                    files.clear();
                                else
                                    files = removeFiles(terminal, textIO, files);
                                if (!peer.removeFilesFromRepository(name, files))
                                    terminal.printf("\nERROR IN REMOVING FILES FROM REPO\n");
                                else
                                    terminal.printf("\nFILES REMOVED FROM REPO\n", name);
                            }
                        }
                        break;
                    case 4:
                        terminal.printf("\nENTER REPOSITORY NAME\n");
                        name = textIO.newStringInputReader()
                                .withDefaultValue("default-repo")
                                .read("Repo name:");
                        String message = textIO.newStringInputReader()
                                .withDefaultValue("default-commit")
                                .read("Commit message:");
                        if (!peer.commit(name, message))
                            terminal.printf("\nERROR IN COMMIT\n");
                        else
                            terminal.printf("\nCOMMIT SENT\n");
                        break;
                    case 5:
                        terminal.printf("\nENTER REPO NAME\n");
                        name = textIO.newStringInputReader()
                                .withDefaultValue("default-repo")
                                .read("Repo name:");
                        String push = peer.push(name);
                        terminal.printf("\n" + push.toUpperCase() + "\n");
                        break;
                    case 6:
                        terminal.printf("\nENTER REPO NAME\n");
                        name = textIO.newStringInputReader()
                                .withDefaultValue("default-repo")
                                .read("Repo name:");
                        String pull = peer.pull(name);
                        terminal.printf("\n" + pull.toUpperCase() + "\n");
                        break;
                    case 7:
                        if (peer.getRepository() == null)
                            terminal.printf("\nFIRST OF ALL, CREATE A REPO\n");
                        else {
                            files = peer.getRepository().getFiles();
                            if (files.isEmpty())
                                terminal.printf("\nNO FILE\n");
                            else {
                                terminal.printf("\n-----FILES-----\n");
                                for (File f : files)
                                    terminal.printf("\n" + f.getName() + "\n");
                                terminal.printf("\n-----FILES-----\n");
                            }
                        }
                        break;
                    case 8:
                        if (peer.getRepository() == null)
                            terminal.printf("\nFIRST OF ALL, CREATE A REPO\n");
                        else {
                            ArrayList<Commit> commits = peer.getRepository().getCommits();
                            if (commits.isEmpty())
                                terminal.printf("\nNO COMMIT\n");
                            else {
                                terminal.printf("\n-----COMMITS-----\n");
                                for (Commit c : commits)
                                    terminal.printf("\n" + c + "\n");
                                terminal.printf("\n-----COMMITS-----\n");
                            }
                        }
                        break;
                    case 9:
                        terminal.printf("\nARE YOU SURE TO LEAVE THE NETWORK?\n");
                        boolean exit = textIO.newBooleanInputReader().withDefaultValue(false).read("exit?");
                        if (exit) {
                            peer.leaveNetwork();
                            System.exit(0);
                        }
                        break;

                    default:
                        break;
                }
            }

        } catch (CmdLineException clEx) {  
			System.err.println("ERROR: Unable to parse command-line options: " + clEx);  
		} 

    }

    public static void printMenu(TextTerminal terminal) {
		terminal.printf("\n1 - CREATE REPO\n");
		terminal.printf("\n2 - ADD FILES TO REPO\n");
        terminal.printf("\n3 - REMOVE FILES FROM REPO\n");
		terminal.printf("\n4 - COMMIT\n");
		terminal.printf("\n5 - PUSH\n");
        terminal.printf("\n6 - PULL\n");
        terminal.printf("\n7 - SHOW FILES\n");
        terminal.printf("\n8 - SHOW COMMITS\n");
		terminal.printf("\n9 - EXIT\n");
	}

    public static ArrayList<File> createFiles(TextTerminal terminal, TextIO textIO) {
        terminal.printf("\nENTER FILES NUMBER TO ADD\n");
        int n = textIO.newIntInputReader()
                .withDefaultValue(1)
                .withMinVal(0)
                .read("Number:");
        
        ArrayList<File> files = new ArrayList<>();
        int i = 0;
        while (i < n) {
            terminal.printf("\nENTER FILE " + (i + 1) + " NAME\n");
            String name = textIO.newStringInputReader()
                            .read("File name:");
            File file = new File(name);
            if (files.contains(file)) {
                terminal.printf("\n\"" + name + "\" FILE ALREADY EXISTS\n");
                continue;
            }
            files.add(file);
            i++;
        }

        return files;
    }

    public static HashSet<File> removeFiles(TextTerminal terminal, TextIO textIO, HashSet<File> files) {
        terminal.printf("\nENTER FILES NUMBER TO REMOVE\n");
        int n = textIO.newIntInputReader()
                .withDefaultValue(1)
                .withMinVal(0)
                .withMaxVal(files.size())
                .read("Number:");
        
        int i = 0;
        while (i < n) {
            terminal.printf("\nENTER FILE " + (i + 1) + " NAME\n");
            String name = textIO.newStringInputReader()
                            .read("File name:");
            File file = new File(name);
            if (!files.contains(file)) {
                terminal.printf("\n\"" + name + "\" FILE NOT EXIST\n");
                continue;
            }
            terminal.printf("\nARE YOU SURE TO REMOVE THE FILE?\n");
            boolean remove = textIO.newBooleanInputReader().withDefaultValue(false).read("remove?");
            if (remove) {
                files.remove(file);
                i++;
            }
        }

        return files;
    }

}
