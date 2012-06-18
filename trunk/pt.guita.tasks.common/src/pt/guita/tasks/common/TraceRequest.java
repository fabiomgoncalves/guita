package pt.guita.tasks.common;

import java.io.Serializable;

public class TraceRequest implements Serializable {
	private static final long serialVersionUID = 1L;

	public final int port;
	public final boolean traceFieldReads;
	public final boolean traceFieldWrites;
	final String[] filter;
	
	public TraceRequest() {
		port = 0;
		traceFieldReads = false;
		traceFieldWrites = false;
		filter = null;
	}
	
	public TraceRequest(int port, boolean traceFieldReads, boolean traceFieldWrites, String[] filter) {
		this.port = port;
		this.traceFieldReads = traceFieldReads;
		this.traceFieldWrites = traceFieldWrites;
		this.filter = filter;
	}
	
	public boolean activate() {
		return port != 0;
	}
}
