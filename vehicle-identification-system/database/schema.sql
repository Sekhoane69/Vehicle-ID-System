-- ============================================================
-- Car Identification System - PostgreSQL Database Schema
-- ============================================================

-- Drop existing tables (in reverse dependency order)
DROP TABLE IF EXISTS Tickets CASCADE;
DROP TABLE IF EXISTS police_logs CASCADE;
DROP TABLE IF EXISTS Client_queries CASCADE;
DROP TABLE IF EXISTS garage_logs CASCADE;
DROP TABLE IF EXISTS insurance_logs CASCADE;
DROP TABLE IF EXISTS Cars CASCADE;
DROP TABLE IF EXISTS Clients CASCADE;
DROP TABLE IF EXISTS Persons CASCADE;

-- ============================================================
-- PersonS TABLE (Admin + role-based access)
-- ============================================================
CREATE TABLE Persons (
    Person_id     SERIAL PRIMARY KEY,
    Personname    VARCHAR(50)  NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,  -- BCrypt hashed
    email       VARCHAR(100) NOT NULL UNIQUE,
    role        VARCHAR(20)  NOT NULL CHECK (role IN ('ADMIN','WORKSHOP','Client','POLICE','INSURANCE')),
    created_at  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    is_active   BOOLEAN      DEFAULT TRUE
);

-- ============================================================
-- ClientS TABLE
-- ============================================================
CREATE TABLE Clients (
    Client_id SERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    address     TEXT,
    phone       VARCHAR(20),
    email       VARCHAR(100) UNIQUE,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- CarS TABLE
-- ============================================================
CREATE TABLE Cars (
    Car_id          SERIAL PRIMARY KEY,
    registration_number VARCHAR(20)  NOT NULL UNIQUE,
    make                VARCHAR(50)  NOT NULL,
    model               VARCHAR(50)  NOT NULL,
    year                INT          NOT NULL,
    color               VARCHAR(30),
    owner_id            INT REFERENCES Clients(Client_id) ON DELETE SET NULL,
    status              VARCHAR(20)  DEFAULT 'ACTIVE', -- ACTIVE, STOLEN, RECOVERED, IMPOUNDED
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- SERVICE RECORDS TABLE (Workshop Module)
-- ============================================================
CREATE TABLE garage_logs (
    service_id   SERIAL PRIMARY KEY,
    Car_id   INT NOT NULL REFERENCES Cars(Car_id) ON DELETE CASCADE,
    service_date DATE NOT NULL,
    service_type VARCHAR(100) NOT NULL,
    description  TEXT,
    cost         NUMERIC(10,2) DEFAULT 0.00,
    technician   VARCHAR(100),
    status       VARCHAR(20) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'IN_PROGRESS', 'COMPLETED')),
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- SERVICE REQUESTS TABLE (Workshop Module)
-- ============================================================
CREATE TABLE bookings (
    request_id   SERIAL PRIMARY KEY,
    Car_id   INT NOT NULL REFERENCES Cars(Car_id) ON DELETE CASCADE,
    request_date DATE DEFAULT CURRENT_DATE,
    description  TEXT,
    status       VARCHAR(20) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'IN_PROGRESS', 'COMPLETED')),
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- INSURANCE RECORDS TABLE (Insurance Module)
-- ============================================================
CREATE TABLE insurance_logs (
    insurance_id    SERIAL PRIMARY KEY,
    Car_id      INT NOT NULL REFERENCES Cars(Car_id) ON DELETE CASCADE,
    provider        VARCHAR(100) NOT NULL,
    policy_number   VARCHAR(50) NOT NULL UNIQUE,
    start_date      DATE NOT NULL,
    expiry_date     DATE NOT NULL,
    premium_amount  NUMERIC(10,2),
    coverage_type   VARCHAR(50),
    status          VARCHAR(20) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE','EXPIRED','CANCELLED')),
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- Client QUERIES TABLE (Client Module)
-- ============================================================
CREATE TABLE Client_queries (
    query_id      SERIAL PRIMARY KEY,
    Client_id   INT NOT NULL REFERENCES Clients(Client_id) ON DELETE CASCADE,
    Car_id    INT REFERENCES Cars(Car_id) ON DELETE SET NULL,
    query_date    DATE NOT NULL,
    query_text    TEXT NOT NULL,
    response_text TEXT,
    status        VARCHAR(20) DEFAULT 'PENDING' CHECK (status IN ('PENDING','ANSWERED','CLOSED','POLICE_FEEDBACK','POLICE_UPDATE')),
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- POLICE REPORTS TABLE (Police Module)
-- ============================================================
CREATE TABLE police_logs (
    report_id    SERIAL PRIMARY KEY,
    Car_id   INT NOT NULL REFERENCES Cars(Car_id) ON DELETE CASCADE,
    report_date  DATE NOT NULL,
    report_type  VARCHAR(50) NOT NULL CHECK (report_type IN ('Accident','Theft','Recovered','Other')),
    description  TEXT,
    officer_name VARCHAR(100),
    badge_number VARCHAR(20),
    status       VARCHAR(20) DEFAULT 'OPEN' CHECK (status IN ('OPEN','CLOSED','PENDING')),
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- TicketS TABLE (Police Module)
-- ============================================================
CREATE TABLE Tickets (
    Ticket_id      SERIAL PRIMARY KEY,
    Car_id        INT NOT NULL REFERENCES Cars(Car_id) ON DELETE CASCADE,
    Ticket_date    DATE NOT NULL,
    Ticket_type    VARCHAR(100) NOT NULL,
    fine_amount       NUMERIC(10,2) DEFAULT 0.00,
    status            VARCHAR(10) DEFAULT 'UNPAID' CHECK (status IN ('PAID','UNPAID')),
    officer_name      VARCHAR(100),
    location          VARCHAR(200),
    payment_due_date  DATE,
    description       TEXT,
    created_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- AlertS TABLE (Global)
-- ============================================================
CREATE TABLE Alerts (
    Alert_id SERIAL PRIMARY KEY,
    sender_id       INT REFERENCES Persons(Person_id),
    recipient_id    INT REFERENCES Persons(Person_id), -- NULL if broadcast
    message         TEXT NOT NULL,
    type            VARCHAR(20) DEFAULT 'INFO' CHECK (type IN ('INFO', 'WARNING', 'SYSTEM', 'ALARM')),
    is_broadcast    BOOLEAN DEFAULT FALSE,
    is_read         BOOLEAN DEFAULT FALSE,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at      TIMESTAMP
);

-- ============================================================
-- INDEXES for performance
-- ============================================================
CREATE INDEX idx_Cars_reg    ON Cars(registration_number);
CREATE INDEX idx_Cars_owner  ON Cars(owner_id);
CREATE INDEX idx_service_Car ON garage_logs(Car_id);
CREATE INDEX idx_police_Car  ON police_logs(Car_id);
CREATE INDEX idx_Ticket_veh   ON Tickets(Car_id);
CREATE INDEX idx_insurance_veh   ON insurance_logs(Car_id);
CREATE INDEX idx_query_Client  ON Client_queries(Client_id);

-- ============================================================
-- SEED DATA
-- ============================================================

-- Default admin Person (password: admin123 — BCrypt hash)
INSERT INTO Persons (Personname, password, email, role) VALUES
('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lh2y', 'admin@vis.com', 'ADMIN'),
('workshop1', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lh2y', 'workshop@vis.com', 'WORKSHOP'),
('police1',   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lh2y', 'police@vis.com',   'POLICE'),
('insurance1','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lh2y', 'insurance@vis.com','INSURANCE');

-- Sample Clients
INSERT INTO Clients (name, address, phone, email) VALUES
('Alice Johnson',  '123 Maple St, Cityville',  '555-1001', 'alice@email.com'),
('Bob Smith',      '456 Oak Ave, Townsburg',   '555-1002', 'bob@email.com'),
('Carol Davis',    '789 Pine Rd, Villageton',  '555-1003', 'carol@email.com'),
('David Wilson',   '321 Elm Blvd, Metro City', '555-1004', 'david@email.com'),
('Eva Martinez',   '654 Cedar Ln, Uptown',     '555-1005', 'eva@email.com'),
('Frank Lee',      '987 Birch Way, Downtown',  '555-1006', 'frank@email.com'),
('Grace Kim',      '147 Walnut Dr, Eastside',  '555-1007', 'grace@email.com'),
('Henry Brown',    '258 Spruce Ct, Westend',   '555-1008', 'henry@email.com'),
('Iris Taylor',    '369 Maple Pl, Northgate',  '555-1009', 'iris@email.com'),
('James Anderson', '741 Oak Ln, Southpark',    '555-1010', 'james@email.com');

-- Sample Cars
INSERT INTO Cars (registration_number, make, model, year, color, owner_id) VALUES
('ABC-1234', 'Toyota',     'Camry',    2020, 'Silver',  1),
('XYZ-5678', 'Honda',      'Civic',    2019, 'Blue',    2),
('DEF-9012', 'Ford',       'F-150',    2021, 'Red',     3),
('GHI-3456', 'Chevrolet',  'Malibu',   2018, 'Black',   4),
('JKL-7890', 'Nissan',     'Altima',   2022, 'White',   5),
('MNO-2345', 'BMW',        '3 Series', 2020, 'Gray',    6),
('PQR-6789', 'Mercedes',   'C-Class',  2021, 'Black',   7),
('STU-0123', 'Audi',       'A4',       2019, 'Blue',    8),
('VWX-4567', 'Volkswagen', 'Jetta',    2022, 'White',   9),
('YZA-8901', 'Hyundai',    'Elantra',  2020, 'Red',    10);

-- Sample service records
INSERT INTO garage_logs (Car_id, service_date, service_type, description, cost, technician) VALUES
(1, '2024-01-10', 'Oil Change',        'Full synthetic oil change, filter replaced', 85.00,  'Mike Tech'),
(1, '2024-03-15', 'Brake Service',     'Front brake pads and rotors replaced',       320.00, 'Mike Tech'),
(2, '2024-02-20', 'Tire Rotation',     'All four tires rotated and balanced',        60.00,  'Sara Fix'),
(3, '2024-01-25', 'Engine Tune-Up',    'Spark plugs, air filter, fuel injectors',   450.00, 'Joe Wrench'),
(4, '2024-04-05', 'AC Repair',         'AC compressor replaced',                    680.00, 'Amy Cool'),
(5, '2024-03-01', 'Transmission',      'Transmission fluid flush',                  200.00, 'Bob Gear'),
(6, '2024-02-14', 'Battery Replace',   'New AGM battery installed',                 190.00, 'Mike Tech'),
(7, '2024-04-20', 'Suspension',        'Front struts and shocks replaced',           540.00, 'Sara Fix'),
(8, '2024-01-30', 'Alignment',         'Four-wheel alignment adjustment',             90.00, 'Joe Wrench'),
(9, '2024-03-22', 'Oil Change',        'Conventional oil change, filter replaced',    65.00, 'Amy Cool'),
(10,'2024-04-10', 'Windshield',        'Windshield chip repair',                      120.00, 'Bob Gear');

-- Sample insurance records
INSERT INTO insurance_logs (Car_id, provider, policy_number, start_date, expiry_date, premium_amount, coverage_type, status) VALUES
(1,  'AllState',    'AS-001234', '2024-01-01', '2025-01-01', 1200.00, 'COMPREHENSIVE', 'ACTIVE'),
(2,  'Geico',       'GC-005678', '2024-02-01', '2025-02-01',  980.00, 'THIRD PARTY',   'ACTIVE'),
(3,  'Progressive', 'PG-009012', '2023-06-01', '2024-06-01', 1450.00, 'COMPREHENSIVE', 'EXPIRED'),
(4,  'State Farm',  'SF-003456', '2024-03-01', '2025-03-01', 1100.00, 'COLLISION',     'ACTIVE'),
(5,  'Liberty',     'LM-007890', '2024-01-15', '2025-01-15',  870.00, 'COMPREHENSIVE', 'ACTIVE'),
(6,  'Nationwide',  'NW-002345', '2024-04-01', '2025-04-01', 2100.00, 'COMPREHENSIVE', 'ACTIVE'),
(7,  'AllState',    'AS-006789', '2024-02-15', '2025-02-15', 1950.00, 'COMPREHENSIVE', 'ACTIVE'),
(8,  'Geico',       'GC-000123', '2023-09-01', '2024-09-01', 1600.00, 'COLLISION',     'ACTIVE'),
(9,  'Progressive', 'PG-004567', '2024-03-15', '2025-03-15',  820.00, 'THIRD PARTY',   'ACTIVE'),
(10, 'State Farm',  'SF-008901', '2024-01-01', '2025-01-01',  950.00, 'COMPREHENSIVE', 'ACTIVE');

-- Sample police reports
INSERT INTO police_logs (Car_id, report_date, report_type, description, officer_name, badge_number, status) VALUES
(3, '2024-02-10', 'Accident', 'Minor rear-end collision at StartApp St intersection', 'Ofc. Johnson', 'P-1042', 'CLOSED'),
(5, '2024-03-05', 'Theft',    'Car reported stolen from shopping mall parking',  'Ofc. Martinez', 'P-2031', 'OPEN'),
(8, '2024-01-20', 'Accident', 'Side-swipe incident on highway ramp',                'Ofc. Davis',    'P-3015', 'CLOSED'),
(2, '2024-04-01', 'Other',    'Abandoned Car report, towed to impound',          'Ofc. Wilson',   'P-1099', 'CLOSED'),
(7, '2024-04-15', 'Accident', 'Car rolled stop sign, minor collision',           'Ofc. Johnson',  'P-1042', 'PENDING');

-- Sample Tickets
INSERT INTO Tickets (Car_id, Ticket_date, Ticket_type, fine_amount, status, officer_name, location) VALUES
(1,  '2024-01-15', 'Speeding',         150.00, 'PAID',   'Ofc. Brown',   'Highway 1, Mile 23'),
(2,  '2024-02-03', 'Illegal Parking',   75.00, 'PAID',   'Ofc. Taylor',  'Downtown Ave'),
(3,  '2024-03-12', 'Running Red Light',200.00, 'UNPAID', 'Ofc. Johnson', 'StartApp St & 5th Ave'),
(4,  '2024-01-28', 'No Seat Belt',      50.00, 'PAID',   'Ofc. Martinez','Route 66'),
(5,  '2024-04-05', 'Speeding',         175.00, 'UNPAID', 'Ofc. Davis',   'School Zone Rd'),
(6,  '2024-02-20', 'Expired Tags',     100.00, 'UNPAID', 'Ofc. Wilson',  'Parking Lot B'),
(7,  '2024-03-25', 'Illegal Parking',   75.00, 'PAID',   'Ofc. Brown',   'City Hall Plaza'),
(8,  '2024-04-10', 'DUI',             1000.00, 'UNPAID', 'Ofc. Taylor',  'Lakeview Rd'),
(9,  '2024-01-05', 'No Insurance',     500.00, 'PAID',   'Ofc. Johnson', 'Westside Blvd'),
(10, '2024-04-18', 'Speeding',         150.00, 'UNPAID', 'Ofc. Martinez','Interstate 95'),
(1,  '2024-03-08', 'Illegal U-Turn',    80.00, 'PAID',   'Ofc. Davis',   'Commerce St'),
(2,  '2024-04-22', 'Speeding',         200.00, 'UNPAID', 'Ofc. Wilson',  'Freeway On-Ramp');

-- Sample Client queries
INSERT INTO Client_queries (Client_id, Car_id, query_date, query_text, response_text, status) VALUES
(1, 1, '2024-02-01', 'When is my next service due?',           'Based on your mileage, next service is due in 3 months.', 'ANSWERED'),
(2, 2, '2024-03-10', 'Is my Car insurance still valid?',    'Your insurance is active until Feb 2025.',                'ANSWERED'),
(3, 3, '2024-04-05', 'What is the status of accident report?', NULL,                                                       'PENDING'),
(4, 4, '2024-01-20', 'How do I update my address?',             'Please visit the nearest office with ID proof.',          'ANSWERED'),
(5, 5, '2024-03-28', 'My Car was stolen, what now?',        'A police report has been filed. Report ID: 2.',           'ANSWERED');

-- Sample service requests
INSERT INTO bookings (Car_id, description, status) VALUES
(1, 'Engine making strange noise', 'PENDING'),
(2, 'Routine StartApptenance request', 'PENDING'),
(5, 'Check brakes and alignment',  'IN_PROGRESS');

-- ============================================================
-- STORED PROCEDURE: Get full Car info
-- ============================================================
CREATE OR REPLACE FUNCTION get_Car_full_info(reg_num VARCHAR)
RETURNS TABLE (
    Car_id INT, registration_number VARCHAR, make VARCHAR, model VARCHAR,
    year INT, color VARCHAR, owner_name VARCHAR, owner_phone VARCHAR
) AS $$
BEGIN
    RETURN QUERY
    SELECT v.Car_id, v.registration_number, v.make, v.model,
           v.year, v.color, c.name, c.phone
    FROM Cars v
    LEFT JOIN Clients c ON v.owner_id = c.Client_id
    WHERE v.registration_number = reg_num;
END;
$$ LANGUAGE plpgsql;

-- ============================================================
-- VIEW: Car summary with owner info
-- ============================================================
CREATE OR REPLACE VIEW Car_summary AS
SELECT
    v.Car_id,
    v.registration_number,
    v.make || ' ' || v.model AS Car_name,
    v.year,
    v.color,
    c.name   AS owner_name,
    c.phone  AS owner_phone,
    c.email  AS owner_email
FROM Cars v
-- ============================================================
-- ADDITIONAL SAMPLE DATA (Requested)
-- ============================================================

-- Additional Persons (Password: 123456 -> $2a$10$8.UnS3G.Z7zG.Z7zG.Z7zO)
INSERT INTO Persons (Personname, password, email, role) VALUES
('sarahj', '$2a$10$8.UnS3G.Z7zG.Z7zG.Z7zO', 'sarah@email.com', 'Client'),
('michaels', '$2a$10$8.UnS3G.Z7zG.Z7zG.Z7zO', 'michael@email.com', 'Client'),
('pamb', '$2a$10$8.UnS3G.Z7zG.Z7zG.Z7zO', 'pam@email.com', 'Client'),
('jimh', '$2a$10$8.UnS3G.Z7zG.Z7zG.Z7zO', 'jim@email.com', 'Client'),
('dwights', '$2a$10$8.UnS3G.Z7zG.Z7zG.Z7zO', 'dwight@email.com', 'Client');

-- Additional Clients
INSERT INTO Clients (name, address, phone, email) VALUES
('Sarah Jenkins', '101 Rose Blvd, Cityville', '555-2001', 'sarah@email.com'),
('Michael Scott', '1725 Slough Ave, Scranton', '555-2002', 'michael@email.com'),
('Pam Beesly',    '1725 Slough Ave, Scranton', '555-2003', 'pam@email.com'),
('Jim Halpert',   '1725 Slough Ave, Scranton', '555-2004', 'jim@email.com'),
('Dwight Schrute', 'Schrute Farms, Honesdale',  '555-2005', 'dwight@email.com');

-- Additional Cars (Ids 11-15)
INSERT INTO Cars (registration_number, make, model, year, color, owner_id) VALUES
('KTM-1234', 'Tesla',      'Model 3',  2023, 'White',   11),
('SCR-5678', 'Chrysler',   'Sebring',  2004, 'Silver',  12),
('PAM-9012', 'Toyota',     'Yaris',    2018, 'Blue',    13),
('JIM-3456', 'Saab',       '9-3',      2008, 'Gray',    14),
('DWT-7890', 'Pontiac',    'Firebird', 1987, 'Maroon',  15);

-- Insurance for some of the new Cars
INSERT INTO insurance_logs (Car_id, provider, policy_number, start_date, expiry_date, premium_amount, coverage_type, status) VALUES
(11, 'AllState', 'AS-999001', '2024-01-01', '2025-01-01', 1500.00, 'COMPREHENSIVE', 'ACTIVE'),
(13, 'Geico',    'GC-999003', '2024-03-01', '2025-03-01', 800.00,  'THIRD PARTY',   'ACTIVE'),
(15, 'Liberty',  'LM-999005', '2024-05-01', '2025-05-01', 1200.00, 'COMPREHENSIVE', 'ACTIVE');

-- Additional Tickets
INSERT INTO Tickets (Car_id, Ticket_date, Ticket_type, fine_amount, status, officer_name, location) VALUES
(12, '2024-05-01', 'Speeding',          250.00, 'UNPAID', 'Ofc. Dwight', 'Scranton Expressway'),
(14, '2024-05-02', 'Illegal Parking',    60.00, 'PAID',   'Ofc. Jim',    'Dunder Mifflin Parking'),
(15, '2024-05-03', 'Reckless Driving',  500.00, 'UNPAID', 'Ofc. Michael', 'Farms Road');
