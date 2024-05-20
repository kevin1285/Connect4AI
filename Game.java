import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.Timer;

public class Game extends JPanel{
	int turn=1;
	Color[][] G = new Color[6][7];
	static final int N=6, M=7;
	Map<Integer, Color> colors = new HashMap<>();
	static final int startX=100, startY = 100;
	final int r = 20;
	int d = r*2 + 20;
	boolean gameOver = false;
	AIPlayer AI, AI2;
	Stack<Integer> movesStk = new Stack<>();
	MouseAdapter mouse;
	
	public Game(String gameMode, int AITurn) {
		if(gameMode.equals("Player vs AI"))
			AI = new AIPlayer(AITurn);
		else if(gameMode.equals("AI vs AI")) {
			AI = new AIPlayer(1);
			AI2 = new AI2Player(2);
		}
		colors.put(1, Color.RED);
		colors.put(2, Color.YELLOW);
		
		if(AI2 == null) {
			JButton takebackButton = new JButton("Take Back");
	        takebackButton.addActionListener(new ActionListener() {
	            @Override
	            public void actionPerformed(ActionEvent e) {
	                takeback();
	                if(gameOver) {
	                	gameOver = false;
	                	addMouseListener(mouse);
	                }
	                repaint();
	            }
	        });
	        setLayout(null);
	        takebackButton.setBounds(startX+d*M/2-100/2, startY+d*N, 100, 50);
	        add(takebackButton);
		}
	}
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		drawBoard(g);
	}
	
	private void drawBoard(Graphics g) {
		g.setColor(Color.BLUE); 
		int x=startX, y=startY;
		int boardPadding = 5;
	    g.fillRoundRect(x-r/2 - boardPadding, y-r/2 - boardPadding, M * d + boardPadding*2, N * d + boardPadding*2, 25, 25);

		for(int i=0; i<N; i++) {
			for(int j=0; j<M; j++) {			
				drawCircle(g, x+d*j, y+d*i, r, G[i][j]==null ? Color.white : G[i][j]);
			}
		}
	}
	
	public void startGame() {
		if(AI2 != null) {
			AIVsAI();
			return;
		}
		if(AI!=null && turn==AI.TURN) {
            addDisc(M/2);
            repaint();
		}
		mouse = new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
            	if(AI!=null && turn == AI.TURN)
            		return;
            	if(e.getX() < startX)
            		return;
                int col = (e.getX() - startX) / d;
                if (col >= 0 && col < M && e.getX() < startX+ col*d + 2*r) {
                    addDisc(col);
                    repaint();
                    if(endGameIfOver()) {
                    	gameOver = true;
                    	return;
                    }
                    Timer timer = new Timer(1, new ActionListener() {
                    	public void actionPerformed(ActionEvent e) {
	                    	if(AI!=null && turn==AI.TURN) {
	            				AI.move();
	            				repaint();
	            				gameOver = endGameIfOver();
	            			}
                    	}
                    });
                    timer.setRepeats(false);  
            	    timer.start(); 
                    
                }
            }
        };
        
        addMouseListener(mouse);  
	}
	
	private void AIVsAI() {
		Timer timer = new Timer(1, new ActionListener() {  
	        public void actionPerformed(ActionEvent e) {
	            if (turn==1) {
	                AI.move();  // AI 1 makes a move
	                repaint();
	                if (endGameIfOver()) {
	                    ((Timer) e.getSource()).stop(); 
	                    return;
	                }
	            } else {
	                AI2.move();  
	                repaint();
	                if (endGameIfOver()) {
	                    ((Timer) e.getSource()).stop(); 
	                    return;
	                }
	            }
	        }
	    });
	    timer.setRepeats(true);  
	    timer.start(); 
	}
	
	private boolean endGameIfOver() {
		int gameState = checkGameOver();
		if(gameState != 0) {
			String gameOverStr;
			if(gameState > 0) {
				if(AI != null) {
					if(AI2 != null)
						gameOverStr = gameState==AI.TURN ? "AI 1 wins!" : "AI 2 wins!";
					else
						gameOverStr = gameState==AI.TURN ? "AI wins!" : "You win!";
				}else {
					gameOverStr = "Player " + gameState + " wins!";
				}
			}else {
				gameOverStr = "Draw!";
			}
			JOptionPane.showMessageDialog(null, gameOverStr);
			
			removeMouseListener(mouse);
		}
		return false;
	}
	private void drawCircle(Graphics g, int x, int y, int r, Color color) {
		g.setColor(color);
		g.fillOval(x, y, r*2, r*2);
	}
	public int checkGameOver() {//1->player1 win, 2-> player2 win, -1-> draw, 0-> not gameOver
		for(int rs=0; rs<N; rs++) {
			for(int cs=0; cs<M; cs++) {
				if(G[rs][cs] == null)
					continue;
				Color start = G[rs][cs];
				boolean row4=true, col4=true, diagR4=true, diagL4=true;
				for(int i=1; i<4; i++) {
					if(!inBounds(rs+i, cs) || start != G[rs+i][cs])
						row4=false;
					if(!inBounds(rs, cs+i) || start != G[rs][cs+i])
						col4=false;
					if(!inBounds(rs+i, cs+i) || start != G[rs+i][cs+i])
						diagR4=false;
					if(!inBounds(rs+i, cs-i) || start != G[rs+i][cs-i])
						diagL4=false;
				}
				if(row4 || col4 || diagL4 || diagR4)
					return start==colors.get(1) ? 1 : 2;	
				
			}
		}
		return isBoardFull() ? -1 : 0;
	}
	private boolean isBoardFull() {
		for(int r=0; r<N; r++) {
			for(int c=0; c<M; c++) {
				if(G[r][c]==null)
					return false;
			}
		}
		return true;
	}
	private boolean inBounds(int r, int c) {
		return r>=0&&r<N && c>=0&&c<M;
	}
	
	public void addDisc(int col) {
		for(int r=N-1; r>=0; r--) {
			if(G[r][col]==null) {
				G[r][col] = colors.get(turn);
				movesStk.add(col);
				turn = 3 - turn;
				return;
			}
		}
	}
	
	
	
	private void takeback() {
		if(movesStk.isEmpty())
			return;
		int takebacks = 1;
		if(AI != null) {
			takebacks = 2;
			if(movesStk.size()<2)
				return;
		}
		for(int i=0; i<takebacks; i++) {
			int col = movesStk.pop();
			removeDisc(col);		
		}
		turn = AI!=null ? 3 - AI.TURN : 3 - turn;
	}
	
	private void removeDisc(int col) {
		for(int r=0; r<N; r++) {
			if(G[r][col] != null) {
				G[r][col] = null;
				turn = 3 - turn;
				break;
			}
		}
	}
	
	public ArrayList<Integer> getValidCols() {
		ArrayList<Integer> validCols = new ArrayList<>();
		for(int c=0; c<M; c++) {
			for(int r=0; r<N; r++) {
				if(G[r][c] == null) {
					validCols.add(c);
					break;
				}
			}
		}
		return validCols;
	}
	
	
	private class AIPlayer{
		final int MAX_DEPTH = 8;
		final int TURN;
		final Color COLOR;
		public AIPlayer(int turn) {
			TURN = turn;
			COLOR = colors.get(turn);
		}
		public void move() {
			//randMove();
			minimaxMove();
		}
		
		public void randMove() {
			ArrayList<Integer> validCols = getValidCols();
			int col = (int)(Math.random()*validCols.size());
			addDisc(validCols.get(col));
		}
		
		public void minimaxMove() {
	        int bestCol = -1;
	        int bestScore = Integer.MIN_VALUE;

	        for (int col : getValidCols()) {
	            addDisc(col); 
	            int score = minimax(MAX_DEPTH, false, Integer.MIN_VALUE, Integer.MAX_VALUE);
	            removeDisc(col);  

	            if (score > bestScore) {
	                bestScore = score;
	                bestCol = col;
	            }
	        }

	        addDisc(bestCol);
		}
		public ArrayList shuffle(ArrayList<Integer> L) {
			for(int i=0; i<L.size(); i++) {
				int temp = L.get(i);
				int swapIdx = (int)(Math.random()*L.size());
				L.set(i, L.get(swapIdx));
				L.set(swapIdx, temp);
			}
			return L;
		}
		public int minimax(int depth, boolean maximize, int alpha, int beta) {
			int gameState = checkGameOver();
			if(gameState != 0) {
				if(gameState == -1)
					return 0;
				return gameState==TURN ? 999999999+depth : -999999999-depth;
			}
			if(depth == 0) {
				return evaluate();
			}
			
			ArrayList<Integer> validCols = shuffle(getValidCols());
			if(maximize) {
				int maxScore = Integer.MIN_VALUE;
				for(int col : validCols) {
					addDisc(col);
					int score = minimax(depth-1, false, alpha, beta);
					removeDisc(col);
					maxScore = Math.max(score, maxScore);
					alpha = Math.max(score, alpha);
					if(alpha >= beta)
						break;
				}
				return maxScore;
			
			}else {
				int minScore = Integer.MAX_VALUE;
				for(int col : validCols) {
					addDisc(col);
					int score = minimax(depth-1, true, alpha, beta);
					removeDisc(col);
					minScore = Math.min(score, minScore);
					beta = Math.min(score, beta);
					if(alpha >= beta)
						break;
				}
				return minScore;
			}
			
		}
		
		public int evaluate() {
			int score = streakScore();
			score += centerColumnScore();
			return score;
		}
		public int centerColumnScore() {
			int score = 0;
			int centerBonus = 5;
			for(int r=0; r<N; r++) {
				score += G[r][M/2] == COLOR ? centerBonus : -centerBonus;
			}
			return score;
		}
		public int streakScore() {
			int score = 0;
			int base = 10;
			for(int rs=0; rs<N; rs++) {
				for(int cs=0; cs<M; cs++) {
					//horiz
					for(int i=0; i<3; i++) {
						if(!inBounds(rs, cs+i) || G[rs][cs+i]!=COLOR)
							break;
						score += (int)Math.pow(base, i);
					}
					for(int i=0; i<3; i++) {
						if(!inBounds(rs, cs+i) || G[rs][cs+i]==COLOR)
							break;
						score -= (int)Math.pow(base, i);
					}
					
					
					//vert
					for(int i=0; i<3; i++) {
						if(!inBounds(rs+i, cs) || G[rs+i][cs]!=COLOR)
							break;
						score += (int)Math.pow(base, i);
					}
					for(int i=0; i<3; i++) {
						if(!inBounds(rs+i, cs) || G[rs+i][cs]==COLOR)
							break;
						score -= (int)Math.pow(base, i);
					}
					
					//pos diag
					for(int i=0; i<3; i++) {
						if(!inBounds(rs+i, cs+i) || G[rs+i][cs+i]!=COLOR)
							break;
						score += (int)Math.pow(base, i);
					}
					for(int i=0; i<3; i++) {
						if(!inBounds(rs+i, cs+i) || G[rs+i][cs+i]==COLOR)
							break;
						score -= (int)Math.pow(base, i);
					}
					
					//neg diag
					for(int i=0; i<3; i++) {
						if(!inBounds(rs+i, cs-i) || G[rs+i][cs-i]!=COLOR)
							break;
						score += (int)Math.pow(base, i);
					}
					for(int i=0; i<3; i++) {
						if(!inBounds(rs+i, cs-i) || G[rs+i][cs-i]==COLOR)
							break;
						score -= (int)Math.pow(base, i);
					}
				}
			}
			return score;
		}
		
	}
	
	
	private class AI2Player extends AIPlayer{
		public AI2Player(int turn) {
			super(turn);
		}
		
		
		public int streakScore() {
			int score = 0;
			
			for(int rs=0; rs<N; rs++) {
				for(int cs=0; cs<M; cs++) {
					//horiz
					int good=0, bad=0;
					for(int i=0; i<3; i++) {
						if(!inBounds(rs, cs+i))
							break;
						if(G[rs][cs+i] == COLOR)
							good++;
						else if(G[rs][cs+i] != Color.WHITE)
							bad++;
					}
					score += evalGroup(good, bad);
					
					//vert
					good=0; bad=0;
					for(int i=0; i<3; i++) {
						if(!inBounds(rs+i, cs))
							break;
						if(G[rs+i][cs] == COLOR)
							good++;
						else if(G[rs+i][cs] != Color.WHITE)
							bad++;
					}
					score += evalGroup(good, bad);
					
					//pos diag
					good=0; bad=0;
					for(int i=0; i<3; i++) {
						if(!inBounds(rs+i, cs+i))
							break;
						if(G[rs+i][cs+i] == COLOR)
							good++;
						else if(G[rs+i][cs+i] != Color.WHITE)
							bad++;
					}
					score += evalGroup(good, bad);
					
					//pos diag
					good=0; bad=0;
					for(int i=0; i<3; i++) {
						if(!inBounds(rs+i, cs-i))
							break;
						if(G[rs+i][cs-i] == COLOR)
							good++;
						else if(G[rs+i][cs-i] != Color.WHITE)
							bad++;
					}
					score += evalGroup(good, bad);
				}
				
				
			}
			return score;
		}
		
		public int evalGroup(int good, int bad) {
			int s3=10, s2=5;
			int so3=9, so2=4;
			int score = 0;
			if(bad == 0) {
				if(good == 3)
					score += s3;
				if(good == 2)
					score += s2;
			}else if(good == 0){
				if(bad == 3)
					score -= so3;
				if(good == 2)
					score -= so2;
			}
			return 0;
		}
		
	}
}
