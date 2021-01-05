import java.io.*;
import java.sql.*;

import java.util.Properties;
import java.util.ArrayList;

import java.text.DecimalFormat;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;


class Assignment {

	private static String readEntry(String prompt) {
		try {
			StringBuffer buffer = new StringBuffer();
			System.out.print(prompt);
			System.out.flush();
			int c = System.in.read();
			while(c != '\n' && c != -1) {
				buffer.append((char)c);
				c = System.in.read();
			}
			return buffer.toString().trim();
		} catch (IOException e) {
			return "";
		}
 	}

	public static void main(String args[]) throws SQLException, IOException {
		// Fetch the connection details
		Connection conn = getConnection();

		// Loop menu: read in user data and call the appropriate option 
		do {
			//Print Menu
			System.out.println(); //Menu is printed on a separate line to previous message.
			System.out.println("Menu:");
			System.out.println("(1) In-Store Purchases");
			System.out.println("(2) Collection");
			System.out.println("(3) Delivery");
			System.out.println("(4) Biggest Sellers");
			System.out.println("(5) Reserved Stock");
			System.out.println("(6) Staff Life-Time Success");
			System.out.println("(7) Staff Contribution");
			System.out.println("(8) Employees of the Year");
			System.out.println("(0) Quit");

			// Wait for user input
			String selectedOption = readEntry("Enter your choice: ");

			//Changes date format
			DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MMM-yy");

			// Act on selected option
			if (selectedOption.charAt(0) == '1') {
				//User input
				int productQuantity[][] = OrderMenu();
				String orderDate = readEntry("Enter the date sold: ");
				String staffID = readEntry("Enter your staff ID: ");

				//If staffID is invalid then option1 does not run
				try {
					int id = Integer.parseInt(staffID);
					option1(conn, productQuantity[0], productQuantity[1], orderDate, id);
				} catch (Exception e) {
					System.err.println("ERROR: Invalid staffID input. Cannot continue with Option1.");
				}

			} else if (selectedOption.charAt(0) == '2') {
				//User input
				int productQuantity[][] = OrderMenu();
				String orderDate = readEntry("Enter the date sold: ");
				String collectionDate = readEntry("Enter the date of collection: ");
				String fName = readEntry("Enter the first name of the collector: ");
				String LName = readEntry("Enter the last name of the collector: ");
				String staffID = readEntry("Enter your staff ID: ");

				//If staffID is invalid then option2 does not run
				try {
					int id = Integer.parseInt(staffID);

					//Check collection date is not before date in which order is placed
					try {
						Date order = formatOrderDate(orderDate);
						Date collection = java.sql.Date.valueOf(LocalDate.parse(collectionDate, dateFormatter));
						if (collection.compareTo(order) > 0 || collection.compareTo(order) == 0) {
							option2(conn, productQuantity[0], productQuantity[1], orderDate, collectionDate, fName, LName, id);
						} else {
							System.err.println("ERROR: Collection date occurs before the date order is placed. Cannot continue with Option2.");
						}
					} catch (Exception ex) {
						System.err.println("ERROR: Invalid collection input. Cannot continue with Option2.");
					}
	
					
				} catch (Exception e) {
					System.err.println("ERROR: Invalid staffID input. Cannot continue with Option2.");
				}

			} else if (selectedOption.charAt(0) == '3') {
				//User input
				int productQuantity[][] = OrderMenu();
				String orderDate = readEntry("Enter the date sold: ");
				String deliveryDate = readEntry("Enter the date of delivery: ");
				String fName = readEntry("Enter the first name of the collector: ");
				String LName = readEntry("Enter the last name of the collector: ");
				String house = readEntry("Enter the house name/no: ");
				String street = readEntry("Enter the street: ");
				String city = readEntry("Enter the City: ");
				String staffID = readEntry("Enter your staff ID: ");

				//If staffID is invalid then option3 does not run
				try {
					int id = Integer.parseInt(staffID);

					//Check delivery date is not before date in which order is placed
					try {
						Date order = formatOrderDate(orderDate);
						Date delivery = java.sql.Date.valueOf(LocalDate.parse(deliveryDate, dateFormatter));
						if (delivery.compareTo(order) > 0 || delivery.compareTo(order) == 0) {
							option3(conn, productQuantity[0], productQuantity[1], orderDate, deliveryDate, fName, LName, house, street, city, id);
						} else {
							System.err.println("ERROR: Delivery date occurs before the date order is placed. Cannot continue with Option3.");
						}
					} catch (Exception ex) {
						System.err.println("ERROR: Invalid delivery date input. Cannot continue with Option3.");
					}

				} catch (Exception e) {
					System.err.println("ERROR: Invalid staffID input. Cannot continue with Option3.");
				}

			} else if (selectedOption.charAt(0) == '4') {
				option4(conn);
			} else if (selectedOption.charAt(0) == '5') {
				String date = readEntry("Enter the date: ");
				option5(conn, date);
			} else if (selectedOption.charAt(0) == '6') {
				option6(conn);
			} else if (selectedOption.charAt(0) == '7') {
				option7(conn);
			} else if (selectedOption.charAt(0) == '8') {
				String year = readEntry("Enter the year: ");
				
				//If year is invalid then option8 does not run
				try {
					int yr = Integer.parseInt(year);
					//Check year format (YYYY)
					if (year.length() == 4) {
						option8(conn, yr);
					} else {
						System.err.println("ERROR: the year provided is not in the correct format YYYY.");
						System.err.println("Cannot run option8.");
					}
					
				} catch (Exception e) {
					System.err.println("ERROR: Invalid year input. Cannot continue with Option8.");
				}
				
			} else if (selectedOption.charAt(0) == '0') {
				System.out.println("Exiting...");
				break;
			} else {
				System.out.println("No valid option given...");
			}  
		} while(true);
         
		conn.close();
	}

	public static int[][] OrderMenu(){
		//Define array lists for product IDs and the quantities
		ArrayList<Integer> Products = new ArrayList<Integer>();
		ArrayList<Integer> Quantities = new ArrayList<Integer>();

		//looping menu for IDs and quantities
		do {
			String productID = readEntry("Enter a Product ID: ");
			String productQuantity = readEntry("Enter the quantity sold: ");
			
			//Try parsing strings to integers
			try{
				int pID = Integer.parseInt(productID);
				Products.add(pID);

				int quantity = Integer.parseInt(productQuantity);
				Quantities.add(quantity);
			} catch (Exception e) {
				System.err.println("ERROR: Invalid ID or quantity input. Cannot be stored.");
			}
			
			String nextProduct = readEntry("Is there another product in the order?: ");
			if (!nextProduct.equals("Y")){
				if(!nextProduct.equals("N")){
					System.out.println("Invalid response. Next time select Y or N only.");
				}
				break;
			}
		} while (true);

		//Arraylists to arrays
		int[][] productQuantity = new int[2][Products.size()];
		for(int i=0; i < Products.size(); i++){
			productQuantity[0][i] = Products.get(i);
			productQuantity[1][i] = Quantities.get(i);
		}

		return productQuantity;
	}
	
	/**
	* @param conn An open database connection 
	* @param OrderID The id of an order made
	*/
	public static void Stock(Connection conn, int OrderID){
		//Find the stock left of products in the given order

		String selectStockStatement = "SELECT I.ProductID, I.ProductStockAmount "
									+	" FROM INVENTORY I "
									+	" JOIN ORDER_PRODUCTS OP "
									+	" ON I.ProductID = OP.ProductID"
									+   " JOIN ORDERS O"
									+   " ON OP.OrderID = O.OrderID"
									+   " WHERE O.OrderID = ?";

		try {
			//Prepared Statement
			PreparedStatement p = conn.prepareStatement(selectStockStatement);
			
			//Set OrderId in p
			p.setInt(1, OrderID);

			ResultSet r = p.executeQuery();
			//Print out updated stock quantities
			while(r.next()){
				System.out.println("Product ID " + r.getInt(1) + " stock is now at " + r.getInt(2) + ".");
			}

			p.clearParameters();
			p.close(); r.close();

		} catch (SQLException e) {
			//SELECT statement won't work if there is no products in the order
			System.err.println();
			System.err.println("There are no products in the current order");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	* @param date A string in the form of 'DD-Mon-YY' that represents the date the order was made 
	*/
	public static Date formatOrderDate(String date) {
		//Changes date format
		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MMM-yy");
		Date formattedDate;

		try {
			//Try parsing string to date object
			formattedDate = java.sql.Date.valueOf(LocalDate.parse(date, dateFormatter));
		} catch (Exception e) {
			System.err.println("ERROR: Invalid order date (when product was sold) input.");
			System.err.println("Instead assume order was placed today. Setting the order date to today...");
			//Invalid user input: select current date
			long m = System.currentTimeMillis();  
			java.sql.Date now = new java.sql.Date(m);    
			formattedDate = now;
		}

		return formattedDate;
	}

	/**
	* @param conn An open database connection 
	* @param productIDs An array of productIDs associated with an order
    * @param quantities An array of quantities of a product. The index of a quantity correspeonds with an index in productIDs
	* @param orderDate A string in the form of 'DD-Mon-YY' that represents the date the order was made
	* @param staffID The id of the staff member who sold the order
	*/
	public static void option1(Connection conn, int[] productIDs, int[] quantities, String orderDate, int staffID) {
		//SQL statements
		String InsertOrderStatement = "INSERT INTO ORDERS VALUES (null, 'InStore', 1, ?)";
		String SelectOrderIDStatement = "SELECT currval('OrderSequence')";
		String InsertProductOrderStatement = "INSERT INTO ORDER_PRODUCTS VALUES (?, ?, ?)";
		String InsertStaffOrderStatement = "INSERT INTO STAFF_ORDERS VALUES (?, ?)";

        try {
			//Prepared Statements
			PreparedStatement p1 = conn.prepareStatement(InsertOrderStatement);
			PreparedStatement p2 = conn.prepareStatement(SelectOrderIDStatement);
			PreparedStatement p3 = conn.prepareStatement(InsertProductOrderStatement);
			PreparedStatement p4 = conn.prepareStatement(InsertStaffOrderStatement);

			conn.setAutoCommit(false);

			//Insert new order into ORDERS statement
			p1.setDate(1, formatOrderDate(orderDate));
			p1.executeUpdate();

			//get current value of OrderSequence 
			ResultSet r = p2.executeQuery(); 
			r.next();
			//Set OrderID into ORDER_PRODUCTS, STAFF_ORDERS statement
			p3.setInt(1, r.getInt(1));
			p4.setInt(2, r.getInt(1));

			//Insert new row into STAFF_ORDERS 
			p4.setInt(1, staffID);
			p4.executeUpdate();

            //Loop over each product ordered
			for (int i=0; i < productIDs.length; i++) {
				try {
					//Set ID and quantity (per row) and commit if execution completes
					p3.setInt(2, productIDs[i]);
					p3.setInt(3, quantities[i]);
					p3.executeUpdate();
					conn.commit(); 
				} catch (SQLException ex) {
					//Error in executeUpdate()
					System.err.format("SQL State: %s\n%s", ex.getSQLState(), ex.getMessage());
					System.out.println();
					System.err.println("ERROR: (1) Not enough stock for Product " + productIDs[i]);
					System.err.println("   OR: (2) Product " + productIDs[i] + " does not exist in inventory.");
					System.err.println("This product (" + productIDs[i] + ") won't be part of the current order.");
				}
			} 

			//Print out new product stocks
			Stock(conn, r.getInt(1));

			p1.clearParameters(); p3.clearParameters(); p4.clearParameters();
			p1.close(); p2.close(); p3.close(); p4.close();
			r.close();

        } catch (SQLException e) {
			System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
			//In case of any SQL error do not commit to the database
			if (conn != null) {
				try {
					System.out.println();
					System.err.println("Transaction is being rolled back");
					conn.rollback();
				} catch (SQLException excep) {
					System.err.format("SQL State: %s\n%s", excep.getSQLState(), excep.getMessage());
				}
			}
        } catch (Exception e) {
            e.printStackTrace();
		}
	}

	/**
	* @param conn An open database connection 
	* @param productIDs An array of productIDs associated with an order
    * @param quantities An array of quantities of a product. The index of a quantity correspeonds with an index in productIDs
	* @param orderDate A string in the form of 'DD-Mon-YY' that represents the date the order was made
	* @param collectionDate A string in the form of 'DD-Mon-YY' that represents the date the order will be collected
	* @param fName The first name of the customer who will collect the order
	* @param LName The last name of the customer who will collect the order
	* @param staffID The id of the staff member who sold the order
	*/
	public static void option2(Connection conn, int[] productIDs, int[] quantities, String orderDate, String collectionDate, String fName, String LName, int staffID) {
		//SQL Statements
		String InsertOrderStatement = "INSERT INTO ORDERS VALUES (null, 'Collection', 0, ?)";
		String SelectOrderIDStatement = "SELECT currval('OrderSequence')";
		String InsertProductOrderStatement = "INSERT INTO ORDER_PRODUCTS VALUES (?, ?, ?)";
		String InsertStaffOrderStatement = "INSERT INTO STAFF_ORDERS VALUES (?, ?)";
		String InsertCollectionStatement = "INSERT INTO COLLECTIONS VALUES (?, ?, ?, ?)";

		//Changes date format
		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MMM-yy");

		try {
			//Prepared Statements
			PreparedStatement p1 = conn.prepareStatement(InsertOrderStatement);
			PreparedStatement p2 = conn.prepareStatement(SelectOrderIDStatement);
			PreparedStatement p3 = conn.prepareStatement(InsertProductOrderStatement);
			PreparedStatement p4 = conn.prepareStatement(InsertStaffOrderStatement);
			PreparedStatement p5 = conn.prepareStatement(InsertCollectionStatement);

			conn.setAutoCommit(false);

			//Insert new order into ORDERS
			p1.setDate(1, formatOrderDate(orderDate));
			p1.executeUpdate();

			//get current value of OrderSequence
			ResultSet r = p2.executeQuery(); 
			r.next();
			//Set OrderID into ORDER_PRODUCTS, STAFF_ORDERS, COLLECTIONS statements
			p3.setInt(1, r.getInt(1));
			p4.setInt(2, r.getInt(1));
			p5.setInt(1, r.getInt(1));

			//Insert new row into STAFF_ORDERS
			p4.setInt(1, staffID);
			p4.executeUpdate();

			//Insert new row into COLLECTIONS
			p5.setString(2, fName);
			p5.setString(3, LName);
			p5.setDate(4, java.sql.Date.valueOf(LocalDate.parse(collectionDate, dateFormatter)));
			p5.executeUpdate();

            //Loop over each product ordered
			for (int i=0; i < productIDs.length; i++) {
				try {
					//Set ID and quantity (per row) and commit if execution completes
					p3.setInt(2, productIDs[i]);
					p3.setInt(3, quantities[i]);
					p3.executeUpdate();
					conn.commit(); 
				} catch (SQLException ex) {
					//Error in executeUpdate()
					System.err.format("SQL State: %s\n%s", ex.getSQLState(), ex.getMessage());
					System.out.println();
					System.err.println("ERROR: (1) Not enough stock for Product " + productIDs[i]);
					System.err.println("   OR: (2) Product " + productIDs[i] + " does not exist in inventory.");
					System.err.println("This product (" + productIDs[i] + ") won't be part of the current order.");
				}
			} 

			//Print out new product stocks
			Stock(conn, r.getInt(1));

			p1.clearParameters(); p3.clearParameters(); p4.clearParameters(); p5.clearParameters();
			p1.close(); p2.close(); p3.close(); p4.close(); p5.close();
			r.close();

        } catch (SQLException e) {
			System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
			//In case of any SQL error do not commit to the database
			if (conn != null) {
				try {
					System.out.println();
					System.err.println("Transaction is being rolled back");
					conn.rollback();
				} catch (SQLException excep) {
					System.err.format("SQL State: %s\n%s", excep.getSQLState(), excep.getMessage());
				}
			}
        } catch (Exception e) {
            e.printStackTrace();
		}

	}

	/**
	* @param conn An open database connection 
	* @param productIDs An array of productIDs associated with an order
    * @param quantities An array of quantities of a product. The index of a quantity correspeonds with an index in productIDs
	* @param orderDate A string in the form of 'DD-Mon-YY' that represents the date the order was made
	* @param deliveryDate A string in the form of 'DD-Mon-YY' that represents the date the order will be delivered
	* @param fName The first name of the customer who will receive the order
	* @param LName The last name of the customer who will receive the order
	* @param house The house name or number of the delivery address
	* @param street The street name of the delivery address
	* @param city The city name of the delivery address
	* @param staffID The id of the staff member who sold the order
	*/
	public static void option3(Connection conn, int[] productIDs, int[] quantities, String orderDate, String deliveryDate, String fName, String LName,
				   String house, String street, String city, int staffID) {
		//SQL statements
		String InsertOrderStatement = "INSERT INTO ORDERS VALUES (null, 'Delivery', 0, ?)";
		String SelectOrderIDStatement = "SELECT currval('OrderSequence')";
		String InsertProductOrderStatement = "INSERT INTO ORDER_PRODUCTS VALUES (?, ?, ?)";
		String InsertStaffOrderStatement = "INSERT INTO STAFF_ORDERS VALUES (?, ?)";
		String InsertDeliveryStatement = "INSERT INTO DELIVERIES VALUES (?, ?, ?, ?, ?, ?, ?)";

		//Changes date format
		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MMM-yy");

		try {
			//Prepared Statements
			PreparedStatement p1 = conn.prepareStatement(InsertOrderStatement);
			PreparedStatement p2 = conn.prepareStatement(SelectOrderIDStatement);
			PreparedStatement p3 = conn.prepareStatement(InsertProductOrderStatement);
			PreparedStatement p4 = conn.prepareStatement(InsertStaffOrderStatement);
			PreparedStatement p5 = conn.prepareStatement(InsertDeliveryStatement);

			conn.setAutoCommit(false);

			//Insert new order into ORDERS
			p1.setDate(1, formatOrderDate(orderDate));
			p1.executeUpdate();

			//get current value of OrderSequence
			ResultSet r = p2.executeQuery(); 
			r.next();
			//Set OrderID into ORDER_PRODUCTS, STAFF_ORDERS, COLLECTIONS statements 
			p3.setInt(1, r.getInt(1));
			p4.setInt(2, r.getInt(1));
			p5.setInt(1, r.getInt(1));

			//Insert new row into STAFF_ORDERS
			p4.setInt(1, staffID);
			p4.executeUpdate();

			//Insert new row into COLLECTIONS
			p5.setString(2, fName);
			p5.setString(3, LName);
			p5.setString(4, house);
			p5.setString(5, street);
			p5.setString(6, city);
			p5.setDate(7, java.sql.Date.valueOf(LocalDate.parse(deliveryDate, dateFormatter)));
			p5.executeUpdate();

			//Loop over each product ordered
			for (int i=0; i < productIDs.length; i++) {
				try {
					//Set ID and quantity (per row) and commit if execution completes
					p3.setInt(2, productIDs[i]);
					p3.setInt(3, quantities[i]);
					p3.executeUpdate();
					conn.commit(); 
				} catch (SQLException ex) {
					//Error in executeUpdate()
					System.err.format("SQL State: %s\n%s", ex.getSQLState(), ex.getMessage());
					System.out.println();
					System.err.println("ERROR: (1) Not enough stock for Product " + productIDs[i]);
					System.err.println("   OR: (2) Product " + productIDs[i] + " does not exist in inventory.");
					System.err.println("This product (" + productIDs[i] + ") won't be part of the current order.");
				}
			} 

			//Print out new product stocks
			Stock(conn, r.getInt(1));

			p1.clearParameters(); p3.clearParameters(); p4.clearParameters(); p5.clearParameters();
			p1.close(); p2.close(); p3.close(); p4.close(); p5.close();
			r.close();

		} catch (SQLException e) {
			System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
			//In case of any SQL error do not commit to the database
			if (conn != null) {
				try {
					System.out.println();
					System.err.println("Transaction is being rolled back");
					conn.rollback();
				} catch (SQLException excep) {
					System.err.format("SQL State: %s\n%s", excep.getSQLState(), excep.getMessage());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	/**
	* @param conn An open database connection 
	*/
	public static void option4(Connection conn) {
		//SQL statement
		String query = "SELECT I.ProductID, I.ProductDesc, CAST((SUM(PQP.Quantity)*PQP.ProductPrice) AS NUMERIC(20,2)) AS TotalValueSold"
					+	" FROM ProductQuantityPrice PQP"
					+	" JOIN INVENTORY I"
					+	" ON I.ProductID = PQP.ProductID"
					+	" GROUP BY I.ProductID, PQP.ProductPrice"
					+   " ORDER BY TotalValueSold DESC";

		//This formater allows to obtain two decimal places 
		DecimalFormat df = new DecimalFormat("#.00");
		
		try {
			//Prepared Statement
			PreparedStatement p = conn.prepareStatement(query);

			ResultSet r = p.executeQuery(); 

			//Print query results
			System.out.println("ProductID,   ProductDesc,     TotalValueSold");
			while(r.next()){
				System.out.println(r.getInt(1) + ",           " + r.getString(2) + ",               £" + df.format(r.getDouble(3)));
			}

			p.close(); r.close();

		} catch (SQLException e) {
            System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }

		
	}

	/**
	* @param conn An open database connection 
	* @param date The target date to test collection deliveries against
	*/
	public static void option5(Connection conn, String date) {
		//Changes date format
		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MMM-yy");

		//SQL Statement
		String lateCollectionStatement = "SELECT C.OrderID"
									+	" FROM COLLECTIONS C"
									+	" JOIN ORDERS O"
									+	" ON C.OrderID = O.OrderID"
									+	" WHERE O.OderCompleted = 0 "
									+	" AND CollectionDate <= ?";
		String deleteStatement = "DELETE FROM ORDERS WHERE OrderID = ?";

		try {
			conn.setAutoCommit(false);

			//String to date and then date minus 8 days
			Date d = java.sql.Date.valueOf(LocalDate.parse(date, dateFormatter));
			LocalDate d_8 = d.toLocalDate().minusDays(8);

			//Prepared Statement
			PreparedStatement p = conn.prepareStatement(lateCollectionStatement);
			PreparedStatement deleteP = conn.prepareStatement(deleteStatement);

			//Set the date in p
			p.setDate(1, java.sql.Date.valueOf(d_8));

			ResultSet r = p.executeQuery(); 

			//For each order to be deleted loop through ResultSet
			while(r.next()){
				try {
					//Set orderID (per row) and commit if execution is successful 
					deleteP.setInt(1, r.getInt(1));
					deleteP.executeUpdate();
					conn.commit();
					System.out.println("Order " + r.getInt(1) + " has been cancelled");
				} catch (SQLException ex) {
					//Error in executeUpdate()
					System.err.format("SQL State: %s\n%s", ex.getSQLState(), ex.getMessage());
					System.out.println();
					System.err.println("ERROR: Could not delete order for OrderID " + r.getInt(1));
				}
			}

			p.clearParameters(); deleteP.clearParameters();
			p.close(); deleteP.close();
			r.close();


		} catch (SQLException e) {
			System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
			//In case of any SQL error do not commit to the database
			if (conn != null) {
				try {
					System.out.println();
					System.err.println("Transaction is being rolled back");
					conn.rollback();
				} catch (SQLException excep) {
					System.err.format("SQL State: %s\n%s", excep.getSQLState(), excep.getMessage());
				}
			}
		} catch (Exception e) {
			//The only possible Java error that can occur is due to date provided by the user
			System.err.println("ERROR: Invalid date input. The operation cannot run.");
			System.err.println("HINT: May be due to date format. Always provide the date in format dd-MMM-yy.");
		}

	}

	/**
	* @param conn An open database connection 
	*/
	public static void option6(Connection conn) {
		//SQL Statement
		String query = "SELECT name, surname, totalvaluesold FROM Staff_Success(50000)";

		//This formater allows to obtain two decimal places 
		DecimalFormat df = new DecimalFormat("#.00");
		
		try {
			//Prepared Statement
			PreparedStatement p = conn.prepareStatement(query);

			ResultSet r = p.executeQuery(); 
			
			//Check if result set is empty
			if (!r.next() ) {
				System.out.println("No staff memeber sold more than 50000!");
			}

			//Print query's results
			System.out.println("EmployeeName, TotalValueSold");
			while(r.next()){
				System.out.println(r.getString(1) + " " + r.getString(2) + ",  £" + df.format(r.getDouble(3)));
			}

			p.close(); r.close();

		} catch (SQLException e) {
            System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }

	}

	/**
	* @param conn An open database connection 
	*/
	public static void option7(Connection conn) {
		//SQL statement retrieves all data needed
		String query = "SELECT B.Name, B.Surname, B.ProductID, SUM(COALESCE(A.Quantity, 0))" 
					+	" FROM (SELECT *" 
					+		" FROM ProductQuantityPrice PQP" 
					+		" WHERE PQP.ProductID IN (SELECT * FROM BestSellerProducts(20000))) A" 
					+	" RIGHT JOIN (SELECT X.StaffID, X.Name, X.Surname, X.OrderID, BSP.ProductID, X.TotalValueSold" 
					+				" FROM BestSellerProducts(20000) BSP, (SELECT X1.StaffID, X1.Name, X1.Surname, X2.OrderID, X1.TotalValueSold" 
					+												" FROM (SELECT SS.StaffID, SS.Name, SS.Surname, SO.OrderID, SS.TotalValueSold" 
					+														" FROM Staff_Success(0) SS" 
					+														" JOIN STAFF_ORDERS SO" 
					+														" ON SO.StaffID = SS.StaffID) X1" 
					+												" JOIN (SELECT * " 
					+														" FROM ORDER_PRODUCTS " 
					+														" WHERE ProductID IN (SELECT * FROM BestSellerProducts(20000))) X2" 
					+												" ON X1.OrderID = X2.OrderID) X) B" 
					+	" ON A.OrderID = B.OrderID AND A.ProductID = B.ProductID" 
					+	" GROUP BY B.Name, B.Surname, B.ProductID, B.TotalValueSold" 
					+	" ORDER BY B.TotalValueSold DESC";

		try {
			//Prepared Statement
			PreparedStatement p = conn.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

			ResultSet r = p.executeQuery(); 

			//Check if result set is empty
			if (!r.next() ) {
				System.out.println("There are no staff contributions!");
			}

			//Output:
			//1. Print result headings
			int count = 0; //Counts the number of best seller products
			System.out.print("EmployeeName");
			r.next(); 
			int firstProductID = r.getInt(3);
			do {
				System.out.print(",   Product " + r.getInt(3));
				count += 1; 
				//Check if next ProductID is equal to the first one to cicle through them only once
				r.next();
				int nextProductID = r.getInt(3);  
				if (nextProductID == firstProductID) {
					break;
				}

			} while(true);

			//Set Result set cursor back to before first row
			r.beforeFirst();
			//2. Prints results
			System.out.println();			
			while(r.next()){
				System.out.print(r.getString(1) + " " + r.getString(2));
				for (int i=1; i<= count; i++){
					System.out.print(",   " + r.getInt(4));
					if (i < count) {
						r.next();
					}
				}
				System.out.println();
			}

			p.close(); 
			r.close(); 

		} catch (SQLException e) {
            System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
	
	}

	/**
	* @param conn An open database connection 
	* @param year The target year we match employee and product sales against
	*/
	public static void option8(Connection conn, int year) {
		String query = "SELECT X.StaffID, X.Name, X.Surname" 
					+	" FROM (SELECT B.StaffID, B.Name, B.Surname, A.ProductID, SUM(A.Quantity)" 
					+			" FROM (SELECT SO.OrderID, SSY.StaffID, SSY.Name, SSY.Surname" 
					+					" FROM (SELECT * FROM Staff_Success_Yr(30000, ?)) SSY" 
					+					" JOIN STAFF_ORDERS SO" 
					+					" ON SSY.StaffID = SO.StaffID) B" 
					+			" JOIN (SELECT *" 
					+					" FROM ProductQuantityPrice PQP" 
					+					" WHERE PQP.ProductID IN (SELECT * FROM BestSellerProducts_Yr(20000, ?))) A" 
					+			" ON B.OrderID = A.OrderID" 
					+			" GROUP BY B.StaffID, B.Name, B.Surname, A.ProductID) X" 
					+	" GROUP BY X.StaffID, X.Name, X.Surname" 
					+	" HAVING COUNT(X.ProductID) >= (SELECT COUNT(*) " 
					+									" FROM BestSellerProducts_Yr(20000, ?))";

		try {
			//Prepared Statement
			PreparedStatement p = conn.prepareStatement(query);

			//Set year into the p
			//Passed as a string becuase VARCHAR(4) is the type used in query
			String yr = String.valueOf(year);
			p.setString(1, yr);
			p.setString(2, yr);
			p.setString(3, yr);

			ResultSet r = p.executeQuery();
			
			//Check if result set is empty (ie year provided does not exists in db)
			if (!r.next() ) {
				System.out.println("There are no employees of the year for this year!");
			}

			while(r.next()){
				System.out.println(r.getString(2) + " " + r.getString(3));
			}

			p.clearParameters();
			p.close(); r.close();

		} catch (SQLException e) {
            System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }

	}

    public static Connection getConnection(){
        Properties props = new Properties();
        props.setProperty("socketFactory", "org.newsclub.net.unix.AFUNIXSocketFactory$FactoryArg");

        props.setProperty("socketFactoryArg",System.getenv("PGHOST") + "/.s.PGSQL.5432");
        Connection conn;
        try{
          conn = DriverManager.getConnection("jdbc:postgresql://localhost/deptstore", props);
          return conn;
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

/*    public static Connection getConnection() {
        //This version of getConnection uses ports to connect to the server rather than sockets
        //If you use this method, you should comment out the above getConnection method, and comment out lines 19 and 21
        String user = "me";
        String passwrd = "mypassword";
        Connection conn;

        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException x) {
            System.out.println("Driver could not be loaded");
        }

        try {
            conn = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:15432/deptstore?user="+ user +"&password=" + passwrd);

            return conn;
        } catch(SQLException e) {
                e.printStackTrace();
            System.out.println("Error retrieving connection");
            return null;
        }

    }*/

	
}
