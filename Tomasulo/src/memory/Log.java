package memory;

public class Log {
	public static int log(int x) {
		int count = 0;
		while (x != 0) {
			x = x / 2;
			count++;
		}
		return count - 1;
	}
}
