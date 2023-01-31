package pack;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

@SuppressWarnings("serial")
public class View extends JPanel implements ActionListener {

	/* Thresholds */
	private JTextField lowSurviveThresholdText;
	private JTextField highSurviveThresholdText;
	private JTextField lowBirthThresholdText;
	private JTextField highBirthThresholdText;

	/* Select */
	private JTextField startXText;
	private JTextField startYText;
	private JTextField gridWidthText;
	private JTextField gridHeightText;

	/* Move */
	private JTextField moveXText;
	private JTextField moveYText;

	/* Automation */
	private JTextField delayText;

	/* Dynamic */
	private JLabel aliveLabel;
	private JLabel generationLabel;
	private JLabel mousePositionLabel;

	/* Structures */
	private Model model;
	private JFrame mainFrame;

	public View(Model model, JFrame mainFrame) {
		this.model = model;
		this.mainFrame = mainFrame;

		aliveLabel = new JLabel("A 0");
		generationLabel = new JLabel("G 0");
		mousePositionLabel = new JLabel();

		model.setView(this);

		JButton advanceButton = new JButton("ADV");
		JButton randomizeButton = new JButton("Rand");
		JButton clearButton = new JButton("CLR");
		JButton mirrorButton = new JButton("Mirror");
		JButton rotateButton = new JButton("90°\u2B6E");
		JCheckBox torusCheckBox = new JCheckBox("Torus");

		/* Thresholds */
		lowSurviveThresholdText = new JTextField("2");
		highSurviveThresholdText = new JTextField("3");
		lowBirthThresholdText = new JTextField("3");
		highBirthThresholdText = new JTextField("3");

		JButton defineButton = new JButton("SET");

		/* Select */
		gridWidthText = new JTextField(Integer.toString(model.getUniverseWidth()));
		gridHeightText = new JTextField(Integer.toString(model.getUniverseHeight()));

		startXText = new JTextField("0");
		startYText = new JTextField("0");

		JButton selectAllButton = new JButton("ALL");
		JButton cropButton = new JButton("Crop");

		/* Move */
		moveXText = new JTextField("0");
		moveYText = new JTextField("0");

		JButton moveButton = new JButton("Shift");

		/* Automation */
		delayText = new JTextField("1");
		JButton autoButton = new JButton("\u25B6");

		/* GUI */
		setLayout(new GridBagLayout());

		JPanel controlPanel = new JPanel(new GridLayout(8, 3));
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.NORTH;

		add(model);
		add(controlPanel, c);

		/* Dynamic */
		JPanel mousePositionWrap = new JPanel();
		mousePositionWrap.add(mousePositionLabel);

		controlPanel.add(aliveLabel);
		controlPanel.add(generationLabel);
		controlPanel.add(mousePositionWrap);

		/* Thresholds */
		JPanel thresholdsPanel = new JPanel(new GridLayout(1, 4));

		thresholdsPanel.add(lowSurviveThresholdText);
		thresholdsPanel.add(highSurviveThresholdText);
		thresholdsPanel.add(lowBirthThresholdText);
		thresholdsPanel.add(highBirthThresholdText);

		controlPanel.add(new JLabel("Thresh"));
		controlPanel.add(thresholdsPanel);
		controlPanel.add(defineButton);

		/* Select */
		JPanel selectFromPanel = new JPanel(new GridLayout(1, 2));
		JPanel selectSizePanel = new JPanel(new GridLayout(1, 2));

		selectFromPanel.add(startXText);
		selectFromPanel.add(startYText);
		selectSizePanel.add(gridWidthText);
		selectSizePanel.add(gridHeightText);

		controlPanel.add(new JLabel("RGN O"));
		controlPanel.add(selectFromPanel);
		controlPanel.add(selectAllButton);

		controlPanel.add(new JLabel("RGN Size"));
		controlPanel.add(selectSizePanel);
		controlPanel.add(cropButton);

		/* Move */
		JPanel movePanel = new JPanel(new GridLayout(1, 2));

		movePanel.add(moveXText);
		movePanel.add(moveYText);

		controlPanel.add(new JLabel("UR Shift"));
		controlPanel.add(movePanel);
		controlPanel.add(moveButton);

		/* Automation */
		JPanel delayTextPanel = new JPanel(new GridLayout(1, 2));

		delayTextPanel.add(delayText);
		delayTextPanel.add(new JLabel("ms"));

		controlPanel.add(new JLabel("Delay"));
		controlPanel.add(delayTextPanel);
		controlPanel.add(autoButton);

		/* Remains */
		controlPanel.add(randomizeButton);
		controlPanel.add(clearButton);
		controlPanel.add(advanceButton);
		controlPanel.add(torusCheckBox);
		controlPanel.add(mirrorButton);
		controlPanel.add(rotateButton);

		defineButton.addActionListener(this);
		selectAllButton.addActionListener(this);
		cropButton.addActionListener(this);
		moveButton.addActionListener(this);
		autoButton.addActionListener(this);
		advanceButton.addActionListener(this);
		randomizeButton.addActionListener(this);
		clearButton.addActionListener(this);
		mirrorButton.addActionListener(this);
		rotateButton.addActionListener(this);
		torusCheckBox.addActionListener(this);
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		/* Torus Mode */
		if (e.getSource() instanceof JCheckBox) {
			model.toggleTorus();
			return;
		}

		switch (((JButton) e.getSource()).getText()) {

		/* Auto Thread Running */
		case "\u25B6":
			try {
				int delay = Integer.parseInt(delayText.getText());
				if (delay < 1) {
					delayText.setText("1");
					return;
				}

				((JButton) e.getSource()).setText("\u23F8");
				model.toggleAutomation(delay);
			} catch (NumberFormatException f) {
				delayText.setText("1");
			}

			return;

		/* Auto Thread Not Running */
		case "\u23F8":
			try {
				int delay = Integer.parseInt(delayText.getText());
				if (delay < 1) {
					delayText.setText("1");
				}
			} catch (NumberFormatException f) {
				delayText.setText("1");
			}

			((JButton) e.getSource()).setText("\u25B6");
			model.toggleAutomation(1);
			return;

		case "ADV":
			model.advance();
			return;

		case "CLR":
			model.clear();
			return;

		case "Rand":
			model.randomize();
			return;

		case "Crop":
			try {
				model.crop(Integer.parseInt(startXText.getText()), Integer.parseInt(startYText.getText()),
						Integer.parseInt(gridWidthText.getText()), Integer.parseInt(gridHeightText.getText()));
			} catch (NumberFormatException f) {
				update("SelectAll", null);
			}

			return;

		case "Shift":
			try {
				model.moveUniverse(Integer.parseInt(moveXText.getText()), Integer.parseInt(moveYText.getText()));
			} catch (NumberFormatException f) {
				moveXText.setText("0");
				moveYText.setText("0");
			}

			return;

		case "Mirror":
			model.mirror();
			return;

		case "90°\u2B6E":
			model.rotate();
			return;

		case "SET":
			model.setThresholds(thresholdRanger(lowSurviveThresholdText), thresholdRanger(highSurviveThresholdText),
					thresholdRanger(lowBirthThresholdText), thresholdRanger(highBirthThresholdText));
			return;

		case "ALL":
			update("SelectAll", null);
			return;
		}
	}

	private int thresholdRanger(JTextField textField) {
		int validated;

		try {
			validated = Integer.parseInt(textField.getText());
		} catch (NumberFormatException e) {
			textField.setText("0");
			return 0;
		}

		if (validated < 0) {
			textField.setText("0");
			return 0;
		}
		if (validated > 8) {
			textField.setText("8");
			return 8;
		}

		return validated;
	}

	public void update(String s, Object o) {
		switch (s) {

		/* Object is of Integer */
		case "Live":
			aliveLabel.setText("A " + (int) o);
			return;

		/* Object is of Integer */
		case "Generation":
			generationLabel.setText("G " + (int) o);
			return;

		/* Object is null or of Integer[2] */
		case "Mouse":
			if (o != null) {
				mousePositionLabel.setText("[" + ((int[]) o)[0] + ", " + ((int[]) o)[1] + "]");
			} else {
				mousePositionLabel.setText("");
			}

			return;

		/* Object is null */
		case "Pack":
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					mainFrame.pack();
				}
			});

			return;

		/* Object is null */
		case "SelectAll":
			startXText.setText("0");
			startYText.setText("0");

			gridWidthText.setText(Integer.toString(model.getUniverseWidth()));
			gridHeightText.setText(Integer.toString(model.getUniverseHeight()));
		}
	}

}
