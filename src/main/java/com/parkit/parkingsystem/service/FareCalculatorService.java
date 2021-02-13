package com.parkit.parkingsystem.service;

import java.time.Duration;
import java.time.LocalDateTime;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {

	final static double HALF_HOUR = 0.5;

	public void calculateFare(Ticket ticket, boolean oldClient) {
		if ((ticket.getOutTime() == null) || (ticket.getOutTime().isBefore(ticket.getInTime()))) {
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
		LocalDateTime inTime = clearSeconds(ticket.getInTime());
		LocalDateTime outTime = clearSeconds(ticket.getOutTime());
		// 0.5 => 30 minutes ;
		double duration = Duration.between(inTime, outTime).toMinutes() / 60d;
		return duration;
	}
	
	private static LocalDateTime clearSeconds(LocalDateTime d) {
		return d.withSecond(0).withNano(0);
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