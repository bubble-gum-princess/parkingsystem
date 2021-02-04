package com.parkit.parkingsystem.service;

import java.util.Calendar;
import java.util.Date;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {

	final static double HALF_HOUR = 0.5;

	public void calculateFare(Ticket ticket, boolean oldClient) {
		if ((ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime()))) {
			throw new IllegalArgumentException("Out time provided is incorrect:" + ticket.getOutTime().toString());
		}
		double duration = duration(ticket);
		double price = price(duration, ticket.getParkingSpot().getParkingType());
		if (oldClient) {
			double priceWithDiscount = reduction(price);
			ticket.setPrice(priceWithDiscount);
		} else {
			ticket.setPrice(price);
		}

	}

	private double reduction(double price) {
		double priceWithDiscount = price * 0.95;
		return priceWithDiscount;
	}

	private double duration(Ticket ticket) {
		Date inTime = clearSeconds(ticket.getInTime());
		Date outTime = clearSeconds(ticket.getOutTime());
		
		long Diff = outTime.getTime() - inTime.getTime();
		
		double durationinminutes = Diff / 1000 / 60d;
		double duration = durationinminutes / 60;
		return duration;
	}
	
	private static Date clearSeconds(Date d) {
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		c.set(Calendar.MILLISECOND, 0);
		c.set(Calendar.SECOND, 0);
		return c.getTime();
	}

	private double price(double duration, ParkingType parkingType) {
		if (duration <= HALF_HOUR) {
			return 0;
		}
		switch (parkingType) {
		case CAR: {
			return duration * Fare.CAR_RATE_PER_HOUR;
		}
		case BIKE: {
			return duration * Fare.BIKE_RATE_PER_HOUR;
		}
		default: {
			throw new IllegalArgumentException("Unkown Parking Type");
		}
		}
	}

}