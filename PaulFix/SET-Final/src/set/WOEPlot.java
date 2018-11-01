package set;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.Timer;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.DynamicTimeSeriesCollection;
import org.jfree.data.time.Hour;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;
import au.com.bytecode.opencsv.CSVReader;

/** @see http://stackoverflow.com/questions/5048852 */
public class WOEPlot extends ApplicationFrame {

	private static final long serialVersionUID = 1L;
	private static final String TITLE = "Usage vs Time";
	private static final String START = "Start";
	private static final String STOP = "Stop";
	private static final int COUNT = 30 / 2;
	private static final int FAST = 100;
	private static final int SLOW = FAST * 5;
	private float x, y;
	private static final Random random = new Random();
	private Timer timer;

	CSVReader reader;
	String[] readNextLine;

	public WOEPlot(final String title) {
		super(title);

		final DynamicTimeSeriesCollection dataset = new DynamicTimeSeriesCollection(2, COUNT, new Hour());

		dataset.setTimeBase(new Hour());
		dataset.addSeries(plotData(), 0, "1");
		dataset.addSeries(plotData(), 1, "2");

		JFreeChart chart = createChart(dataset);
		ChartPanel chartPanel = new ChartPanel(chart);

		final JButton run = new JButton(STOP);
		run.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				String cmd = e.getActionCommand();
				if (STOP.equals(cmd)) {
					timer.stop();
					run.setText(START);
				} else {
					timer.start();
					run.setText(STOP);
				}
			}
		});

		final JComboBox combo = new JComboBox();
		combo.addItem("Fast");
		combo.addItem("Slow");
		combo.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if ("Fast".equals(combo.getSelectedItem())) {
					timer.setDelay(FAST);
				} else {
					timer.setDelay(SLOW);
				}
			}
		});

		JPanel btnPanel = new JPanel(new FlowLayout());
		btnPanel.add(run);
		btnPanel.add(combo);
		this.add(btnPanel, BorderLayout.SOUTH);

		timer = new Timer(SLOW, new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				float newpt[] = new float[2];
				newpt[1] = y;
				newpt[0] = x;
				dataset.advanceTime();
				dataset.appendData(newpt);
			}
		});

		chartPanel.setPreferredSize(new Dimension(800, 600));
		this.add(chartPanel);
	}

	public void dataUpdate(double a, double b) {
		x = (float)a;
		y = (float)b;
	}

	private float[] plotData() {
		float[] firstPoint = new float[COUNT];
//		for (int i = 0; i < firstPoint.length; i++) {
//			firstPoint[i] = dataUpdate();
//		}
		return firstPoint;
	}

	private JFreeChart createChart(final XYDataset dataset) {
		final JFreeChart result = ChartFactory.createTimeSeriesChart(TITLE, "HH", "Watthours", dataset, true, true,
				false);

		final XYPlot plot = result.getXYPlot();

		ValueAxis domain = plot.getDomainAxis();
		domain.getDefaultAutoRange();
		ValueAxis range = plot.getRangeAxis();
		range.getDefaultAutoRange();
		
		return result;
	}

	public void start() {
		timer.start();
	}

	public static void main(final String[] args) {
		EventQueue.invokeLater(new Runnable() {

			@Override
			public void run() {
				WOEPlot demo = new WOEPlot(TITLE);
				demo.pack();
				RefineryUtilities.centerFrameOnScreen(demo);
				demo.setVisible(true);
				demo.start();
			}
		});
	}
}