package robots;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import robots.event.RobotActionEvent;
import robots.event.RobotActionListener;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RobotTest {

    private enum EVENT {ROBOT_MOVED, ROBOT_SKIP_STEP}

    private List<EVENT> events = new ArrayList<>();
    private List<EVENT> expectedEvents = new ArrayList<>();

    private class EventsListener implements RobotActionListener {

        @Override
        public void robotIsMoved(@NotNull RobotActionEvent event) {
            events.add(EVENT.ROBOT_MOVED);
        }

        @Override
        public void robotIsSkipStep(@NotNull RobotActionEvent event) {
            events.add(EVENT.ROBOT_SKIP_STEP);
        }
    }

    private Cell cell;
    private Cell neighborCell;
    private final Direction direction = Direction.NORTH;

    private final static int DEFAULT_TEST_BATTERY_CHARGE = 10;
    private static final int AMOUNT_OF_CHARGE_FOR_MOVE = 1;
    private static final int AMOUNT_OF_CHARGE_FOR_SKIP_STEP = 2;

    private Robot robot;

    @BeforeEach
    public void testSetup() {
        // clean events
        events.clear();
        expectedEvents.clear();

        // create robot
        robot = new Robot();
        robot.setActive(true);
        robot.setBattery(new Battery(DEFAULT_TEST_BATTERY_CHARGE));
        robot.addRobotActionListener(new EventsListener());

        // create field
        cell = new Cell();
        neighborCell = new Cell();
        cell.setNeighbor(neighborCell, direction);
    }

    @Test
    public void test_setActiveAndIsActive() {
        robot.setActive(true);

        assertTrue(robot.isActive());
        assertTrue(events.isEmpty());
    }

    @Test
    public void test_canStayAtPosition_emptyCell() {
        assertTrue(Robot.canStayAtPosition(cell));
        assertTrue(events.isEmpty());
    }

    @Test
    public void test_canStayAtPosition_cellWithRobot() {
        cell.setRobot(robot);

        assertFalse(Robot.canStayAtPosition(cell));
        assertTrue(events.isEmpty());
    }

    @Test
    public void test_canStayAtPosition_cellWithBattery() {
        cell.setBattery(new Battery(DEFAULT_TEST_BATTERY_CHARGE));

        assertTrue(Robot.canStayAtPosition(cell));
        assertTrue(events.isEmpty());
    }

    @Test
    public void test_move_emptyCellInDirectionAndRobotActiveAndEnoughCharge() {
        cell.setRobot(robot);

        robot.move(direction);

        expectedEvents.add(EVENT.ROBOT_MOVED);

        assertEquals(robot, neighborCell.getRobot());
        assertEquals(neighborCell, robot.getPosition());
        assertNull(cell.getRobot());
        assertEquals(DEFAULT_TEST_BATTERY_CHARGE - AMOUNT_OF_CHARGE_FOR_MOVE, robot.getCharge());
        assertEquals(expectedEvents, events);
    }

    @Test
    public void test_move_noCellInDirectionAndRobotActiveAndEnoughCharge() {
        neighborCell.setRobot(robot);

        robot.move(Direction.NORTH);

        assertEquals(DEFAULT_TEST_BATTERY_CHARGE, robot.getCharge());
        assertEquals(neighborCell, robot.getPosition());
        assertEquals(robot, neighborCell.getRobot());
        assertTrue(events.isEmpty());
    }

    @Test
    public void test_move_emptyCellInDirectionWithWallAndRobotActiveAndEnoughCharge() {
        cell.setRobot(robot);
        new Wall(new BetweenCellsPosition(cell, neighborCell));

        robot.setBattery(new Battery(DEFAULT_TEST_BATTERY_CHARGE));
        robot.move(direction);

        assertEquals(robot, cell.getRobot());
        assertEquals(cell, robot.getPosition());
        assertNull(neighborCell.getRobot());
        assertEquals(DEFAULT_TEST_BATTERY_CHARGE, robot.getCharge());
        assertTrue(events.isEmpty());
    }

    @Test
    public void test_move_emptyCellInDirectionAndRobotNotActiveAndEnoughCharge() {
        cell.setRobot(robot);

        robot.setActive(false);
        robot.move(direction);

        assertEquals(robot, cell.getRobot());
        assertEquals(cell, robot.getPosition());
        assertNull(neighborCell.getRobot());
        assertEquals(DEFAULT_TEST_BATTERY_CHARGE, robot.getCharge());
        assertTrue(events.isEmpty());
    }

    @Test
    public void test_move_emptyCellInDirectionAndRobotActiveAndNotEnoughCharge() {
        cell.setRobot(robot);

        robot.setBattery(new Battery(0));
        robot.move(direction);

        assertEquals(robot, cell.getRobot());
        assertEquals(cell, robot.getPosition());
        assertNull(neighborCell.getRobot());
        assertEquals(0, robot.getCharge());
        assertTrue(events.isEmpty());
    }

    @Test
    public void test_skipStep_robotActiveEnoughCharge() {
        cell.setRobot(robot);

        robot.skipStep();

        expectedEvents.add(EVENT.ROBOT_SKIP_STEP);

        assertEquals(cell, robot.getPosition());
        assertEquals(robot, cell.getRobot());
        assertEquals(DEFAULT_TEST_BATTERY_CHARGE - AMOUNT_OF_CHARGE_FOR_SKIP_STEP, robot.getCharge());
        assertEquals(expectedEvents, events);
    }

    @Test
    public void test_skipStep_robotNotActiveEnoughCharge() {
        cell.setRobot(robot);

        robot.setActive(false);

        robot.skipStep();

        assertEquals(cell, robot.getPosition());
        assertEquals(robot, cell.getRobot());
        assertEquals(DEFAULT_TEST_BATTERY_CHARGE, robot.getCharge());
        assertTrue(events.isEmpty());
    }

    @Test
    public void test_skipStep_robotActiveNotEnoughCharge() {
        cell.setRobot(robot);

        robot.setBattery(new Battery(1));

        robot.skipStep();

        expectedEvents.add(EVENT.ROBOT_SKIP_STEP);

        assertEquals(cell, robot.getPosition());
        assertEquals(robot, cell.getRobot());
        assertEquals(0, robot.getCharge());
        assertEquals(expectedEvents, events);
    }

    @Test
    public void test_changeBattery_robotIsActiveCellContainsBattery() {
        cell.setRobot(robot);
        Battery newBattery = new Battery(5);
        cell.setBattery(newBattery);

        robot.changeBattery();

        assertNull(cell.getBattery());
        assertEquals(newBattery.charge(), robot.getCharge());
        assertTrue(events.isEmpty());
    }

    @Test
    public void test_changeBattery_robotIsNotActiveCellContainsBattery() {
        cell.setRobot(robot);
        Battery newBattery = new Battery(5);
        cell.setBattery(newBattery);

        Battery robotBattery = new Battery(DEFAULT_TEST_BATTERY_CHARGE);
        robot.setBattery(robotBattery);
        robot.setActive(false);

        robot.changeBattery();

        assertEquals(newBattery, cell.getBattery());
        assertEquals(robotBattery.charge(), robot.getCharge());
        assertTrue(events.isEmpty());
    }

    @Test
    public void test_changeBattery_robotIsActiveCellNotContainsBattery() {
        cell.setRobot(robot);

        Battery robotBattery = new Battery(DEFAULT_TEST_BATTERY_CHARGE);
        robot.setBattery(robotBattery);

        robot.changeBattery();

        assertEquals(robotBattery.charge(), robot.getCharge());
        assertTrue(events.isEmpty());
    }
}

