package hu.bme.mit.spaceship;

import static junit.framework.Assert.assertFalse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GT4500Test {

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
	void fireTorpedo_Single_Success() {
		// Arrange
		when(mockPrimaryTS.fire(1)).thenReturn(true);

		// Act
		boolean result = ship.fireTorpedo(FiringMode.SINGLE);

		// Assert
		assertTrue(result);
		verify(mockPrimaryTS, times(1)).fire(1);
	}

	@Test
	void fireTorpedo_All_Success() {
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

	@ParameterizedTest
	@CsvSource({
			"true, false",
			"false, true",
			"true, true"
	})
	void fireTorpedo_All_Fail_TorpedoesJammed(boolean isPrimaryJammed, boolean isSecondaryJammed) {
		// Arrange
		int primaryTorpedoCount = mockPrimaryTS.getTorpedoCount();
		int secondaryTorpedoCount = mockSecondaryTS.getTorpedoCount();
		when(mockPrimaryTS.fire(primaryTorpedoCount)).thenReturn(!isPrimaryJammed);
		when(mockSecondaryTS.fire(secondaryTorpedoCount)).thenReturn(!isSecondaryJammed);

		// Act
		boolean result = ship.fireTorpedo(FiringMode.ALL);

		// Assert
		assertFalse(result);
		verify(mockPrimaryTS, times(1)).fire(primaryTorpedoCount);
		verify(mockSecondaryTS, times(1)).fire(secondaryTorpedoCount);
	}

	@Test
	void fireTorpedo_All_Fail_PrimaryEmpty() {
		// Arrange
		int primaryTorpedoCount = mockPrimaryTS.getTorpedoCount();
		int secondaryTorpedoCount = mockSecondaryTS.getTorpedoCount();
		when(mockPrimaryTS.fire(primaryTorpedoCount)).thenThrow(new IllegalArgumentException("numberOfTorpedos"));
		when(mockSecondaryTS.fire(secondaryTorpedoCount)).thenReturn(false);

		// Assert and Act
		assertThrows(IllegalArgumentException.class, () -> ship.fireTorpedo(FiringMode.ALL), "numberOfTorpedos");
		verify(mockPrimaryTS, times(1)).fire(primaryTorpedoCount);
		verify(mockSecondaryTS, times(0)).fire(secondaryTorpedoCount);
	}

	@Test
	void fireTorpedo_All_Fail_SecondaryEmpty() {
		// Arrange
		int primaryTorpedoCount = mockPrimaryTS.getTorpedoCount();
		int secondaryTorpedoCount = mockSecondaryTS.getTorpedoCount();
		when(mockPrimaryTS.fire(primaryTorpedoCount)).thenReturn(true);
		when(mockSecondaryTS.fire(secondaryTorpedoCount)).thenThrow(new IllegalArgumentException("numberOfTorpedos"));

		// Assert and Act
		assertThrows(IllegalArgumentException.class, () -> ship.fireTorpedo(FiringMode.ALL), "numberOfTorpedos");
		verify(mockPrimaryTS, times(1)).fire(primaryTorpedoCount);
		verify(mockSecondaryTS, times(1)).fire(secondaryTorpedoCount);
	}

	/**
	 * Test when primary torpedo store is out of torpedoes and primary ts is firing.
	 */
	@Test
	void fireTorpedo_Primary_Single_Throw() {
		// Arrange
		when(mockPrimaryTS.fire(1)).thenThrow(new IllegalArgumentException("numberOfTorpedos"));

		// Assert and Act
		assertThrows(IllegalArgumentException.class, () -> ship.fireTorpedo(FiringMode.SINGLE), "numberOfTorpedos");
		verify(mockPrimaryTS, times(1)).fire(1);
	}

	/**
	 * Test when primary torpedo store is out of torpedoes and secondary ts is firing.
	 */
	@Test
	void fireTorpedo_Secondary_Single_DoesNotThrow() {
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
	@Test
	void fireTorpedo_Primary_Single_DoesNotThrow() {
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
	@Test
	void fireTorpedo_Secondary_Single_Throw() {
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
	@Test
	void fireTorpedo_Primary_Single_Failure() {
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
	@Test
	void fireTorpedo_Secondary_Single_Failure() {
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

	/**
	 * Test when primary torpedo store is empty and primary ts is firing.
	 * Secondary can fire.
	 * Primary should fire first.
	 */
	@Test
	void fireTorpedo_Single_PrimaryEmpty_SecondaryNotEmpty() {
		// Arrange
		when(mockPrimaryTS.isEmpty()).thenReturn(true);
		when(mockSecondaryTS.isEmpty()).thenReturn(false);
		when(mockSecondaryTS.fire(1)).thenReturn(true);

		// Act
		boolean result = ship.fireTorpedo(FiringMode.SINGLE);

		// Assert
		assertTrue(result);
		verify(mockPrimaryTS, times(1)).isEmpty();
		verify(mockSecondaryTS, times(1)).isEmpty();
		verify(mockSecondaryTS, times(1)).fire(1);
	}

	/**
	 * Test when secondary torpedo store is empty and secondary ts is firing.
	 * Primary can fire.
	 * Secondary should fire first.
	 */
	@Test
	void fireTorpedo_Single_SecondaryEmpty_PrimaryNotEmpty() {
		// Arrange
		when(mockSecondaryTS.isEmpty()).thenReturn(true);
		when(mockPrimaryTS.isEmpty()).thenReturn(false);
		when(mockPrimaryTS.fire(1)).thenReturn(true);
		assertTrue(ship.fireTorpedo(FiringMode.SINGLE));

		// Act
		boolean result = ship.fireTorpedo(FiringMode.SINGLE);

		// Assert
		assertTrue(result);
		verify(mockSecondaryTS, times(1)).isEmpty();
		verify(mockPrimaryTS, times(2)).isEmpty();
		verify(mockPrimaryTS, times(2)).fire(1);
	}

	/**
	 * Both torpedoes are empty.
	 * Primary should fire first.
	 */
	@Test
	void fireTorpedo_Single_PrimaryEmpty_SecondaryEmpty() {
		// Arrange
		when(mockSecondaryTS.isEmpty()).thenReturn(true);
		when(mockPrimaryTS.isEmpty()).thenReturn(true);

		// Act
		boolean result = ship.fireTorpedo(FiringMode.SINGLE);

		// Assert
		assertFalse(result);
		verify(mockSecondaryTS, times(1)).isEmpty();
		verify(mockPrimaryTS, times(1)).isEmpty();
	}

	/**
	 * Both torpedoes are empty.
	 * Secondary should fire first.
	 */
	@Test
	void fireTorpedo_Single_SecondaryEmpty_PrimaryEmpty() {
		// Arrange
		// Emptying primary torpedo store to fire secondary store next round.
		when(mockPrimaryTS.isEmpty()).thenReturn(false);
		when(mockPrimaryTS.fire(1)).thenReturn(true);
		assertTrue(ship.fireTorpedo(FiringMode.SINGLE));
		// Resetting primary ts to be empty
		when(mockSecondaryTS.isEmpty()).thenReturn(true);
		when(mockPrimaryTS.isEmpty()).thenReturn(true);

		// Act
		boolean result = ship.fireTorpedo(FiringMode.SINGLE);

		// Assert
		assertFalse(result);
		verify(mockSecondaryTS, times(1)).isEmpty();
		verify(mockPrimaryTS, times(2)).isEmpty();
		verify(mockPrimaryTS, times(1)).fire(1);
	}
}
