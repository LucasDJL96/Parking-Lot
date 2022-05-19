package parking

import java.util.Optional

const val NOT_CREATED = "Sorry, a parking lot has not been created."

fun main() {
    val parking = ParkingLot(0)
    while (true) {
        val command = readln().split(" ")
        if (command[0] == "exit") return
        if (command[0] != "create" && !parking.created()) {
            println(NOT_CREATED)
            continue
        }
        when (command[0]) {
            "create" -> Commands.CREATE.act(command, parking)
            "park" -> Commands.PARK.act(command, parking)
            "leave" -> Commands.LEAVE.act(command, parking)
            "status" -> Commands.STATUS.act(command, parking)
            "reg_by_color" -> Commands.REG_BY_COLOR.act(command, parking)
            "spot_by_color" -> Commands.SPOT_BY_COLOR.act(command, parking)
            "spot_by_reg" -> Commands.SPOT_BY_REG.act(command, parking)
        }
    }
}

/**
 * Enum class representing the available commands for the application.
 * It doesn't include the exit command, as that one is handled directly by the app.
 */
enum class Commands {
    /**
     * Command to create a parking lot of the specified size
     *
     * @param size: the size of the parking lot
     */
    CREATE {
        override fun act(command: List<String>, parkingLot: ParkingLot) {
            val size = command[1].toInt()
            parkingLot.remake(size)
            println("Created a parking lot with $size spots.")
        }
    },

    /**
     * Command to park a car in the parking lot
     *
     * @param registrationNumber: the registration number of the car
     * @param color: the color of the car
     */
    PARK {
        override fun act(command: List<String>, parkingLot: ParkingLot) {
            val car = Car(command[1], command[2])
            val spot = parkingLot.park(car)
            println(
                if (spot != -1) "${car.color} car parked in spot $spot."
                else "Sorry, the parking lot is full."
            )
        }
    },

    /**
     * Command for a car to leave
     *
     * @param spot: the number of the spot where the car is parked
     */
    LEAVE {
        override fun act(command: List<String>, parkingLot: ParkingLot) {
            val spot = command[1].toInt()
            val success = parkingLot.leave(spot)
            println(
                if (success) "Spot $spot is free."
                else "There is no car in spot $spot."
            )
        }
    },

    /**
     * Command to get a status report of the parking lot
     */
    STATUS {
        override fun act(command: List<String>, parkingLot: ParkingLot) {
            if (parkingLot.isEmpty()) {
                println("Parking lot is empty.")
                return
            }
            parkingLot.spotsWithCars().forEach {
                println("${it.id} ${it.car.get().registrationNumber} ${it.car.get().color}")
            }
        }
    },

    /**
     * Command to find the registration numbers of cars with the specified color
     */
    REG_BY_COLOR {
        override fun act(command: List<String>, parkingLot: ParkingLot) {
            val colorCars = parkingLot.spotsWithCars().map { it.car.get() }
                .filter { it.color.lowercase() == command[1].lowercase() }
            if (colorCars.isEmpty()) {
                println("No cars with color ${command[1]} were found.")
                return
            }
            println(colorCars.joinToString(", ") { it.registrationNumber })
        }
    },

    /**
     * Command to find the spot numbers were parks of the specified color are parked
     */
    SPOT_BY_COLOR {
        override fun act(command: List<String>, parkingLot: ParkingLot) {
            val colorSpots = parkingLot.spotsWithCars()
                .filter { it.car.get().color.lowercase() == command[1].lowercase() }
            if (colorSpots.isEmpty()) {
                println("No cars with color ${command[1]} were found.")
                return
            }
            println(colorSpots.joinToString(", ") { it.id.toString() })
        }
    },

    /**
     * Command to find the spot number where the car with the specified registration number is parked
     */
    SPOT_BY_REG {
        override fun act(command: List<String>, parkingLot: ParkingLot) {
            val spot = parkingLot.spotsWithCars().filter { it.car.get().registrationNumber == command[1] }
            if (spot.isEmpty()) {
                println("No cars with registration number ${command[1]} were found.")
                return
            }
            println(spot[0].id)
        }
    };

    /**
     * Applies the corresponding action for each command
     */
    abstract fun act(command: List<String>, parkingLot: ParkingLot)

}

/**
 * Represents a parking lot
 *
 * @param size: the size of the praking lot
 */
class ParkingLot(private var size: Int) {

    /**
     * A list containing the parking spots
     */
    private var parkings: List<ParkingSpot> = List<ParkingSpot>(size) { ParkingSpot(it + 1) }

    /**
     * Checks if this parking lot has any parking spots
     */
    fun created(): Boolean {
        return size > 0
    }

    /**
     * Empties the specified spot
     *
     * @param spot: the number of the spot
     *
     * @return whether there was a car on that spot
     */
    fun leave(spot: Int): Boolean {
        return if (parkings[spot - 1].car.isPresent) {
            parkings[spot - 1].car = Optional.empty()
            true
        } else {
            false
        }
    }

    /**
     * Parks a car in the first empty spot
     *
     * @param car: the car to park
     *
     * @return the spot where the car was parked or -1 if it couldn't be parked
     */
    fun park(car: Car): Int {
        for (spot in parkings) {
            if (spot.car.isEmpty) {
                spot.car = Optional.of(car)
                return spot.id
            }
        }
        return -1
    }

    /**
     * Checks if the parking lot is empty
     *
     * @return whether the parking lot is empty or not
     */
    fun isEmpty(): Boolean {
        return parkings.all { it.car.isEmpty }
    }

    /**
     * Remakes this into an empty parking lot of the specified size
     *
     * @param newSize: the new size of the parking lot
     */
    fun remake(newSize: Int) {
        size = newSize
        parkings = List<ParkingSpot>(size) { ParkingSpot(it + 1) }
    }

    /**
     * Returns a list of the spots with cars parked
     */
    fun spotsWithCars(): List<ParkingSpot> {
        return parkings.filter { it.car.isPresent }
    }
}

/**
 * Represents a parking spot
 *
 * @param id: the number of this spot
 */
class ParkingSpot(val id: Int) {
    /**
     * Represents the car parked on this spot. Empty if no car is parked
     */
    var car: Optional<Car> = Optional.empty()
}

/**
 * Represents a car
 *
 * @param registrationNumber: the registration number of the car
 * @param color:              the color of the car
 */
class Car(val registrationNumber: String, val color: String)
