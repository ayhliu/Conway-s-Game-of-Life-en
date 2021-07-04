package pack;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.Toolkit;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.swing.JPanel;

@SuppressWarnings("serial")
public class Model extends JPanel implements MouseListener, MouseMotionListener {

	/* Maximum Display Size [1303][803] on 3840 × 2160 Resolution */
	private static final int MaxWidth = Math.max(Toolkit.getDefaultToolkit().getScreenSize().width - 233, 0);
	private static final int MaxHeight = Math.max(Toolkit.getDefaultToolkit().getScreenSize().height - 61, 0);

	private static final Stroke ThinestStroke = new BasicStroke(0);

	/* Defaults */
	private int lowSurviveThreshold = 2;
	private int highSurviveThreshold = 3;
	private int lowBirthThreshold = 3;
	private int highBirthThreshold = 3;

	private boolean automation;
	private boolean torus;

	/* Components */
	private boolean[][] universe;
	private ScheduledFuture<?> schedule;

	/* Display */
	private int pointSize;
	private View view;

	/* Dynamic */
	private int alive;
	private int generation;

	/* mouse[0] == -1 Means Pointer is off Model */
	private int[] mouse = new int[] { -1, 0 };

	public Model() {
		universe = new boolean[600][600];

		setBackground(Color.BLACK);
		setUniverseSize();

		addMouseListener(this);
		addMouseMotionListener(this);
	}

	private void setUniverseSize() {
		pointSize = Math.min(MaxWidth / universe.length, MaxHeight / universe[0].length);
		setPreferredSize(new Dimension(pointSize * universe.length, pointSize * universe[0].length));
	}

	public synchronized void clear() {
		for (int i = 0; i < universe.length; i++) {
			for (int j = 0; j < universe[0].length; j++) {
				universe[i][j] = false;
			}
		}

		view.update("Live", alive = 0);
		view.update("Generation", generation = 0);
		repaint();
	}

	public synchronized void randomize() {
		alive = 0;

		for (int i = 0; i < universe.length; i++) {
			for (int j = 0; j < universe[0].length; j++) {
				if (universe[i][j] = Math.random() < 0.5) {
					alive++;
				}
			}
		}

		view.update("Live", alive);
		view.update("Generation", generation = 0);
		repaint();
	}

	public synchronized void mirror() {
		for (int i = universe.length / 2 - 1; i >= 0; i--) {
			for (int j = 0; j < universe[0].length; j++) {
				boolean temp = universe[i][j];
				universe[i][j] = universe[universe.length - i - 1][j];
				universe[universe.length - i - 1][j] = temp;
			}
		}

		repaint();
	}

	public synchronized void rotate() {
		boolean[][] rotated = new boolean[universe[0].length][universe.length];

		for (int i = 0; i < universe.length; i++) {
			for (int j = 0; j < universe[0].length; j++) {
				rotated[universe[0].length - j - 1][i] = universe[i][j];
			}
		}

		universe = rotated;
		setUniverseSize();

		view.update("SelectAll", null);
		view.update("Pack", null);

		repaint();
	}

	public synchronized void crop(int x, int y, int width, int height) {
		if (width < 1) {
			throw new IllegalArgumentException("Illegal Width!");
		}
		if (height < 1) {
			throw new IllegalArgumentException("Illegal Height!");
		}

		boolean[][] next = new boolean[width][height];

		width = Math.min(width, universe.length - x);
		height = Math.min(height, universe[0].length - y);

		alive = 0;

		for (int i = Math.max(-x, 0); i < width; i++) {
			for (int j = Math.max(-y, 0); j < height; j++) {
				if (next[i][j] = universe[x + i][y + j]) {
					alive++;
				}
			}
		}

		universe = next;
		setUniverseSize();

		view.update("Live", alive);
		view.update("Generation", generation = 0);
		view.update("Pack", null);
		repaint();
	}

	public synchronized void moveUniverse(int x, int y) {

		/* Modulus */
		x = x % universe.length;
		y = y % universe[0].length;

		if (x < 0) {
			x += universe.length;
		}
		if (y < 0) {
			y += universe[0].length;
		}

		boolean[][] move = new boolean[universe.length][universe[0].length];

		/* Move Bottom Left Piece */
		for (int i = universe.length - x - 1; i >= 0; i--) {
			for (int j = y; j < universe[0].length; j++) {
				move[i + x][j - y] = universe[i][j];
			}
		}

		/* Move Top Right Piece */
		for (int i = universe.length - x; i < universe.length; i++) {
			for (int j = 0; j < y; j++) {
				move[i - universe.length + x][j + universe[0].length - y] = universe[i][j];
			}
		}

		/* Move Top Left Piece */
		for (int i = universe.length - x - 1; i >= 0; i--) {
			for (int j = 0; j < y; j++) {
				move[i + x][j + universe[0].length - y] = universe[i][j];
			}
		}

		/* Move Bottom Right Piece */
		for (int i = universe.length - x; i < universe.length; i++) {
			for (int j = y; j < universe[0].length; j++) {
				move[i - universe.length + x][j - y] = universe[i][j];
			}
		}

		if (!torus) {
			view.update("Generation", generation = 0);
		}

		universe = move;
		repaint();
	}

	public int getUniverseWidth() {
		return universe.length;
	}

	public int getUniverseHeight() {
		return universe[0].length;
	}

	public synchronized void setThresholds(int lowSurviveThreshold, int highSurviveThreshold, int lowBirthThreshold,
			int highBirthThreshold) {
		this.lowSurviveThreshold = lowSurviveThreshold;
		this.highSurviveThreshold = highSurviveThreshold;
		this.lowBirthThreshold = lowBirthThreshold;
		this.highBirthThreshold = highBirthThreshold;

		view.update("Generation", generation = 0);
	}

	public void setView(View view) {
		this.view = view;
	}

	public synchronized void toggleTorus() {
		torus = !torus;
	}

	/* Automation Initialized to False */
	public void toggleAutomation(int delay) {
		if (delay < 1) {
			throw new IllegalArgumentException("Illegal Delay Time!");
		}

		if (automation = !automation) {
			schedule = Executors.newScheduledThreadPool(1).scheduleAtFixedRate(new Runnable() {
				@Override
				public void run() {
					advance();
				}
			}, 0, delay, TimeUnit.MILLISECONDS);
		} else {
			schedule.cancel(false);
		}
	}

	public synchronized void advance() {
		int[][] neighbor = new int[universe.length][universe[0].length];

		neighborCalculate: {
			if (universe.length != 1 && universe[0].length != 1) {

				/* Core */
				for (int i = 1; i < universe.length - 1; i++) {
					for (int j = 1; j < universe[0].length - 1; j++) {
						if (universe[i][j]) {
							neighbor[i - 1][j - 1]++;
							neighbor[i - 1][j]++;
							neighbor[i - 1][j + 1]++;
							neighbor[i][j - 1]++;
							neighbor[i][j + 1]++;
							neighbor[i + 1][j - 1]++;
							neighbor[i + 1][j]++;
							neighbor[i + 1][j + 1]++;
						}
					}
				}

				for (int i = 1; i < universe.length - 1; i++) {

					/* North Edge */
					if (universe[i][0]) {
						neighbor[i - 1][0]++;
						neighbor[i + 1][0]++;
						neighbor[i - 1][1]++;
						neighbor[i][1]++;
						neighbor[i + 1][1]++;
						if (torus) {
							neighbor[i - 1][universe[0].length - 1]++;
							neighbor[i][universe[0].length - 1]++;
							neighbor[i + 1][universe[0].length - 1]++;
						}
					}

					/* South Edge */
					if (universe[i][universe[0].length - 1]) {
						neighbor[i - 1][universe[0].length - 1]++;
						neighbor[i + 1][universe[0].length - 1]++;
						neighbor[i - 1][universe[0].length - 2]++;
						neighbor[i][universe[0].length - 2]++;
						neighbor[i + 1][universe[0].length - 2]++;
						if (torus) {
							neighbor[i - 1][0]++;
							neighbor[i][0]++;
							neighbor[i + 1][0]++;
						}
					}
				}

				for (int j = 1; j < universe[0].length - 1; j++) {

					/* West Edge */
					if (universe[0][j]) {
						neighbor[0][j - 1]++;
						neighbor[0][j + 1]++;
						neighbor[1][j - 1]++;
						neighbor[1][j]++;
						neighbor[1][j + 1]++;
						if (torus) {
							neighbor[universe.length - 1][j - 1]++;
							neighbor[universe.length - 1][j]++;
							neighbor[universe.length - 1][j + 1]++;
						}
					}

					/* East Edge */
					if (universe[universe.length - 1][j]) {
						neighbor[universe.length - 1][j - 1]++;
						neighbor[universe.length - 1][j + 1]++;
						neighbor[universe.length - 2][j - 1]++;
						neighbor[universe.length - 2][j]++;
						neighbor[universe.length - 2][j + 1]++;
						if (torus) {
							neighbor[0][j - 1]++;
							neighbor[0][j]++;
							neighbor[0][j + 1]++;
						}
					}
				}

				/* NorthWest Corner */
				if (universe[0][0]) {
					neighbor[0][1]++;
					neighbor[1][0]++;
					neighbor[1][1]++;
					if (torus) {
						neighbor[universe.length - 1][universe[0].length - 1]++;
						neighbor[0][universe[0].length - 1]++;
						neighbor[1][universe[0].length - 1]++;
						neighbor[universe.length - 1][0]++;
						neighbor[universe.length - 1][1]++;
					}
				}

				/* NorthEast Corner */
				if (universe[universe.length - 1][0]) {
					neighbor[universe.length - 2][0]++;
					neighbor[universe.length - 2][1]++;
					neighbor[universe.length - 1][1]++;
					if (torus) {
						neighbor[universe.length - 2][universe[0].length - 1]++;
						neighbor[universe.length - 1][universe[0].length - 1]++;
						neighbor[0][universe[0].length - 1]++;
						neighbor[0][0]++;
						neighbor[0][1]++;
					}
				}

				/* SouthWest Corner */
				if (universe[0][universe[0].length - 1]) {
					neighbor[0][universe[0].length - 2]++;
					neighbor[1][universe[0].length - 2]++;
					neighbor[1][universe[0].length - 1]++;
					if (torus) {
						neighbor[universe.length - 1][universe[0].length - 2]++;
						neighbor[universe.length - 1][universe[0].length - 1]++;
						neighbor[universe.length - 1][0]++;
						neighbor[0][0]++;
						neighbor[1][0]++;
					}
				}

				/* SouthEast Corner */
				if (universe[universe.length - 1][universe[0].length - 1]) {
					neighbor[universe.length - 2][universe[0].length - 2]++;
					neighbor[universe.length - 1][universe[0].length - 2]++;
					neighbor[universe.length - 2][universe[0].length - 1]++;
					if (torus) {
						neighbor[0][universe[0].length - 2]++;
						neighbor[0][universe[0].length - 1]++;
						neighbor[0][0]++;
						neighbor[universe.length - 1][0]++;
						neighbor[universe.length - 2][0]++;
					}
				}

				break neighborCalculate;
			}

			/* Vertical Stick */
			if (universe.length == 1 && universe[0].length != 1) {

				/* Core */
				for (int j = 1; j < universe[0].length - 1; j++) {
					if (universe[0][j]) {
						neighbor[0][j - 1]++;
						neighbor[0][j + 1]++;
						if (torus) {
							neighbor[0][j - 1] += 2;
							neighbor[0][j] += 2;
							neighbor[0][j + 1] += 2;
						}
					}
				}

				/* Ends */
				if (universe[0][0]) {
					neighbor[0][1]++;
					if (torus) {
						neighbor[0][0] += 2;
						neighbor[0][1] += 2;
						neighbor[0][universe[0].length - 1] += 3;
					}
				}
				if (universe[0][universe[0].length - 1]) {
					neighbor[0][universe[0].length - 2]++;
					if (torus) {
						neighbor[0][0] += 3;
						neighbor[0][universe[0].length - 1] += 2;
						neighbor[0][universe[0].length - 2] += 2;
					}
				}

				break neighborCalculate;
			}

			/* Horizontal Stick */
			if (universe.length != 1 && universe[0].length == 1) {

				/* Core */
				for (int i = 1; i < universe.length - 1; i++) {
					if (universe[i][0]) {
						neighbor[i - 1][0]++;
						neighbor[i + 1][0]++;
						if (torus) {
							neighbor[i - 1][0] += 2;
							neighbor[i][0] += 2;
							neighbor[i + 1][0] += 2;
						}
					}
				}

				/* Ends */
				if (universe[0][0]) {
					neighbor[1][0]++;
					if (torus) {
						neighbor[0][0] += 2;
						neighbor[1][0] += 2;
						neighbor[universe.length - 1][0] += 3;
					}
				}
				if (universe[universe.length - 1][0]) {
					neighbor[universe.length - 2][0]++;
					if (torus) {
						neighbor[universe.length - 2][0] += 2;
						neighbor[universe.length - 1][0] += 2;
						neighbor[0][0] += 3;
					}
				}

				break neighborCalculate;
			}

			/* Size [1][1] */
			if (universe[0][0] && torus) {
				neighbor[0][0] += 8;
			}

		}

		for (int i = 0; i < universe.length; i++) {
			for (int j = 0; j < universe[0].length; j++) {

				/* Survive */
				if (universe[i][j]) {
					if (neighbor[i][j] < lowSurviveThreshold || neighbor[i][j] > highSurviveThreshold) {
						universe[i][j] = false;
						alive--;
					}

					continue;
				}

				/* Birth */
				if (neighbor[i][j] >= lowBirthThreshold && neighbor[i][j] <= highBirthThreshold) {
					universe[i][j] = true;
					alive++;
				}
			}
		}

		view.update("Live", alive);
		view.update("Generation", ++generation);
		repaint();
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		Graphics2D g2d = (Graphics2D) g.create();
		g2d.setColor(Color.WHITE);

		for (int i = 0; i < universe.length; i++) {
			for (int j = 0; j < universe[0].length; j++) {
				if (universe[i][j]) {
					g2d.fillRect(i * pointSize, j * pointSize, pointSize, pointSize);
				}
			}
		}

		/* Highlight */
		if (mouse[0] != -1) {
			g2d.setColor(Color.ORANGE);
			g2d.setStroke(ThinestStroke);
			g2d.drawRect(mouse[0] * pointSize, mouse[1] * pointSize, pointSize, pointSize);
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (universe[mouse[0]][mouse[1]] = !universe[mouse[0]][mouse[1]]) {
			alive++;
		} else {
			alive--;
		}

		view.update("Live", alive);
		view.update("Generation", generation = 0);
		repaint();
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
		mouse[0] = -1;

		view.update("Mouse", null);
		repaint();
	}

	@Override
	public void mouseDragged(MouseEvent e) {
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		mouse[0] = e.getX() / pointSize;
		mouse[1] = e.getY() / pointSize;

		view.update("Mouse", mouse);
		repaint();
	}

}
