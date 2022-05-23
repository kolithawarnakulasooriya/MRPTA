
import TaskBot.TaskBotUI;
import TaskBot.TaskPlane;

public class mainRunner {
	public static void main(String []args) {
		new TaskBotUI(new TaskPlane(System.currentTimeMillis())).createController();
	}
}
