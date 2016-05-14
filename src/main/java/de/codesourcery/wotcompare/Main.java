package de.codesourcery.wotcompare;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Paint;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.apache.commons.lang.StringUtils;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.CategoryToolTipGenerator;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;


public class Main {

	private final SimpleAPIClient client;

	public static void main(String[] args) throws FileNotFoundException, IOException
	{
		final File configFile = args.length == 0 ? new File(".wotcompare") : new File( args[0] );
		new Main(configFile).run();
	}

	public Main(File configFile) throws FileNotFoundException, IOException
	{
		if ( ! configFile.exists() || ! configFile.canRead() ) {
			throw new IOException("Config file "+configFile.getAbsolutePath()+" does not exist or is not readable");
		}

		final Properties props = new Properties();
		props.load( new FileInputStream( configFile ) );
		final String appId = props.getProperty("application_id");

		if (StringUtils.isBlank(appId)) {
			throw new IOException("Configuration file "+configFile.getAbsolutePath()+" lacks value for 'application_id' property");
		}

		client = new SimpleAPIClient( ApplicationId.createInstance( appId ) );
		Runtime.getRuntime().addShutdownHook( new Thread( () -> client.dispose() ) );
	}

	public void run()
	{
		final Frame frame = new Frame();
		frame.setLocationRelativeTo( null );
		frame.setVisible(true);
	}

	protected final class TankModel extends AbstractTableModel {

		private final List<Tank> tanks=new ArrayList<>();

		public TankModel() {
		}

		public void setData(List<Tank> tanks) {
			if (tanks == null) {
				throw new IllegalArgumentException("tanks must not be NULL");
			}
			this.tanks.clear();
			this.tanks.addAll(tanks);
			fireTableDataChanged();
		}

		@Override
		public String getColumnName(int column)
		{
			switch(column) {
				case 0:
					return "Tier";
				case 1:
					return "Type";
				case 2:
					return "Nation";
				case 3:
					return "Name";
				default:
					throw new IllegalArgumentException("Invalid column "+column);
			}
		}

		@Override
		public int getRowCount() { return tanks.size(); }

		@Override
		public int getColumnCount() { return 4; }

		@Override
		public Object getValueAt(int row, int column)
		{
			final Tank t = tanks.get(row);
			switch(column) {
			case 0:
				return t.getTier();
			case 1:
				return t.getType();
			case 2:
				return t.getNation();
			case 3:
				return t.getName();
			default:
				throw new IllegalArgumentException("Invalid column "+column);
			}
		}
	}

	protected final class Frame extends JFrame
	{
		private final JComboBox<Integer> tierChooser;
		private final JComboBox<TankType> typeChooser;
		private final JComboBox<WotProperty> propertyChooser;
		private final TankModel tankModel = new TankModel();
		private final JTable tankTable = new JTable( tankModel );
		private final ChartPanel chartPanel;

		public Frame()
		{
			super("WoT Compare");
			setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

			// tier chooser
			final List<Integer> tierModel = new ArrayList<>();
			final int minTier = client.getMinTier();

			for ( int i = minTier ; i <= client.getMaxTier() ; i++ ) {
				tierModel.add( i );
			}
			tierChooser = new JComboBox<>( tierModel.toArray( new Integer[tierModel.size()]));
			tierChooser.setSelectedItem( minTier );

			// tank type chooser
			typeChooser = new JComboBox<>( TankType.values() );
			typeChooser.setSelectedItem( TankType.values()[0] );

			// setup property chooser
			final List<WotProperty> tankProperties = new ArrayList<>( WotProperty.getTankProperties() );
			tankProperties.sort( Comparator.comparing( prop -> prop.toString() ) );

			propertyChooser = new JComboBox<>( tankProperties.toArray( new WotProperty[ tankProperties.size() ] ) );
			propertyChooser.setSelectedItem( tankProperties.iterator().next() );

			// setup tank table
			final TableRowSorter<TableModel> sorter= new TableRowSorter<TableModel>(tankTable.getModel());
			tankTable.setRowSorter(sorter);

			// attach action listeners
			tierChooser.addActionListener( event -> refresh() );
			typeChooser.addActionListener( event -> refresh() );
			propertyChooser.addActionListener( event -> refresh() );

			// setup panel
			final JPanel panel = new JPanel();
			panel.setLayout( new GridBagLayout() );

			GridBagConstraints cnstrs = new GridBagConstraints();
			cnstrs.gridx = 0 ; cnstrs.gridy = 0;
			cnstrs.gridwidth = 1 ; cnstrs.gridheight = 1;
			cnstrs.fill = GridBagConstraints.HORIZONTAL;
			cnstrs.weightx = 0.33; cnstrs.weighty = 0.2;
			panel.add( tierChooser , cnstrs  );

			cnstrs = new GridBagConstraints();
			cnstrs.gridx = 1 ; cnstrs.gridy = 0;
			cnstrs.gridwidth = 1 ; cnstrs.gridheight = 1;
			cnstrs.fill = GridBagConstraints.HORIZONTAL;
			cnstrs.weightx = 0.33; cnstrs.weighty = 0.2;
			panel.add( typeChooser , cnstrs  );

			// add property chooser
			cnstrs = new GridBagConstraints();
			cnstrs.gridx = 2 ; cnstrs.gridy = 0;
			cnstrs.gridwidth = 1 ; cnstrs.gridheight = 1;
			cnstrs.fill = GridBagConstraints.HORIZONTAL;
			cnstrs.weightx = 0.33; cnstrs.weighty = 0.2;
			panel.add( propertyChooser , cnstrs );

			// add table
			cnstrs = new GridBagConstraints();
			cnstrs.gridx = 0 ; cnstrs.gridy = 1;
			cnstrs.gridwidth = 3 ; cnstrs.gridheight = 1;
			cnstrs.fill = GridBagConstraints.BOTH;
			cnstrs.weightx = 1; cnstrs.weighty = 0.2;
			panel.add( new JScrollPane(tankTable) , cnstrs );

			// add chart panel
			cnstrs = new GridBagConstraints();
			cnstrs.gridx = 0 ; cnstrs.gridy = 2;
			cnstrs.gridwidth = 3 ; cnstrs.gridheight = 1;
			cnstrs.fill = GridBagConstraints.BOTH;
			cnstrs.weightx = 1; cnstrs.weighty = 0.6;
			chartPanel = createChartPanel();
			panel.add( new JScrollPane( chartPanel ) , cnstrs );

			getContentPane().add( panel );

			// refresh view
			refresh();

			// set frame size
			setSize( new Dimension(640,480 ) );
			setPreferredSize( new Dimension(640,480 ) );
			pack();
		}

		private JFreeChart createChart(String title,
	            String categoryAxisLabel, String valueAxisLabel)
		{
			final DefaultCategoryDataset dataset = new DefaultCategoryDataset();

	        final CategoryAxis categoryAxis = new CategoryAxis(categoryAxisLabel);
	        final ValueAxis valueAxis = new NumberAxis(valueAxisLabel);

	        // setup renderer
	        final LineAndShapeRenderer renderer = new LineAndShapeRenderer(true, false)
	        {
	            @Override
				public Paint getItemFillPaint(int row, int column)
	            {
	            	final String tankShortname = (String) getPlot().getDataset().getColumnKey( column );
	            	return client.getTankByShortNameOrName( tankShortname ).getNation().getColor();
	            }
	        };

			renderer.setBaseShapesVisible(true);
            renderer.setDrawOutlines(false);
            renderer.setUseFillPaint(true);
            renderer.setBaseToolTipGenerator(new StandardCategoryToolTipGenerator());
            // renderer.setBaseItemURLGenerator(new StandardCategoryURLGenerator());

			final NumberFormat NF = new DecimalFormat("####0.0#");
			final CategoryToolTipGenerator tooltipGenerator = new StandardCategoryToolTipGenerator("{1} - {2}",NF) {
				@Override
				public String generateToolTip(CategoryDataset dataset, int row,int column)
				{
	            	final String tankShortname = (String) dataset.getColumnKey( column );
	            	final Tank tank = client.getTankByShortNameOrName( tankShortname );
	            	final Number value = dataset.getValue( row ,  column );
					return tank.getName()+" ("+tank.getNation()+") - "+NF.format( value );
				}
			};

			renderer.setSeriesToolTipGenerator( 0 ,  tooltipGenerator );

			// setup plot
	        final CategoryPlot plot = new CategoryPlot(dataset, categoryAxis, valueAxis,renderer);
	        plot.setOrientation(PlotOrientation.VERTICAL);

	        final JFreeChart chart = new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT,plot, true); // legend = on
	        ChartFactory.getChartTheme().apply( chart );
	        return chart;
		}

		private ChartPanel createChartPanel()
		{
			// create a chart...
			final JFreeChart chart = createChart("Title", "xAxisLabel", "yAxisLabel");

			final ChartPanel result = new ChartPanel( chart );
			result.setDisplayToolTips( true );
			result.setInitialDelay(1);
			result.setDismissDelay( 20000 );
			return result;
		}

		private void refresh()
		{
			// refresh tank table
			final int tier = (Integer) tierChooser.getSelectedItem();
			final TankType selectedType = (TankType) typeChooser.getSelectedItem();
			final List<Tank> filteredTanks = client.getTanks().stream()
					.filter( t -> t.getTier() == tier)
					.filter( t-> t.hasType( selectedType ) )
					.collect( Collectors.toList() );

			tankModel.setData(filteredTanks);

			// refresh JFreeChart
			final WotProperty property = (WotProperty) propertyChooser.getSelectedItem();
			final Map<Tank, List<TankDetails>> detailsByTank = client.getTankDetails( filteredTanks ).stream().collect( Collectors.groupingBy( detail -> detail.getTank() ) );

			final DefaultCategoryDataset dataset = new DefaultCategoryDataset();

			final Function<Tank,TankDetails> detailsSupplier = tank ->
				Optional.ofNullable( detailsByTank.get( tank ) ).orElseThrow( () -> new RuntimeException("Internal error, no details for tank "+tank) ).get(0);

			final Function<Tank,ValueWithUnit> valueSupplier = tank -> detailsSupplier.apply( tank ).getPropertyValue( property );

			// sort tanks ascending by property value
			final List<Tank> sortedTanks = (List<Tank>) filteredTanks.stream().sorted( Comparator.comparing( a -> valueSupplier.apply(a).getValue() ) ).collect( Collectors.toList() );

			sortedTanks.stream().forEach( tank ->
			{
				final ValueWithUnit value = valueSupplier.apply( tank );
				final String name = tank.isShortNameUnique() ? tank.getShortName() : tank.getName();
				dataset.addValue( (Number) value.getValue() , property.getUnit().toString() , name );
			});

			chartPanel.getChart().setTitle( property.toString() );
			final CategoryPlot plot = (CategoryPlot) chartPanel.getChart().getPlot();

			plot.getRangeAxis().setLabel( property.getUnit().toString() );
			plot.setDataset( dataset );
		}
	}
}
