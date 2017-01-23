package org.vivecraft.command;

public class Cmd {
	
	private String cmd = "";
	private String desc = "";
	private String hovertext = "";
	
	public Cmd(String cmd, String desc, String hovertext){
		this.cmd = cmd;
		this.desc = desc;
		this.hovertext = hovertext;
	}
	
	public String getCommand(){
		return this.cmd;
	}
	
	public String getDescription(){
		return this.desc;
	}

	public String getHoverText(){
		return this.hovertext;
	}
}
