package hu.bme.mit.spaceship;

import static junit.framework.Assert.assertFalse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class GT4500Test {

	private GT4500 ship;
	private TorpedoStore mockPrimaryTS;
	private TorpedoStore mockSecondaryTS;

	@BeforeEach
	public void init() {
		mockPrimaryTS = mock(TorpedoStore.class);
		mockSecondaryTS = mock(TorpedoStore.class);
		this.ship = new GT4500(mockPrimaryTS, mockSecondaryTS);
	}

	@Test
	public void fireTorpedo_Single_Success() {
		// Arrange
		when(mockPrimaryTS.fire(1)).thenReturn(true);

		// Act
		boolean result = ship.fireTorpedo(FiringMode.SINGLE);

		// Assert
		assertTrue(result);
		verify(mockPrimaryTS, times(1)).fire(1);
	}

	@Test
	public void fireTorpedo_All_Success() {
		// Arrange
		int primaryTorpedoCount = mockPrimaryTS.getTorpedoCount();
		int secondaryTorpedoCount = mockSecondaryTS.getTorpedoCount();
		when(mockPrimaryTS.fire(primaryTorpedoCount)).thenReturn(true);
		when(mockSecondaryTS.fire(secondaryTorpedoCount)).thenReturn(true);

		// Act
		boolean result = ship.fireTorpedo(FiringMode.ALL);

		// Assert
		assertTrue(result);
		verify(mockPrimaryTS, times(1)).fire(primaryTorpedoCount);
		verify(mockSecondaryTS, times(1)).fire(secondaryTorpedoCount);
	}

	/**
	 * Test when primary torpedo store is out of torpedoes and primary ts is firing.
	 */
	@Test void fireTorpedo_Primary_Single_Throw() {
		// Arrange
		when(mockPrimaryTS.fire(1)).thenThrow(new IllegalArgumentException("numberOfTorpedos"));

		// Assert and Act
		assertThrows(IllegalArgumentException.class, () -> ship.fireTorpedo(FiringMode.SINGLE), "numberOfTorpedos");
		verify(mockPrimaryTS, times(1)).fire(1);
	}

	/**
	 * Test when primary torpedo store is out of torpedoes and secondary ts is firing.
	 */
	@Test void fireTorpedo_Secondary_Single_DoesNotThrow() {
		// Arrange
		when(mockPrimaryTS.fire(1)).thenReturn(true);
		// Firing from primary torpedo store
		ship.fireTorpedo(FiringMode.SINGLE);
		// Primary is out of torpedoes
		when(mockPrimaryTS.fire(1)).thenThrow(new IllegalArgumentException("numberOfTorpedos"));
		when(mockSecondaryTS.fire(1)).thenReturn(true);


		// Assert and Act
		assertDoesNotThrow(() -> ship.fireTorpedo(FiringMode.SINGLE));
		verify(mockPrimaryTS, times(1)).fire(1);
		verify(mockSecondaryTS, times(1)).fire(1);
	}

	/**
	 * Test when secondary torpedo store is out of torpedoes and primary ts is firing.
	 */
	@Test void fireTorpedo_Primary_Single_DoesNotThrow() {
		// Arrange
		when(mockPrimaryTS.fire(1)).thenReturn(true);
		when(mockSecondaryTS.fire(1)).thenThrow(new IllegalArgumentException("numberOfTorpedos"));

		// Assert and Act
		assertDoesNotThrow(() -> ship.fireTorpedo(FiringMode.SINGLE));
		verify(mockPrimaryTS, times(1)).fire(1);
		verify(mockSecondaryTS, times(0)).fire(1);
	}

	/**
	 * Test when secondary torpedo store is out of torpedoes and secondary ts is firing.
	 */
	@Test void fireTorpedo_Secondary_Single_Throw() {
		// Arrange
		when(mockPrimaryTS.fire(1)).thenReturn(true);
		when(mockSecondaryTS.fire(1)).thenThrow(new IllegalArgumentException("numberOfTorpedos"));
		// Firing from primary torpedo store
		ship.fireTorpedo(FiringMode.SINGLE);

		// Assert and Act
		assertThrows(IllegalArgumentException.class, () -> ship.fireTorpedo(FiringMode.SINGLE), "numberOfTorpedos");
		verify(mockPrimaryTS, times(1)).fire(1);
		verify(mockSecondaryTS, times(1)).fire(1);
	}

	/**
	 * Test when primary torpedo store is jammed and primary ts is firing.
	 */
	@Test void fireTorpedo_Primary_Single_Failure() {
		// Arrange
		when(mockPrimaryTS.fire(1)).thenReturn(false);

		// Act
		boolean result = ship.fireTorpedo(FiringMode.SINGLE);

		// Assert
		assertFalse(result);
		verify(mockPrimaryTS, times(1)).fire(1);
	}

	/**
	 * Test when secondary torpedo store is jammed and secondary ts is firing.
	 */
	@Test void fireTorpedo_Secondary_Single_Failure() {
		// Arrange
		when(mockPrimaryTS.fire(1)).thenReturn(true);
		when(mockSecondaryTS.fire(1)).thenReturn(false);

		// Act
		boolean primaryResult = ship.fireTorpedo(FiringMode.SINGLE);
		boolean secondaryResult = ship.fireTorpedo(FiringMode.SINGLE);

		// Assert
		assertTrue(primaryResult);
		assertFalse(secondaryResult);
		verify(mockSecondaryTS, times(1)).fire(1);
	}
}
