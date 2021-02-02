package quickfix.examples.banzai;

import quickfix.Message;

import java.util.UUID;

public class Util {
	public static String get32UUID() {
		String uuid = UUID.randomUUID().toString().trim().replaceAll("-", "");
		return uuid;
	}
	public static String generateID() {
		return Long.toString(System.currentTimeMillis());
	}

	public static void printMsg(Message msg) {
		char delimiter = 1;
		String str = msg.toString();
		str = str.replace(delimiter, '|');
		System.out.println("get message is " + str);
	}
}
