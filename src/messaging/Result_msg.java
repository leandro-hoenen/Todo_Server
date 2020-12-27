package messaging;

public class Result_msg extends Message {
	private boolean result;
	private String data;
	private String token;
	
	public Result_msg(String result, String data) {
		super(MessageType.RESULT);
		this.result = Boolean.parseBoolean(result);
		this.data = data;
	}

	@Override
	public String toString() {
		return type.toString() + '|' + result + '|' + data;
	}
}
