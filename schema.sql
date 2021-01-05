/*Part 1: Initialisation*/
CREATE TABLE IF NOT EXISTS INVENTORY (
    ProductID  INTEGER PRIMARY KEY,
    ProductDesc VARCHAR(30) NOT NULL,
    ProductPrice NUMERIC(8,2) NOT NULL CHECK (ProductPrice > 0),
    ProductStockAmount INTEGER NOT NULL CHECK (ProductStockAmount >= 0)
);

CREATE TABLE IF NOT EXISTS ORDERS (
    OrderID  INTEGER PRIMARY KEY,
    OrderType VARCHAR(30) NOT NULL CHECK(OrderType = 'InStore' OR OrderType = 'Collection' OR OrderType = 'Delivery'),
    OderCompleted INTEGER NOT NULL CHECK(OderCompleted = 0 OR OderCompleted = 1),
    OrderPlaced DATE NOT NULL
);

CREATE TABLE IF NOT EXISTS ORDER_PRODUCTS (
    OrderID INTEGER NOT NULL,
    ProductID INTEGER NOT NULL,
    ProductQuantity INTEGER NOT NULL CHECK(ProductQuantity > 0),
    PRIMARY KEY (OrderID, ProductID),
    FOREIGN KEY (OrderID) REFERENCES ORDERS(OrderID) ON DELETE CASCADE,
    FOREIGN KEY (ProductID) REFERENCES INVENTORY(ProductID) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS DELIVERIES (
    OrderID INTEGER NOT NULL,
    FName VARCHAR(30) NOT NULL,
    LName VARCHAR(30) NOT NULL,
    House VARCHAR(30) NOT NULL,
    Street VARCHAR(30) NOT NULL,
    City VARCHAR(30) NOT NULL,
    DeliveryDate DATE NOT NULL,
    PRIMARY KEY (OrderID),
    FOREIGN KEY (OrderID) REFERENCES ORDERS(OrderID) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS COLLECTIONS (
    OrderID  INTEGER NOT NULL,
    FName VARCHAR(30) NOT NULL,
    LName VARCHAR(30) NOT NULL,
    CollectionDate DATE NOT NULL,
    PRIMARY KEY (OrderID),
    FOREIGN KEY (OrderID) REFERENCES ORDERS(OrderID) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS STAFF (
    StaffID INTEGER PRIMARY KEY,
    FName VARCHAR(30) NOT NULL,
    LName VARCHAR(30) NOT NULL
);

CREATE TABLE IF NOT EXISTS STAFF_ORDERS (
    StaffID INTEGER NOT NULL,
    OrderID INTEGER NOT NULL,
    PRIMARY KEY (StaffID, OrderID),
    FOREIGN KEY (StaffID) REFERENCES STAFF(StaffID) ON DELETE CASCADE,
    FOREIGN KEY (OrderID) REFERENCES ORDERS(OrderID) ON DELETE CASCADE
);


/*Part 3: Option 1-3*/
/*OrderID sequence*/
CREATE SEQUENCE IF NOT EXISTS OrderSequence;

/*Select new orderID with function trigger*/
CREATE OR REPLACE FUNCTION newOrder() RETURNS trigger AS $$ 
        BEGIN
        IF NEW.OrderID IS NULL THEN
        NEW.OrderID = nextVal('OrderSequence');
        END IF;
        RETURN NEW;
        END;
        $$ language plpgsql;

DROP TRIGGER IF EXISTS newOrder
    ON ORDERS;
CREATE TRIGGER newOrder BEFORE INSERT
        ON ORDERS
        FOR EACH ROW EXECUTE FUNCTION newOrder();

/*Reduce stock AFTER an order is placed in ORDER_PRODUCTS with funciton trigger*/
CREATE OR REPLACE FUNCTION reduceStock() RETURNS trigger AS $$ 
        BEGIN
            UPDATE INVENTORY 
            SET ProductStockAmount = ProductStockAmount-NEW.ProductQuantity
            WHERE ProductID = NEW.ProductID;
        RETURN NULL;
        END;
        $$ language plpgsql;

DROP TRIGGER IF EXISTS stock 
    ON ORDER_PRODUCTS;
CREATE TRIGGER stock AFTER INSERT
        ON ORDER_PRODUCTS
        FOR EACH ROW EXECUTE FUNCTION reduceStock();


/*Part 3: Option 4-8*/
/*Table view for quantity of product per order incluiding if product was never sold*/
CREATE OR REPLACE VIEW ProductQuantityPrice AS
SELECT O.OrderID, I.ProductID, COALESCE(O.ProductQuantity, 0) AS Quantity, I.ProductPrice
FROM INVENTORY I
LEFT JOIN ORDER_PRODUCTS O
ON I.ProductID = O.ProductID;

/*Option 5: Increase the stock amount for orders cancelled*/
CREATE OR REPLACE FUNCTION addStock() RETURNS trigger AS $$ 
        BEGIN
            UPDATE INVENTORY 
            SET ProductStockAmount = ProductStockAmount + OLD.ProductQuantity
            WHERE ProductID = OLD.ProductID;
        RETURN NULL;
        END;
        $$ language plpgsql;

DROP TRIGGER IF EXISTS Astock
    ON ORDER_PRODUCTS;
CREATE TRIGGER Astock AFTER DELETE
        ON ORDER_PRODUCTS
        FOR EACH ROW EXECUTE FUNCTION addStock();


/*option 6-7: total value sold per employee*/
CREATE OR REPLACE FUNCTION Staff_Success(amount INTEGER)
    RETURNS TABLE(StaffID INTEGER, Name VARCHAR(30), Surname VARCHAR(30), TotalValueSold NUMERIC(20,2))
    LANGUAGE plpgsql AS $$ 
        BEGIN
        RETURN QUERY
            SELECT S.StaffID, S.FName, S.LName, CAST(SUM(PQP.Quantity*PQP.ProductPrice) AS NUMERIC(20,2)) AS TotalValueSold
            FROM STAFF S
            JOIN STAFF_ORDERS SO ON S.StaffID = SO.StaffID
            JOIN ProductQuantityPrice PQP ON SO.OrderID = PQP.OrderID
            GROUP BY S.StaffID
            HAVING SUM(PQP.Quantity*PQP.ProductPrice) >= amount
            ORDER BY TotalValueSold DESC;
        END; $$;


/*Option 7: Best Seller Items*/
CREATE OR REPLACE FUNCTION BestSellerProducts(amount INTEGER)
    RETURNS TABLE(ProductID INTEGER)
    LANGUAGE plpgsql AS $$ 
        BEGIN
        RETURN QUERY
            SELECT PQP.ProductID
            FROM ProductQuantityPrice PQP
            GROUP BY PQP.ProductID, PQP.ProductPrice
            HAVING (SUM(PQP.Quantity)*PQP.ProductPrice) > amount;
        END; $$;


/*Option8: Staff success in a year*/
CREATE OR REPLACE FUNCTION Staff_Success_Yr(amount INTEGER, yr VARCHAR(4))
    RETURNS TABLE(StaffID INTEGER, Name VARCHAR(30), Surname VARCHAR(30))
    LANGUAGE plpgsql AS $$ 
        BEGIN
        RETURN QUERY
            SELECT S.StaffID, S.FName, S.LName
            FROM STAFF S
            JOIN (SELECT O.OrderID, SO.StaffID
                    FROM (SELECT OrderID, EXTRACT(YEAR FROM OrderPlaced) AS year
                        FROM ORDERS) O
                    JOIN STAFF_ORDERS SO
                    ON SO.OrderID = O.OrderID
                    WHERE O.year = CAST(yr AS DOUBLE PRECISION)) X 
            ON S.StaffID = X.StaffID
            JOIN ProductQuantityPrice PQP 
            ON X.OrderID = PQP.OrderID
            GROUP BY S.StaffID
            HAVING SUM(PQP.Quantity*PQP.ProductPrice) >= amount
            ORDER BY SUM(PQP.Quantity*PQP.ProductPrice) DESC;
        END; $$;


/*Option8: Best Seller Products in a year*/
CREATE OR REPLACE FUNCTION BestSellerProducts_Yr(amount INTEGER, yr VARCHAR(4))
    RETURNS TABLE(ProductID INTEGER)
    LANGUAGE plpgsql AS $$ 
        BEGIN
        RETURN QUERY
            SELECT PQP.ProductID
            FROM ProductQuantityPrice PQP
            JOIN (SELECT OrderID
                FROM ORDERS
                WHERE EXTRACT(YEAR FROM OrderPlaced) = CAST(yr AS DOUBLE PRECISION)) O
            ON O.OrderID = PQP.OrderID
            GROUP BY PQP.ProductID, PQP.ProductPrice
            HAVING (SUM(PQP.Quantity)*PQP.ProductPrice) > amount;
        END; $$;





