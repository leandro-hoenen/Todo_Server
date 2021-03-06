package jat.appClasses;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;

import jat.ServiceLocator;
import jat.abstractClasses.Model;
import jat.commonClasses.Client;
import messaging.Result_msg;
import userData.Account;
import userData.Priority;
import userData.ToDo;

/**
 * Copyright 2015, FHNW, Prof. Dr. Brad Richards. All rights reserved. This code
 * is licensed under the terms of the BSD 3-clause license (see the file
 * license.txt).
 * 
 * @author Brad Richards
 */
public class App_Model extends Model {
	private ArrayList<Client> clients = new ArrayList<>();
	private ArrayList<Account> accounts = new ArrayList<>();
	
    ServiceLocator serviceLocator;
    ServerSocket listener;
    private volatile boolean stop = false;
    
    public App_Model() {
        serviceLocator = ServiceLocator.getServiceLocator();        
        serviceLocator.getLogger().info("Application model initialized");
    }
    
    // starts server & creates new client objects
    public void startServer(int port) {
    	serviceLocator.getLogger().info("Start Server");
    	try {
    		listener = new ServerSocket(port, 10, null);
    		Runnable runnable = new Runnable() {
				@Override
				public void run() {
					while (!stop) {
						try {
							Socket socket = listener.accept();
							Client client = new Client(App_Model.this, socket);
							synchronized(clients) {
								clients.add(client);	
							}
							serviceLocator.getLogger().info("New connection with: " + client.toString() + ", " + "Socket: " + socket.toString());
						} catch (IOException e) {
							serviceLocator.getLogger().info(e.toString());
						}
					}					
				}   			
    		};
    		Thread thread = new Thread(runnable);
    		thread.start();
    	} catch (IOException e) {
    		serviceLocator.getLogger().info(e.toString());
    	}
    }
    
    // No UI implemented so far and for this reason not used. Could be used by later implementation of a UI.
    public void stopServer() {
    	serviceLocator.getLogger().info("Stop running clients");
    	for (Client client : clients)
    		client.stop();
    	
    	serviceLocator.getLogger().info("Stop server");
    	stop = true;
    	if (listener != null) {
    		try {
    			listener.close();
    		} catch (IOException e) {
    		}
    	}
    }
    
    public synchronized void createAccount(String userName, String password, Client client) {
    	// check if account with the requested userName already exists
    	for (Account account : accounts) {
    		if (account.getUserName().equals(userName)) {
    			answerInvalidRequest(client);
    			return;
    		}
    	}
    	
    	// validate account data
    	if (!Account.validateLoginData(userName, password)) {
    		answerInvalidRequest(client);
    		return;
    	}
    	
    	// creates new account with the data given by the user
    	serviceLocator.getLogger().info("Create new Account with username: " + userName);
    	Account account = new Account(userName, password);
    	accounts.add(account);
    	answerValidRequest(client);
    }

	public void login(String userName, String password, Client client) {
		if (client.getToken() != null) {
			answerInvalidRequest(client);
			return;
		}
		
		synchronized(accounts) {
			boolean exists = false;
			Iterator<Account> iterator = accounts.iterator();
			
			while (iterator.hasNext() && exists == false) {
				Account account = iterator.next();
				if (account.getUserName().equals(userName) && account.getPassword().equals(password)) {
					serviceLocator.getLogger().info("Login user: " + userName);
					client.setToken();
					client.setUserName(userName);
					answerValidRequest(client, client.getToken());
					exists = true;
				}
			}
			if (!exists)
				answerInvalidRequest(client);
		}
	}
	
	public void changePassword(String newPassword, String token, Client client) {	
		if (!client.validateToken(token) || !Account.validatePassword(newPassword)) {
			answerInvalidRequest(client);
			return;
		}
		
		serviceLocator.getLogger().info("change password for user: " + client.getUserName());
		synchronized (accounts) {
			for (Account account : accounts) {
				if (account.getUserName().equals(client.getUserName())) {
					account.setPassword(newPassword);
					answerValidRequest(client);
					break;
				}
			}
		}
	}
	
	public void Logout(Client client) {
		if (client.getToken() != null) {
			serviceLocator.getLogger().info("Logout user: " + client.getUserName());
			client.setToken();
			client.setUserName();
			answerValidRequest(client);
		} else {
			answerInvalidRequest(client);
		}
	}
	
	public void createToDo(String title, Priority priority, String description, 
			LocalDate dueDate, String token, Client client) {	
		
		if (!client.validateToken(token)) {
			answerInvalidRequest(client);
			return;
		}
		
		ToDo toDo = null;
		serviceLocator.getLogger().info("Store new todo");
		synchronized(accounts) {
			for (Account account : accounts) {
				if (account.getUserName().equals(client.getUserName())) {
					if (dueDate == null && ToDo.validateToDoData(title, description)) {
						toDo = new ToDo(account.generateToDoId(), title, priority, description);
					} else if (dueDate != null && ToDo.validateToDoData(title, description, dueDate)) {
						toDo = new ToDo(account.generateToDoId(), title, priority, description, dueDate);
					} else {
						answerInvalidRequest(client);
						return;
					}									
					account.addToDo(toDo);
					answerValidRequest(client, Integer.toString(toDo.getId()));
					break;
				}
			}
		}
	}
	
	public void getToDo(int id, String token, Client client) {
		if (!client.validateToken(token)) {
			answerInvalidRequest(client);
			return;
		}
		
		boolean accountFound = false;		
		ToDo toDo = null;		
		Iterator<Account> iterator = accounts.iterator();
		
		while (iterator.hasNext() && !accountFound) {
			Account account = iterator.next();
			if (account.getUserName().equals(client.getUserName())) {
				accountFound = true;
				toDo = account.getToDo(id);
			}
		}
		
		if (toDo != null) {
			serviceLocator.getLogger().info("Send todo to client: " + client.toString());
			answerValidRequest(client, toDo.toString());
		} else {
			answerInvalidRequest(client);
		}
	}
	
	public void deleteToDo(int id, String token, Client client) {
		if (!client.validateToken(token)) {
			answerInvalidRequest(client);
			return;
		}
		
		serviceLocator.getLogger().info("delete todo: " + Integer.toString(id));
		boolean deleted = false;
		for (Account account : accounts) {
			if (account.getUserName().equals(client.getUserName())) {
				deleted = account.deleteToDo(id);
				break;
			}
		}		
		if (deleted)
			answerValidRequest(client, Integer.toString(id));
		else
			answerInvalidRequest(client);
	}
	
	public void listToDos(String token, Client client) {
		if (!client.validateToken(token)) {
			answerInvalidRequest(client);
			return;
		}
		
		serviceLocator.getLogger().info("List todos");
		boolean accountFound = false;
		Iterator<Account> iterator = accounts.iterator();
		String toDos = null;
		
		while (iterator.hasNext() && !accountFound) {
			Account account = iterator.next();
			if (account.getUserName().equals(client.getUserName())) {
				accountFound = true;
				toDos = account.toDoListToString();
			}
		}
		
		if (toDos != null)
			answerValidRequest(client, toDos);
		else
			answerInvalidRequest(client);
	}
	
	public void getPing(String token, Client client) {
		serviceLocator.getLogger().info("Send ping");
		if ((token == null && client.getToken() == null) 
				|| (token != null && client.validateToken(token)))
			answerValidRequest(client);
		else
			answerInvalidRequest(client);
	}
	
	// answers client with valid reply if only the result must be send
	public void answerValidRequest(Client client) {
		serviceLocator.getLogger().info("Reply valid request without data");
		Result_msg msg = new Result_msg("true");
		client.send(msg);
	}
	
	// answers client with valid reply if the result and data are required
	public void answerValidRequest(Client client, String data) {
		serviceLocator.getLogger().info("Reply valid request with data");
		Result_msg msg = new Result_msg("true", data);
		client.send(msg);
	}
	
	// answers the client if the request is invalid
	public void answerInvalidRequest(Client client) {
		serviceLocator.getLogger().info("Reply invalid request");
		Result_msg msg = new Result_msg("false");
		client.send(msg);
	}
	
	// removes client from arrayList
	public void removeClient(Client client) {
		serviceLocator.getLogger().info("Remove client: " + client.toString());
		clients.remove(client);
	}
 
}
