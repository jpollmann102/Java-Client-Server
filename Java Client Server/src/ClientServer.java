/* Name: Joshua Pollmann
 * Course: CNT4714 - Fall 2018
 * Assignment title: Program 3 - Java Client-Server
 * Date: Sunday October 14, 2018
 */

// Code below from Dr. Mark Llewellyn

// Display the results of queries against the bikes table in the bikedb database.
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;

import com.mysql.cj.jdbc.MysqlDataSource;

public class ClientServer extends JFrame {

	private ResultSetTableModel tableModel;
	private JTextArea queryArea;
	private JTable resultTable;
	private Connection connection;
	private Statement statement;
	String username = "";
	private String password = "";
	private boolean connectedToDatabase = false;

	// create ResultSetTableModel and GUI
	public ClientServer() {
		super("SQL Client GUI - (JP - Fall 2018)");

		// create ResultSetTableModel and display database table
		try {

			// set up JTextArea in which user types queries
			queryArea = new JTextArea("", 3, 50);
			queryArea.setWrapStyleWord(true);
			queryArea.setLineWrap(true);

			JScrollPane scrollPane = new JScrollPane(queryArea, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
					ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

			// set up JButton for submitting queries
			JButton submitButton = new JButton("Execute SQL Command");
			submitButton.setBackground(Color.GREEN);
			submitButton.setForeground(Color.BLACK);

			// set up JButton for connecting to DB
			JButton connectDBButton = new JButton("Connect to Database");
			connectDBButton.setBackground(Color.BLUE);
			connectDBButton.setForeground(Color.YELLOW);

			// set up JButton for clearing SQL command
			JButton clearSQLButton = new JButton("Clear SQL Command");
			clearSQLButton.setBackground(Color.WHITE);
			clearSQLButton.setForeground(Color.RED);

			// set up JLabel for connection status
			JButton connectionStatus = new JButton("No Connection Now");
			connectionStatus.setBackground(Color.BLACK);
			connectionStatus.setForeground(Color.RED);
			connectionStatus.setEnabled(false);

			JPanel buttonPanel = new JPanel();
			buttonPanel.add(connectionStatus);
			buttonPanel.add(connectDBButton);
			buttonPanel.add(clearSQLButton);
			buttonPanel.add(submitButton);

			String[] driverStrings = { "com.mysql.cj.jdbc.Driver", "oracle.jdbc.driver.OracleDriver",
					"com.ibm.db2.jdbc.netDB2Driver", "com.jdbc.odbc.jdbcOdbcDriver" };
			String[] urlStrings = { "jdbc:mysql://localhost:3312/project3", "jdbc:mysql://localhost:3306/project3",
					"jdbc:mysql://localhost:3310/bikedb", "jdbc:mysql://localhost:3312/test" };

			JComboBox driverBox = new JComboBox(driverStrings);
			driverBox.setSelectedIndex(0);

			JComboBox urlBox = new JComboBox(urlStrings);
			driverBox.setSelectedIndex(0);

			JPanel dropdownPanel = new JPanel();
			dropdownPanel.add(driverBox);
			dropdownPanel.add(urlBox);

			JTextField username = new JTextField(10);
			username.setText("");
			JPasswordField password = new JPasswordField(10);
			password.setText("");

			JPanel userPassPanel = new JPanel();
			userPassPanel.add(username);
			userPassPanel.add(password);

			// create Box to manage placement of drop downs
			Box dropdownBox = Box.createHorizontalBox();
			dropdownBox.add(dropdownPanel);

			// create Box to manage placement of username/pass
			Box userPassBox = Box.createHorizontalBox();
			userPassBox.add(userPassPanel);

			// create Box to manage placement of queryArea in GUI
			Box commandBox = Box.createHorizontalBox();
			commandBox.add(scrollPane);

			// create Box to manage placement of buttons
			Box buttonBox = Box.createHorizontalBox();
			buttonBox.add(buttonPanel);

			// create JTable delegate for tableModel
			resultTable = new JTable();

			JButton clearResultButton = new JButton("Clear Result Window");
			clearResultButton.setBackground(Color.YELLOW);

			JScrollPane resultPane = new JScrollPane(resultTable);
			resultPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			resultPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

			setLayout(new FlowLayout());
			add(driverBox);
			add(urlBox);
			add(username);
			add(password);
			add(commandBox);
			add(buttonBox);
			add(resultPane);
			add(clearResultButton);

			// create event listener for clearSQLButton
			clearSQLButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					queryArea.setText("");
				}
			});

			clearResultButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					tableModel.clearTable();
				}
			});

			connectDBButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try {
						if (username.getText().length() > 0 && password.getPassword().length > 0) {
							updateProperties(driverBox.getSelectedItem().toString(),
									urlBox.getSelectedItem().toString(), username.getText(), password.getPassword());
							if (connectToDatabase()) {
								connectionStatus.setText("Connected to " + urlBox.getSelectedItem().toString());
							}
						}
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (SQLException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			});

			// create event listener for submitButton
			submitButton.addActionListener(

					new ActionListener() {
						// pass query to table model
						public void actionPerformed(ActionEvent event) {
							try {
								tableModel = new ResultSetTableModel(connection, statement, connectedToDatabase);
								System.out.println("created table model");
							} catch (ClassNotFoundException | SQLException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							// perform a new query
							try {
								tableModel.setQuery(queryArea.getText());
								System.out.println("Set the query");
								resultTable = new JTable(tableModel);
							} // end try
							catch (SQLException sqlException) {
								JOptionPane.showMessageDialog(null, sqlException.getMessage(), "Database error",
										JOptionPane.ERROR_MESSAGE);

								// try to recover from invalid user query
								// by executing default query
								try {
									tableModel.setQuery("SELECT * FROM bikes");
									resultTable = new JTable(tableModel);
									queryArea.setText("SELECT * FROM bikes");
								} // end try
								catch (SQLException sqlException2) {
									JOptionPane.showMessageDialog(null, sqlException2.getMessage(), "Database error",
											JOptionPane.ERROR_MESSAGE);

									// ensure database connection is closed
									tableModel.disconnectFromDatabase();

									System.exit(1); // terminate application
								} // end inner catch
							} // end outer catch
						} // end actionPerformed
					} // end ActionListener inner class
			); // end call to addActionListener

			setSize(850, 600); // set window size
			setResizable(false);
			setVisible(true); // display window
		} finally {
		} // end try
		/*
		 * catch ( ClassNotFoundException classNotFound ) {
		 * JOptionPane.showMessageDialog( null, "MySQL driver not found",
		 * "Driver not found", JOptionPane.ERROR_MESSAGE );
		 * 
		 * System.exit( 1 ); // terminate application } // end catch catch (
		 * SQLException sqlException ) { JOptionPane.showMessageDialog( null,
		 * sqlException.getMessage(), "Database error", JOptionPane.ERROR_MESSAGE );
		 * 
		 * // ensure database connection is closed tableModel.disconnectFromDatabase();
		 * 
		 * System.exit( 1 ); // terminate application } // end catch
		 */
		// dispose of window when user quits application (this overrides
		// the default of HIDE_ON_CLOSE)
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		// ensure database connection is closed when user quits application
		addWindowListener(new WindowAdapter() {
			// disconnect from database and exit when window has closed
			public void windowClosed(WindowEvent event) {
				tableModel.disconnectFromDatabase();
				System.exit(0);
			} // end method windowClosed
		} // end WindowAdapter inner class
		); // end call to addWindowListener
	} // end DisplayQueryResults constructor

	static void updateProperties(String driver, String url, String username, char[] password) throws IOException {
		File f = new File("db.properties");
		FileWriter w = new FileWriter(f);
		w.write("# MySQL DB Properties\n");
		w.write("#\n");
		w.write("MYSQL_DB_DRIVER_CLASS=" + driver + "\n");
		w.write("MYSQL_DB_URL=" + url + "\n");
		w.write("MYSQL_DB_USERNAME=" + username + "\n");
		w.write("MYSQL_DB_PASSWORD=");
		for (int i = 0; i < password.length; i++) {
			w.write(password[i]);
		}
		w.close();
	}

	static void alertUser(String message) {
		JOptionPane.showMessageDialog(null, message);
	}

	private boolean connectToDatabase() throws SQLException {
		Properties properties = new Properties();
		FileInputStream filein = null;
		MysqlDataSource dataSource = null;

		// read properties file
		try {
			filein = new FileInputStream("db.properties");
			properties.load(filein);
			dataSource = new MysqlDataSource();
			dataSource.setURL(properties.getProperty("MYSQL_DB_URL"));
			dataSource.setUser(properties.getProperty("MYSQL_DB_USERNAME"));
			dataSource.setPassword(properties.getProperty("MYSQL_DB_PASSWORD"));

			// connect to database bikes and query database
			// establish connection to database
			connection = dataSource.getConnection();

			// create Statement to query database
			statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

			// update database connection status
			connectedToDatabase = true;
			return true;
		} // end try
		catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	// execute application
	public static void main(String args[]) {
		new ClientServer();
	} // end main
} // end class DisplayQueryResults