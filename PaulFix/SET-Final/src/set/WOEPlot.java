package set;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
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
	private static final int COUNT = 30 / 2;
	private float x, y;
	private static final Random random = new Random();
	private Timer timer;
	private int counter = 0;
	private JTabbedPane PLot1;
	private final DynamicTimeSeriesCollection dataset;
	private JTextArea logger = new JTextArea();
	private JTextField userBuyMin;
	private JTextField userBuyMax;
	private JTextField userSellMin;
	private JTextField userSellMax;
	private JLabel luserBuyMin;
	private JLabel luserBuyMax;
	private JLabel luserSellMin;
	private JLabel luserSellMax;
	private JTextArea paid = new JTextArea();

	CSVReader reader;
	String[] readNextLine;

	public WOEPlot(final String title) {
		super(title);
		dataset = new DynamicTimeSeriesCollection(2, COUNT, new Hour());
		dataset.setTimeBase(new Hour());
		dataset.addSeries(plotData(), 0, "Predicted");
		dataset.addSeries(plotData(), 1, "Actual");
		paid.setText("0");
		userBuyMin = new JTextField();
		userBuyMax = new JTextField();
		userSellMin = new JTextField();
		userSellMax = new JTextField();
		luserBuyMin = new JLabel("Minimum buy price",JLabel.LEFT);
		luserBuyMax = new JLabel("Maximum buy price",JLabel.LEFT);
		luserSellMin = new JLabel("Minimum sell price",JLabel.LEFT);
		luserSellMax = new JLabel("Maximum sell price",JLabel.LEFT);
		JFreeChart chart = createChart(dataset);
		ChartPanel chartPanel = new ChartPanel(chart);

		timer = new Timer(1000, new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				float newpt[] = new float[2];
				newpt[1] = y;
				newpt[0] = x;
				dataset.appendData(newpt);
				System.out.println("TRIAL");
			}
		});
		JPanel x = new JPanel();
		x.add(chartPanel);
		PLot1 = new JTabbedPane();
		PLot1.addTab("Graph", x);
		ScrollingTextArea y = new ScrollingTextArea(logger);
		/*logger.setBounds(5, 5, 1000, 1000);
		y.add(logger);
		JScrollPane scroll = new JScrollPane (logger);
	    scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
	    scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);    
		y.add(scroll);*/
		
		PLot1.addTab("Logger", y);
		JPanel settings = new JPanel();
		settings.add(new JLabel("Paid so Far"));
		settings.add(paid);
		settings.add(luserBuyMin);
		settings.add(userBuyMin);
		settings.add(luserBuyMax);
		settings.add(userBuyMax);
		settings.add(luserSellMin);
		settings.add(userSellMin);
		settings.add(luserSellMax);
		settings.add(userSellMax);
		
		
		PLot1.addTab("Settings", settings);
		this.add(PLot1);
	}
	
	public void updateBalance(double a)
	{
		double b = Double.parseDouble(paid.getText()) + a;
		paid.setText(Double.toString(b));
	}
	
	public class ScrollingTextArea extends JPanel {
		 
		JTextArea txt = new JTextArea();
		JScrollPane scrolltxt;
	 
		public ScrollingTextArea(JTextArea a) {
	 
			setLayout(null);
			txt = a;
			scrolltxt = new JScrollPane(txt);
			scrolltxt.setBounds(3, 3, Toolkit.getDefaultToolkit().getScreenSize().width, 1080);
			add(scrolltxt);		
		}
	}

	public void updateLog(String msg)
	{
		logger.append(msg);
		logger.append("\n");
	}
	public void dataUpdate(double a, double b) {
		x = (float)a;
		y = (float)b;
		dataset.advanceTime();
		/*switch(type)
		{
		case "HA":
			dataset.advanceTime();
			break;
		case "FGE":
			aa1.advanceTime();
			break;
		case "TVE":
			aa2.advanceTime();
			break;
		case "SPA":
			aa3.advanceTime();
			break;
		case "WOE":
			aa4.advanceTime();
			break;
		case "HTE":
			aa5.advanceTime();
			break;
		}*/
	//	System.out.println(x + " " + y);
	}

	public void pushData(String a, String b, String c, String d)
	{
		userBuyMin.setText(b);
		userBuyMax.setText(a);
		userSellMin.setText(c);
		userSellMax.setText(d);
	}
	
	public String getMinBuy()
	{
		return userBuyMin.getText();
	}
	
	public String getMaxBuy()
	{
		return userBuyMax.getText();
	}
	
	public String getMinSell()
	{
		return userSellMin.getText();
	}
	
	public String getMaxSell()
	{
		return userSellMax.getText();
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