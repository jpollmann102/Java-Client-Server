/* Name: Joshua Pollmann
 * Course: CNT4714 - Fall 2018
 * Assignment title: Program 3 - Java Client-Server
 * Date: Sunday October 14, 2018
 */

// Code below from Dr. Mark Llewellyn

// Display the results of queries against the bikes table in the bikedb database.
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;

public class ClientServer extends JFrame 
{
   // default query retrieves all data from bikes table
   static final String DEFAULT_QUERY = "SELECT * FROM bikes";
   
   private ResultSetTableModel tableModel;
   private JTextArea queryArea;
   
   // create ResultSetTableModel and GUI
   public ClientServer() 
   {   
      super( "SQL Client GUI - (JP - Fall 2018)" );
        
      // create ResultSetTableModel and display database table
      try 
      {
    	  
    	 JPanel topLevel = new JPanel(new BorderLayout());
    	 JPanel topPanel = new JPanel(new FlowLayout());
    	 JPanel topRightPanel = new JPanel(new FlowLayout());
    	 JPanel topLeftPanel = new JPanel(new FlowLayout());
    	 JPanel middlePanel = new JPanel(new FlowLayout());
    	 JPanel bottomPanel = new JPanel(new BorderLayout());
    	 
         // create TableModel for results of query SELECT * FROM bikes
         tableModel = new ResultSetTableModel( DEFAULT_QUERY );

         // set up JTextArea in which user types queries
         queryArea = new JTextArea( DEFAULT_QUERY, 3, 100 );
         queryArea.setWrapStyleWord(true);
         queryArea.setLineWrap(true);
         
         JScrollPane scrollPane = new JScrollPane( queryArea,
            ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, 
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED );
         
         // set up JButton for submitting queries
         JButton submitButton = new JButton( "Execute SQL Command" );
         submitButton.setBackground(Color.GREEN);
         submitButton.setForeground(Color.BLACK);
         submitButton.setBorder(new EmptyBorder(10,10,10,10));
         
         // set up JButton for connecting to DB
         JButton connectDBButton = new JButton( "Connect to Database" );
         connectDBButton.setBackground(Color.BLUE);
         connectDBButton.setForeground(Color.YELLOW);
         connectDBButton.setBorder(new EmptyBorder(10,10,10,10));
         
         // set up JButton for clearing SQL command
         JButton clearSQLButton = new JButton( "Clear SQL Command" );
         clearSQLButton.setBackground(Color.WHITE);
         clearSQLButton.setForeground(Color.RED);
         clearSQLButton.setBorder(new EmptyBorder(10,10,10,10));
         
         // set up JLabel for connection status
         JButton connectionStatus = new JButton("No Connection Now");
         connectionStatus.setBackground(Color.BLACK);
         connectionStatus.setForeground(Color.RED);
         connectionStatus.setBorder(new EmptyBorder(10,10,10,10));
         connectionStatus.setEnabled(false);
         
         JPanel buttonPanel = new JPanel();
         buttonPanel.add(connectionStatus);
         buttonPanel.add(connectDBButton);
         buttonPanel.add(clearSQLButton);
         buttonPanel.add(submitButton);
 
         // create Box to manage placement of queryArea in GUI
         Box commandBox = Box.createHorizontalBox();
         commandBox.setBorder(new EmptyBorder(10,10,10,10));
         commandBox.add(scrollPane);
         
         topRightPanel.add(commandBox);
         
         // create Box to manage placement of buttons
         Box buttonBox = Box.createHorizontalBox();
         buttonBox.setBorder(new EmptyBorder(10,10,10,10));
         buttonBox.setPreferredSize(new Dimension(700, 20));
         buttonBox.add(buttonPanel);
         
         middlePanel.add(buttonBox);

         // create JTable delegate for tableModel 
         JTable resultTable = new JTable( tableModel );
         resultTable.setBorder(new EmptyBorder(10,10,10,10));
         
         bottomPanel.add(resultTable);
         
         topPanel.add(topLeftPanel);
         topPanel.add(topRightPanel);
         
         // place GUI components on content pane
         topLevel.add(topPanel, BorderLayout.NORTH);
         topLevel.add(middlePanel, BorderLayout.CENTER);
         topLevel.add(bottomPanel, BorderLayout.SOUTH);
         
         add(topLevel);
         
         /*add( commandBox, BorderLayout.LINE_END );
         add( buttonBox, BorderLayout.LINE_START);
         add( new JScrollPane(resultTable), BorderLayout.SOUTH );*/

         // create event listener for submitButton
         submitButton.addActionListener( 
         
            new ActionListener() 
            {
               // pass query to table model
               public void actionPerformed( ActionEvent event )
               {
                  // perform a new query
                  try 
                  {
                     tableModel.setQuery( queryArea.getText() );
                  } // end try
                  catch ( SQLException sqlException ) 
                  {
                     JOptionPane.showMessageDialog( null, 
                        sqlException.getMessage(), "Database error", 
                        JOptionPane.ERROR_MESSAGE );
                     
                     // try to recover from invalid user query 
                     // by executing default query
                     try 
                     {
                        tableModel.setQuery( DEFAULT_QUERY );
                        queryArea.setText( DEFAULT_QUERY );
                     } // end try
                     catch ( SQLException sqlException2 ) 
                     {
                        JOptionPane.showMessageDialog( null, 
                           sqlException2.getMessage(), "Database error", 
                           JOptionPane.ERROR_MESSAGE );
         
                        // ensure database connection is closed
                        tableModel.disconnectFromDatabase();
         
                        System.exit( 1 ); // terminate application
                     } // end inner catch                   
                  } // end outer catch
               } // end actionPerformed
            }  // end ActionListener inner class          
         ); // end call to addActionListener

         setSize( 700, 600 ); // set window size
         //setResizable(false);
         setVisible( true ); // display window  
      } // end try
      catch ( ClassNotFoundException classNotFound ) 
      {
         JOptionPane.showMessageDialog( null, 
            "MySQL driver not found", "Driver not found",
            JOptionPane.ERROR_MESSAGE );
         
         System.exit( 1 ); // terminate application
      } // end catch
      catch ( SQLException sqlException ) 
      {
         JOptionPane.showMessageDialog( null, sqlException.getMessage(), 
            "Database error", JOptionPane.ERROR_MESSAGE );
               
         // ensure database connection is closed
         tableModel.disconnectFromDatabase();
         
         System.exit( 1 );   // terminate application
      } // end catch
      
      // dispose of window when user quits application (this overrides
      // the default of HIDE_ON_CLOSE)
      setDefaultCloseOperation( DISPOSE_ON_CLOSE );
      
      // ensure database connection is closed when user quits application
      addWindowListener(new WindowAdapter() 
         {
            // disconnect from database and exit when window has closed
            public void windowClosed( WindowEvent event )
            {
               tableModel.disconnectFromDatabase();
               System.exit( 0 );
            } // end method windowClosed
         } // end WindowAdapter inner class
      ); // end call to addWindowListener
   } // end DisplayQueryResults constructor
   
   // execute application
   public static void main( String args[] ) 
   {
      new ClientServer();     
   } // end main
} // end class DisplayQueryResults