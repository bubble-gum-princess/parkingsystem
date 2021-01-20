package com.parkit.parkingsystem.integration;

import static org.mockito.Mockito.when;

import java.util.Date;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;

@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

	private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
	private static ParkingSpotDAO parkingSpotDAO;
	private static TicketDAO ticketDAO;
	private static DataBasePrepareService dataBasePrepareService;

	@Mock
	private static InputReaderUtil inputReaderUtil;

	@BeforeAll
	private static void setUp() throws Exception {
		parkingSpotDAO = new ParkingSpotDAO();
		parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
		ticketDAO = new TicketDAO();
		ticketDAO.dataBaseConfig = dataBaseTestConfig;
		dataBasePrepareService = new DataBasePrepareService();
	}

	@BeforeEach
	private void setUpPerTest() throws Exception {
		when(inputReaderUtil.readSelection()).thenReturn(1);
		when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
		dataBasePrepareService.clearDataBaseEntries();
	}

	@AfterAll
	private static void tearDown() {

	}

	@Test
	public void testParkingACar() {
		ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
		Assertions.assertNull(ticketDAO.getTicket("ABCDEF"));
		Assertions.assertEquals(1, parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR));

		parkingService.processIncomingVehicle();
		// TODO: check that a ticket is actualy saved in DB
		// and Parking table is updated with availability
		Assertions.assertNotNull(ticketDAO.getTicket("ABCDEF"));
		Assertions.assertEquals(2, parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR));
	}

	@Test
	public void testParkingLotExit() {
		ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
		ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable();
		parkingSpot.setAvailable(false);
		parkingSpotDAO.updateParking(parkingSpot);
		Date inTime = new Date();
		inTime.setTime(inTime.getTime() - 2 * 60 * 60 * 1000);
		Ticket ticketIn = new Ticket(parkingSpot, "ABCDEF", 0, inTime, null);
		ticketDAO.saveTicket(ticketIn);

		parkingService.processExitingVehicle();
		// TODO: check that the fare generated and out time are populated correctly
		// in the database
		Ticket ticketOut = ticketDAO.getTicket("ABCDEF");
		Assertions.assertNotNull(ticketOut.getOutTime());
		Assertions.assertEquals(2 * Fare.CAR_RATE_PER_HOUR, ticketOut.getPrice());

	}

}
