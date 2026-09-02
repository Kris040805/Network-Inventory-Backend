INSERT INTO network_site (
    site_code, name, address, city, country_code, latitude, longitude, status
)
VALUES
    ('SITE-SOF-001', 'Sofia Data Center', 'Kukush Street 1', 'Sofia', 'BG', 42.697700, 23.321900, 'ACTIVE'),
    ('SITE-PLO-001', 'Plovdiv Network Site', 'Ruski Boulevard 12', 'Plovdiv', 'BG', 42.135400, 24.745300, 'ACTIVE');



INSERT INTO router (
    site_id, hostname, vendor, model, serial_number, management_ip, software_version, status
)
VALUES
    (1, 'router-sof-01', 'Cisco', 'ASR 9001', 'CISCO-ASR-0001', '10.10.1.1', 'IOS XR 7.5', 'IN_SERVICE'),
    (2, 'router-plo-01', 'Juniper', 'MX204', 'JUNIPER-MX-0001', '10.20.1.1', 'Junos 23.2', 'IN_SERVICE'),
    (1, 'router-sof-02', 'Cisco', 'NCS 5501', 'CISCO-NCS-0002', '10.10.1.2', 'IOS XR 7.5', 'IN_SERVICE'),
    (2, 'router-plo-02', 'Juniper', 'PTX12000', 'JUNIPER-PTX-0002', '10.20.1.2', 'Junos 21.4', 'MAINTENANCE');



INSERT INTO shelf (
    router_id, shelf_number, shelf_type, serial_number, total_slots, status
)
VALUES
    (1, 1, 'CHASSIS', 'SHELF-0001', 4, 'ACTIVE'),
    (1, 2, 'CHASSIS', 'SHELF-0002', 4, 'ACTIVE'),
    (2, 1, 'CHASSIS', 'SHELF-0003', 4, 'ACTIVE'),
    (2, 2, 'CHASSIS', 'SHELF-0004', 4, 'ACTIVE'),
    (3, 1, 'CHASSIS', 'SHELF-0005', 4, 'ACTIVE');



INSERT INTO slot (
    shelf_id, slot_number, slot_type, status
)
VALUES
    -- Shelf 1
    (1, 1, 'LINE', 'OCCUPIED'),
    (1, 2, 'FAN', 'OCCUPIED'),
    (1, 3, 'CONTROL', 'OCCUPIED'),
    (1, 4, 'POWER', 'EMPTY'),

    -- Shelf 2
    (2, 1, 'LINE', 'OCCUPIED'),
    (2, 2, 'LINE', 'OCCUPIED'),
    (2, 3, 'CONTROL', 'EMPTY'),
    (2, 4, 'POWER', 'EMPTY'),

    -- Shelf 3
    (3, 1, 'LINE', 'OCCUPIED'),
    (3, 2, 'LINE', 'OCCUPIED'),
    (3, 3, 'CONTROL', 'EMPTY'),
    (3, 4, 'POWER', 'EMPTY'),

    -- Shelf 4
    (4, 1, 'FAN', 'OCCUPIED'),
    (4, 2, 'LINE', 'EMPTY'),
    (4, 3, 'CONTROL', 'EMPTY'),
    (4, 4, 'POWER', 'EMPTY'),

    -- Shelf 5
    (5, 1, 'LINE', 'EMPTY'),
    (5, 2, 'LINE', 'EMPTY'),
    (5, 3, 'CONTROL', 'EMPTY'),
    (5, 4, 'POWER', 'EMPTY');



INSERT INTO card (
    slot_id, part_number, serial_number, card_type, port_count, hardware_revision, status
)
VALUES
    (1, 'PN-LINE-001', 'CARD-SN-0001', 'LINE_CARD', 24, 'REV-A', 'INSTALLED'),
    (2, 'PN-LINE-001', 'CARD-SN-0002', 'LINE_CARD', 24, 'REV-A', 'INSTALLED'),
    (3, 'PN-CONTROL-001', 'CARD-SN-0003', 'CONTROL_CARD', 8, 'REV-B', 'INSTALLED'),
    (5, 'PN-LINE-001', 'CARD-SN-0004', 'LINE_CARD', 24, 'REV-A', 'INSTALLED'),
    (6, 'PN-LINE-002', 'CARD-SN-0005', 'LINE_CARD', 48, 'REV-A', 'INSTALLED'),
    (9, 'PN-LINE-001', 'CARD-SN-0006', 'LINE_CARD', 24, 'REV-A', 'INSTALLED'),
    (10, 'PN-LINE-002', 'CARD-SN-0007', 'LINE_CARD', 48, 'REV-B', 'INSTALLED'),
    (13, 'PN-LINE-001', 'CARD-SN-0008', 'LINE_CARD', 24, 'REV-A', 'INSTALLED'),
    (NULL, 'PN-LINE-001', 'CARD-SN-0009', 'LINE_CARD', 24, 'REV-A', 'IN_STOCK'),
    (NULL, 'PN-LINE-002', 'CARD-SN-0010', 'LINE_CARD', 48, 'REV-B', 'IN_STOCK'),
    (NULL, 'PN-CONTROL-001', 'CARD-SN-0011', 'CONTROL_CARD', 8, 'REV-B', 'IN_STOCK'),
    (NULL, 'PN-POWER-001', 'CARD-SN-0012', 'POWER_CARD', 0, 'REV-A', 'IN_STOCK');