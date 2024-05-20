import java.awt.Color;

import javax.swing.*;

public class Driver extends JFrame{

	public static void main(String[] args) {
		JFrame frame = new JFrame();
		frame.setSize(1920, 1080);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		String[] gameModes = {"Player vs Player", "Player vs AI", "AI vs AI"};
		String mode = (String) JOptionPane.showInputDialog(frame,"Choose the game mode:","Game Mode Selection",JOptionPane.PLAIN_MESSAGE,null,gameModes,gameModes[0]);
		String color = "";
		if(mode.charAt(0) == 'P') {
			String[] colors = new String[] {"Red", "Yellow"};
			color = (String) JOptionPane.showInputDialog(frame,"Choose your color:","Color Selection",JOptionPane.PLAIN_MESSAGE,null,colors,colors[0]);
		}
		
		
		Game c4 = new Game(mode, color.equals("Red") ? 2 : 1);
	
		frame.getContentPane().add(c4);
		frame.setVisible(true);
		c4.startGame();
		
	}

}
