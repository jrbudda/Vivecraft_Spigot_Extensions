package org.vivecraft.command;

public class Cmd {
	
	private String cmd = "";
	private String desc = "";
	
	public Cmd(String cmd, String desc){
		this.cmd = cmd;
		this.desc = desc;
	}
	
	public String getCommand(){
		return this.cmd;
	}
	
	public String getDescription(){
		return this.desc;
	}

}
