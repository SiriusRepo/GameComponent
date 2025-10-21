package example;

import javax.swing.JFrame;

public class Main {

	public static void main(String[] args) {
		
		JFrame frame = new JFrame();
		frame.setTitle("Number RPG");
		frame.setSize(800, 600);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(true);
		
		CountingPanel panel = new CountingPanel();
		frame.add(panel);
		frame.setVisible(true);
		panel.startGame();
	}

}
