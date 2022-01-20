package Name;
import battlecode.common.*;
import java.util.Random;

public class RunMiner {

    static final Direction[] directions = {
            Direction.NORTH,
            Direction.NORTHEAST,
            Direction.EAST,
            Direction.SOUTHEAST,
            Direction.SOUTH,
            Direction.SOUTHWEST,
            Direction.WEST,
            Direction.NORTHWEST,
    };

    static final Random rng = new Random(6147);

    RunMiner(RobotController rc) throws GameActionException{
        MapLocation me = rc.getLocation();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                MapLocation mineLocation = new MapLocation(me.x + dx, me.y + dy);
                while (rc.canMineGold(mineLocation)) {
                    rc.mineGold(mineLocation);
                }
                while (rc.canMineLead(mineLocation)) {
                    rc.mineLead(mineLocation);
                }
            }
        }

        // Also try to move randomly.
        if(runAwayFromEnemySoldiers(rc) == 0) {
            if(toMine(rc) != null){
                tryMove(rc, toMine(rc));
            }
            else if(awayFromEdge(rc) != null){
                tryMove(rc,awayFromEdge(rc));
            }
            else {
                Direction dir = awayFromTeam(rc);
                tryMove(rc, dir);
            }
        }
    }

    public static Direction awayFromTeam(RobotController rc) {
        MapLocation here = rc.getLocation();
        int xShift = 0;
        int yShift = 0;
        int count = 0;
        int radius = rc.getType().actionRadiusSquared;
        Team friendly = rc.getTeam();
        RobotInfo[] ourRobots = rc.senseNearbyRobots(radius, friendly);
        if(ourRobots.length == 0){
            System.out.println("No teammates found!");
            Direction dir = directions[rng.nextInt(directions.length)];
            return dir;
        }
        for(RobotInfo r : ourRobots){
            xShift += r.getLocation().x;
            yShift += r.getLocation().y;
            count ++;
        }
        MapLocation awayFrom = new MapLocation(xShift/count,yShift/count);
        return rc.getLocation().directionTo(awayFrom).opposite();
    }

    public static int runAwayFromEnemySoldiers(RobotController rc){
        int radius = rc.getType().actionRadiusSquared;
        Team opponent = rc.getTeam().opponent();
        RobotInfo[] enemies = rc.senseNearbyRobots(radius, opponent);
        if(enemies.length == 0)
            return 0;
        else{
            for(RobotInfo r:enemies){
                if(r.getType() == RobotType.SOLDIER) {
                    tryMove(rc, rc.getLocation().directionTo(r.getLocation()).opposite());
                }
                return 1;
            }
        }
        return 0;
    }

    public static Direction toMine(RobotController rc){
        try {
            MapLocation[]  visionLocations = rc.getAllLocationsWithinRadiusSquared(rc.getLocation(), rc.getType().visionRadiusSquared);
            for(MapLocation l:visionLocations){
                if(rc.senseLead(l) != 0||rc.senseGold(l) != 0){
                    return rc.getLocation().directionTo(l);
                }
            }
        }catch (GameActionException e){System.out.println(e);}
        return null;
    }

    public static Direction awayFromEdge(RobotController rc){
        MapLocation here = rc.getLocation();
        int vertical = 0;
        int horizontal = 0;
        int x = here.x;
        int y = here.y;
        if(x < 5)
            horizontal = 1;
        if(x >= rc.getMapWidth() - 5)
            horizontal = -1;
        if(y < 5)
            vertical = 1;
        if(y >= rc.getMapHeight() - 5)
            vertical = -1;
        MapLocation origin = new MapLocation(0,0);
        MapLocation location = new MapLocation(horizontal,vertical);
        if(origin.directionTo(location) != Direction.CENTER && origin.directionTo(location) != null)
            return origin.directionTo(location);
        return null;
    }

    public static void tryMove(RobotController rc, Direction dir){
        if(dir == null)
            return;
        try {
            if (rc.canMove(dir)) {
                rc.move(dir);
            }
            else if(rc.canMove(dir.rotateLeft())){
                rc.move(dir.rotateLeft());
            }
            else if(rc.canMove(dir.rotateRight())){
                rc.move(dir.rotateRight());
            }
        }
        catch(GameActionException e){
            System.out.println(e);
        }
    }
}
