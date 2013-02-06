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
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class CloudAppApplication extends Application {
	private CloudApp cloudAppApi;
	private ArrayList<ListItem> list = null;
	private String URLToOpen = null;
	
	public void setURLToOpen(String uRLToOpen) {
		URLToOpen = uRLToOpen;
	}
	
	public String getURLToOpen() {
		return URLToOpen;
	}
	
	public CloudApp getCloudAppApi() {
		if (cloudAppApi == null) {
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			String email = prefs.getString("email", "");
			String password = prefs.getString("password", "");
			
			cloudAppApi = new CloudAppImpl(email, password); 
		}
		
		return cloudAppApi;
	}
	
	public void setCloudAppApi(CloudApp cloudAppApi) {
		this.cloudAppApi = cloudAppApi;
	}
	
	public ArrayList<ListItem> getList() {
		return list;
	}
	
	String filename = "temp";
	
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
