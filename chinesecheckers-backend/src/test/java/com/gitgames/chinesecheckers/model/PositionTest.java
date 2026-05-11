package com.gitgames.chinesecheckers.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PositionTest {

    @Test
    void topTipIsValid() {
        assertTrue(new Position(0, 0).isValid());
    }

    @Test
    void bottomTipIsValid() {
        assertTrue(new Position(16, 0).isValid());
    }

    @Test
    void widestRowMaxColIsValid() {
        assertTrue(new Position(4, 12).isValid());
        assertTrue(new Position(12, 12).isValid());
    }

    @Test
    void centerRowIsValid() {
        assertTrue(new Position(8, 0).isValid());
        assertTrue(new Position(8, 8).isValid());
    }

    @Test
    void negativeRowIsInvalid() {
        assertFalse(new Position(-1, 0).isValid());
    }

    @Test
    void rowBeyondBoardIsInvalid() {
        assertFalse(new Position(17, 0).isValid());
    }

    @Test
    void colBeyondRowWidthIsInvalid() {
        assertFalse(new Position(0, 1).isValid());
        assertFalse(new Position(8, 9).isValid());
        assertFalse(new Position(4, 13).isValid());
    }

    @Test
    void negativeColIsInvalid() {
        assertFalse(new Position(0, -1).isValid());
    }

    @Test
    void positionEquality() {
        assertEquals(new Position(3, 2), new Position(3, 2));
        assertNotEquals(new Position(3, 2), new Position(3, 1));
    }
}
