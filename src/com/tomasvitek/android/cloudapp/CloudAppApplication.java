/**
 * CloudAppApplication
 * 
 * Stores current connection to CloudApp API
 *
 * @copyright   Copyright (c) 2013 Tomas Vitek
 * @author      Tomas Vitek
 */

package com.tomasvitek.android.cloudapp;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;

import com.cloudapp.api.CloudApp;
import com.cloudapp.impl.CloudAppImpl;
import com.tomasvitek.android.cloudapp.models.ListItem;

import android.app.Application;
import android.util.Log;

public class CloudAppApplication extends Application {
	private CloudApp cloudAppApi = null;
	private ArrayList<ListItem> list = null;
	private String URLToOpen = null;
	
	private String email = "";
	private String password = "";
	
	private int page = 1;
	
	public void setURLToOpen(String uRLToOpen) {
		URLToOpen = uRLToOpen;
	}
	
	public String getURLToOpen() {
		return URLToOpen;
	}
	
	
	public CloudApp createCloudAppApi(String email, String password) {
		if (cloudAppApi == null || this.email != email || this.password != password) {
			cloudAppApi = new CloudAppImpl(email, password);
			this.email = email;
			this.password = password;
		}
		return getCloudAppApi();
	}
	
	public CloudApp getCloudAppApi() {
		return cloudAppApi;
	}
	
	public void setCloudAppApi(CloudApp cloudAppApi) {
		this.cloudAppApi = cloudAppApi;
	}
	
	String filename = "temp";
	
	public void incrementPage() {
		this.page++;
	}
	
	public int nextPage() {
		this.page++;
		return this.page;
	}
	
	public int getPage() {
		return this.page;
	}
	
	public ArrayList<ListItem> getList() {
		if (this.list == null) {
			this.list = new ArrayList<ListItem>();
		}
		return this.list;
	}
	
	public void clearList() {
		setList(new ArrayList<ListItem>());
	}
	
	public void setList(ArrayList<ListItem> list) {
		this.list = list;
		
		FileOutputStream fos = null;
		ObjectOutputStream out = null;
		try {
			fos = this.openFileOutput(filename, MODE_PRIVATE);
			out = new ObjectOutputStream(fos);
			out.writeObject(list);
			out.close();
			System.out.println("Object Persisted");
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	public void removeFromList(ListItem item) {
		list.remove(item);
		
		FileOutputStream fos = null;
		ObjectOutputStream out = null;
		try {
			fos = this.openFileOutput(filename, MODE_PRIVATE);
			out = new ObjectOutputStream(fos);
			out.writeObject(list);
			out.close();
			System.out.println("Object Persisted");
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	public boolean clearCachedList() {
		return this.deleteFile(filename);
	}
	
	
	@SuppressWarnings("unchecked")
	public boolean restoreList() {
		FileInputStream fis = null;
        ObjectInputStream in = null;
             
		try {
			fis = openFileInput(filename);
			in = new ObjectInputStream(fis);
			this.list = (ArrayList<ListItem>) in.readObject();
			in.close();
			if (this.list == null) return false;
			if (this.list.size() == 0) return false;
			return true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (StreamCorruptedException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return false;
		}
	}
}
