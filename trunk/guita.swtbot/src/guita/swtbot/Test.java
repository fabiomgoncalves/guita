package guita.swtbot;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.io.PrintStream;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.waits.ICondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;

public class Test {

	SWTWorkbenchBot bot = new SWTWorkbenchBot();

	@org.junit.Test
	public void test() {
		bot.activeView().close();
		bot.perspectiveByLabel("Java").activate();
		int mb = 1024*1024;
		Runtime runtime = Runtime.getRuntime();
		try {
			PrintStream print = new PrintStream("data.txt");

			for(int i = 0; i < 50; i++) {
				bot.menu("File").menu("New").menu("Project...").click();
				final SWTBotShell shell = bot.shell("New Project");
				shell.activate();
				bot.tree().expandNode("Java").select("Java Project");
				bot.button("Next >").click();

				bot.textWithLabel("Project name:").setText("Proj" + i);
				bot.button("Finish").click();
				bot.sleep(2000);

				bot.viewByTitle("Package Explorer").setFocus();

				bot.tree().select("Proj" + i);
				for(int j = 0; j < 5; j++) {
					bot.menu("File").menu("New").menu("Class").click();
					bot.shell("New Java Class").activate();
					bot.textWithLabel("Name:").setText("Proj" + i + "_" + j);
					bot.button("Finish").click();
					bot.sleep(3000);
					bot.menu("File"). menu("Close").click();
				}
				bot.viewByTitle("Package Explorer").setFocus();
				bot.tree().select("Proj" + i);
				
				System.gc();
				print.print(((runtime.totalMemory() - runtime.freeMemory()) / mb) + "\n");
			}
			print.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
