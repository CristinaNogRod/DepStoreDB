Part 1: Initialization
-------------------------------------------------------------------------------------------------------------------
For INVENTORY: added positive checks for ProductPrice and ProductStockAmount because it wouldn't make sense to have negative values in these columns.
For ORDER: check constraints for OrderType to ensure it is one of three states and similarly for OderCompleted.
For ORDER_PRODUCTS: positive non-zero check for ProductQuantity,
                    primary key is defined by the OrderID and the ProductID since there will be several tuples with the same OrdeID but different ProductID, 
                    foreign keys referencing the corresponding primary keys in other tables with ON DELETE CASCADE constraint such that when the referenced row is deleted in the parent table these rows are also deleted.
For DELIVERIES and COLLECTIONS: primary key is the orderID since there might be different orders for the same customer delivery details.
For STAFF_ORDERS: primary key is defined by the order and the staff in charge of the order (since per order only one member of staff is responsible).



Part 2: Design Choices
-------------------------------------------------------------------------------------------------------------------
To improve the table structure we can think about normalisation.



Part 3: Application
-------------------------------------------------------------------------------------------------------------------
Option 1-3
-------------
SQL:
- SEQUENCE OrderSequence: sequence to allocate distinct IDs for OrderID.
- FUNCTION TRIGGER newOrder(): calls OrderSequence if the new OrderID is null.
- TRIGGER newOrder: intercepts new order being placed and executes newOrder().
- FUNCTION TRIGGER reduceStock(): updates the amount of stock left of a product by subtracting the quantity of the order from the current stock. No return because it is an AFTER trigger.
- TRIGGER stock: after new row is inserted into ORDER_PRODUCTS the trigger executes reduceStock().

JAVA:
- METHOD OrderMenu(): has no input parameters. Collects all products and their respective quantities from the user. The product IDs and the product quantities are passed into different array lists of integer. These are then processed in a for loop to create an array of two arrays. This is the output, which is then passed into option1(). If user input is invalid when asked if there is another product in the order (neither Y or N) then the data collection process stops and an error message is printed. If program fails to parse int values into int array lists then error message is displayed and user is notified that even if their input was partially valid the data won't be stored. 
- METHOD Stock(): input parameters are the connection and an order ID. No return but prints out the amount of stock left of products in a given order. It will be called in option1-3 functions where the current order ID will be provided. Uses a SELECT statement to pick out amount left in stock of products ordered. If order wans't successful then an error message will say that there are no products in the current order.
- METHOD formatOrderDate(): takes in string date and returns a Date object. This function is called in options 1-3 to format the date the order was placed. Data is formatted with dd-MMM-yy pattern. If the order date input is invalid then set date to today (assume the order was carried out today). Uses java.time.LocalDate and java.time.format.DateTimeFormatter.


OPTION 1-3:
If staffID is invalid (cannot be parsed into int type) then the option called won't run. And the initial Menu is displayed again. If staffID does not exist then SQL error message is printed and transactions rolled back.
If any SQL exception is thrown then transaction won't be committed.

OPTION 1: 
Uses 4 prepared statements: insert new order, insert new order_products, insert new staff_orders, get new OrderID from OrderSequece. 
Within the for loop used to insert rows into order_products we only commit if there is no SQLException thrown. This means that there needs to be enough stock for order to make sense. SQLException is thrown when function trigger reduceStock() fails or ProductID provided does not exist. If there is not enough stock then there will be a CHECK constraint violated so SQLException is thrown and insert statement won't be committed. If the productID does not exist then SQL exception is thrown because the is a foreign key violation. Note that the first commit() is within the for loop too such that we only commit the previous updates (new order and new staff_order) if there is at least one product which CAN be ordered. For each product which does not have enough stock or does not exist several error message are printed to the user, one saying that that product won't be added to the order.

OPTION 2-3:
Very similar to option1() but with the addition of another prepared statement to insert new row into either deliveries or collections. 
Additionally, need to check whether coolection/delivery dates are valid and whether they occur before the date order is placed. If delivery or collection dates fail (invalid input) then error message is printed and option doesn't run. 
Using delivery.compareTo(order) > 0 and delivery.compareTo(order) == 0. This checks delivery occurs after or on the same day as order date. Similarly for collection dates.


Option 4-8
-------------
SQL:
- VIEW ProductQuantityPrice: (PQP abbreviation) provides a table linking the ProductID with its OrderID (if product was ordered), with the product price and the quantity of that product sold in that order. If product wasn't ordered then quantity is 0 (done by using a left join) and orderID is null.
- FUNCTION TRIGGER addStock(): updates the amount of stock of a product by adding the quantity ordered to the current stock. No return because it is an AFTER trigger.
- TRIGGER Astock: after row is deleted from ORDER_PRODUCTS the trigger executes addStock().
- FUNCTION Staff_Success(amount): takes as input the amount by which we measure the success of the staff (ie if they sell more than 50000 pounds of products). 
[ Eventhough the value 50000 is set in the specification, I have created this function with an input parameter for ease of testing. In the query in option6 the amount will be set to 50000.] 
The function returns the table obtained from a query. 
The query links STAFF, STAFF_ORDERS and ProductQuantityPrice view table to calculate: first the quantity of each product each staff memeber sold and then times this by the price of the product and sum up the results per employee (by grouping by staffID). Select those that have only sold more than amount. Then order by the total value sold.
- FUNCTION BestSellerProducts(amount): takes as input an amount (in java query it will be set to 20000) but for ease of testing I allow this to be specified. The function returns the table obtained from a query. 
The query uses the PQP view and groups by ProductID as well as sum up the quantity sold per product. Then multiply the quantity by the price to get the total sold. Select those that have only sold more than amount.
- FUNCTION Staff_Success_Yr(amount, yr): similar to Staff_Success(amount) but consideres only the orders that occur in a certain year. Then joins this to PQP on orderID so we have the quantity of products per order linked with product price. Joining this then with the staffID we can similarly calculate by grouping and aggregate functions the amount sold per employee in a given year. Select those that have only sold more than amount. And sort them.
- FUNCTION BestSellerProducts_Yr(amount, yr): again similar to BestSellerProducts(amount) but filtering by orders that occur in a given year.


OPTION 4:
Uses PQP view (which already takes into account products not sold) and groups by productID and calculates the total sold per product. Join with INVENTORY to obtain product descriptions.
To obtain the desired format for the output the printing line has a lot of white space to align values.
Use java.text.DecimalFormat to print out TotalValueSold to two decimal places.


OPTION 5:
First uses a query to select all those orders in collections that are late to being collected. Hence consider only those with OrderCompleted = 0. Use minusDays() from java.time.LocalDate to subtract 8 days from the date provided. Convert a few times from date to localDate object. Then for each orderID from result from query we carry out a delete update and if it is successful then commit to the database and signal to the user which orders have successfully been cancelled. If in any case an order cannot be deleted due to an SQL problem then user is notified and we move onto the next orderID. [This catch block is there in case of an error (to avoid other possible deleted from not being processed) though it is unlikely to be used.] 
If date input is invalid then option does not run and user is notified.


OPTION 6:
Uses a simple query that calls Staff_Success(50000). Like option4() uses a formatter to format the total value sold (to print out the value with 2 decimal places). 
If there are no employees that sell more than 50000 then print message.


OPTION 7:
Created two functions that use queries to simplify the query in the java file option 7.
Query breakdown:
- Products whose total value sold is greater than 20000 = best sellers
- Join with PQP to obtain best sellers with their orderID quantities sold per order and product price.
- Right join to account staff memebers that didn't sell a certain best seller 
    Staff memebers that sold at least one best seller:
        - Select orders that sold best sellers
        - Select staff information from those orders by using Staff_Success(0) and joining it with STAFF_ORDERS and query above
        -> Now we have staff that sold at least one best seller
        - Cross join best seller products with list of staff that sold at least one of these (to account for staff that sold 0 of a ceertain best seller)
- Group by staff 
Since from Staff_Success(0) we obtain an ORDERED list of staff memebers based on the total they sell and we cross join in the order best seller - staff list we get a nicely formatted result: (with my test case)
            name | surname | productid | sum 
            ------+---------+-----------+-----
            Ana  | A       |         1 |  15
            Ana  | A       |         2 |   6
            Ana  | A       |         3 |   3
            Ana  | A       |         4 |   0
            
            Ben  | B       |         1 |   0
            Ben  | B       |         2 |  12
            Ben  | B       |         3 |   0
            Ben  | B       |         4 |   2
            
            Tom  | T       |         1 |   0
            Tom  | T       |         2 |   0
            Tom  | T       |         3 |   2
            Tom  | T       |         4 |   0
These visible blocks make it easy to then use java to format the output:
1. The result set's cursor must be of type TYPE_SCROLL_INSENSITIVE and concurrency CONCUR_READ_ONLY for flexibility when moving around result.
2. Loop through first block to obtain the headings of desired output (best seller productIDs). And count how many there are. 
3. Loop to print quantities per employee: first return cursor to before the first row. Then while r.next() print employee name and use a for loop to go through the 4 rows per employee block to print the product quantity sold (on the same line). Then move to the next output line and repeat.
If there are no best sellers (ie no staff contributions) then print message.


OPTION 8:
Created two functions that use complex queries to simplify the query in the java file option 8.
Query breakdown:
- Select staff that in year given sold more than 30000 join this with STAFF_ORDERS to obtain the orderIDs the staff where in charge of.
- Select best seller products for the given year and the orderIDs for orders that involve these best sellers.
- Join the above two and group by employee and productID.
- Then group by employee only and count by productID. Select staff memebers that have sold AT LEAST ONE of EACH best seller by comparing the productID count (per employee) to the number of best sellers in the given year.
The query is very similar to that of option 7 but since we don't need to take into account the best sellers not sold by the staff memeber we don't use a cross join.
If year input is not 4 digits long then option doesn't run (this occurs before calling the option). Note the year is parsed into prepared statement as a string. If for the year given there are no results then print message.




