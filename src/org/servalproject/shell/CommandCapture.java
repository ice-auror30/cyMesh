package org.servalproject.shell;


import java.util.ArrayList;
import java.util.List;

public class CommandCapture extends Command {
	private StringBuilder sb = new StringBuilder();
	private List<String> shellLines = new ArrayList<String>();

	public CommandCapture(String... command) {
		super(command);
	}

	@Override
	public void output(String line) {
		shellLines.add(line);
		sb.append(line).append('\n');
	}

	@Override
	public String toString() {
		return sb.toString();
	}

	public List<String> getShellLines(){
		return shellLines;
	}
}
