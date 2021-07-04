package pack;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

public class Main {

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				JFrame mainFrame = new JFrame();
				mainFrame.setTitle("生命游戏");
				mainFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
				mainFrame.setContentPane(new View(new Model(), mainFrame));
				mainFrame.pack();

				/* View Adjusts Frame Size */
				mainFrame.setResizable(false);
				mainFrame.setVisible(true);
			}
		});
	}
}
